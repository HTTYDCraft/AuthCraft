package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.config.PluginConfig;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.commands.annotation.ConfigurationArgumentError;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.core.link.google.GoogleLinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.core.server.commands.annotations.GoogleUse;
import com.httydcraft.authcraft.core.util.GoogleAuthenticatorQRGenerator;
import com.httydcraft.authcraft.core.util.RandomCodeFactory;
import com.httydcraft.multimessenger.core.file.MessengerFile;
import com.httydcraft.multimessenger.core.message.Message;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.orphan.OrphanCommand;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

// #region Class Documentation
/**
 * Command for linking a Google authenticator to an account.
 * Generates a QR code and key for the authenticator setup.
 */
@CommandKey(GoogleCommand.CONFIGURATION_KEY)
public class GoogleCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "google";

    @Dependency
    private AuthPlugin plugin;
    @Dependency
    private PluginConfig config;
    @Dependency
    private AccountDatabase accountStorage;
    // #endregion

    // #region Command Execution
    /**
     * Executes the Google link command, generating a QR code and key for authenticator setup.
     *
     * @param actorWrapper The actor executing the command. Must not be null.
     * @param linkType     The link type (Google). Must not be null.
     * @param account      The account to link. Must not be null.
     */
    @GoogleUse
    @ConfigurationArgumentError("google-not-enough-arguments")
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void linkGoogle(LinkCommandActorWrapper actorWrapper, LinkType linkType, Account account) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(account, "account must not be null");

        LOGGER.atInfo().log("Processing Google link command for account: %s", account.getName());
        SecurityAuditLogger.logSuccess("GoogleCommand", account.getPlayer().orElse(null), "Google link command started for account: " + account.getName());
        try {
            String rawKey = plugin.getGoogleAuthenticator().createCredentials().getKey();
            String nickname = account.getName();
            String randomCode = "MINECRAFT_" + RandomCodeFactory.generateCode(2);
            String totpKey = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(nickname, randomCode, rawKey);

            LinkUser linkUser = account.findFirstLinkUserOrNew(GoogleLinkType.LINK_USER_FILTER, GoogleLinkType.getInstance());
            String messageKey = linkUser.isIdentifierDefaultOrNull() ? "google-generated" : "google-regenerated";
            String rawContent = linkType.getLinkMessages()
                    .getStringMessage(messageKey, linkType.newMessageContext(account))
                    .replaceAll("(?i)%google_key%", rawKey);

            Message googleQRMessage = buildGoogleQRMessage(totpKey, rawContent, linkType);
            if (googleQRMessage == null) {
                LOGGER.atSevere().log("Failed to build Google QR message for account: %s", account.getName());
                return;
            }

            actorWrapper.send(googleQRMessage);
            linkUser.getLinkUserInfo().getIdentificator().setString(rawKey);
            accountStorage.updateAccountLinks(account);
            LOGGER.atInfo().log("Google authenticator linked for account: %s", account.getName());
            SecurityAuditLogger.logSuccess("GoogleCommand", account.getPlayer().orElse(null), "Google authenticator QR generated for account: " + account.getName());
        } catch (Exception ex) {
            SecurityAuditLogger.logFailure("GoogleCommand", account.getPlayer().orElse(null), "Failed to generate Google authenticator QR for account: " + account.getName() + ", error: " + ex.getMessage());
            throw ex;
        }
    }
    // #endregion

    // #region Helper Methods
    /**
     * Builds a message containing a QR code for Google authenticator setup.
     *
     * @param key            The TOTP key for the QR code. Must not be null.
     * @param messageRawContent The raw content of the message. Must not be null.
     * @param linkType       The link type. Must not be null.
     * @return The constructed message, or {@code null} if an error occurs.
     */
    private Message buildGoogleQRMessage(String key, String messageRawContent, LinkType linkType) {
        Preconditions.checkNotNull(key, "key must not be null");
        Preconditions.checkNotNull(messageRawContent, "messageRawContent must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");

        File temporaryImageFile;
        try {
            temporaryImageFile = File.createTempFile("google-qr-image", ".png");
            BitMatrix matrix = new MultiFormatWriter().encode(key, BarcodeFormat.QR_CODE, 200, 200);
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matrix), "PNG", temporaryImageFile);
        } catch (WriterException | IOException e) {
            LOGGER.atSevere().withCause(e).log("Failed to generate Google QR code");
            return null;
        }

        Message message = linkType.newMessageBuilder(messageRawContent)
                .attachFiles(MessengerFile.of(temporaryImageFile))
                .build();
        temporaryImageFile.deleteOnExit();
        LOGGER.atFine().log("Built Google QR message");
        return message;
    }
    // #endregion
}