package com.httydcraft.authcraft.core.discord.listener;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.config.discord.DiscordConfirmationSettings;
import com.httydcraft.authcraft.core.config.discord.RoleModificationSettings;
import com.httydcraft.authcraft.api.event.AccountLinkEvent;
import com.httydcraft.authcraft.api.event.AccountStepChangeEvent;
import com.httydcraft.authcraft.core.hooks.DiscordHook;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.core.link.discord.DiscordLinkType;
import com.httydcraft.authcraft.api.link.user.LinkUser;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.link.user.info.LinkUserInfo;
import com.httydcraft.authcraft.api.server.player.ServerPlayer;
import com.httydcraft.authcraft.core.step.impl.link.DiscordLinkAuthenticationStep;
import com.httydcraft.authcraft.core.util.SecurityAuditLogger;
import io.github.revxrsal.eventbus.SubscribeEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.Collection;
import java.util.Optional;

// #region Class Documentation
/**
 * Listener for modifying Discord roles based on account link and step change events.
 * Updates roles for users based on configured settings.
 */
public class DiscordLinkRoleModifierListener {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final DiscordHook discordHook = AuthPlugin.instance().getHook(DiscordHook.class);
    private final DiscordConfirmationSettings discordConfirmationSettings = ((DiscordConfirmationSettings) DiscordLinkType.getInstance()
            .getSettings()
            .getConfirmationSettings());
    private final Collection<RoleModificationSettings> roleModificationSettings = discordConfirmationSettings.getRoleModificationSettings().values();
    private final long guildId = discordConfirmationSettings.getGuildId();
    // #endregion

    // #region Event Handlers
    /**
     * Handles account step change events to update Discord roles.
     *
     * @param event The account step change event. Must not be null.
     */
    @SubscribeEvent
    public void onDiscordAuthenticationStep(AccountStepChangeEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        if (!discordConfirmationSettings.shouldUpdateRoles()) {
            LOGGER.atFine().log("Role updates disabled for Discord");
            return;
        }
        if (!event.getOldStep().getStepName().equals(DiscordLinkAuthenticationStep.STEP_NAME)) {
            LOGGER.atFine().log("Old step is not DiscordLinkAuthenticationStep: %s", event.getOldStep().getStepName());
            return;
        }
        Account account = event.getAccount();
        SecurityAuditLogger.logSuccess("DiscordLinkRoleModifierListener: discord role update", null, "Discord role update triggered");
        if (account == null) {
            SecurityAuditLogger.logFailure("DiscordLinkRoleModifierListener", null, "Account is null on discord role update");
            LOGGER.atFine().log("Account is null");
            return;
        }
        account.findFirstLinkUser(DiscordLinkType.LINK_USER_FILTER)
                .filter(linkUser -> !linkUser.isIdentifierDefaultOrNull())
                .map(LinkUser::getLinkUserInfo)
                .map(LinkUserInfo::getIdentificator)
                .map(LinkUserIdentificator::asNumber)
                .ifPresent(discordId -> {
                    updateAccountDiscordRoles(account, discordId);
                    LOGGER.atInfo().log("Triggered role update for account: %s, discordId: %d", account.getPlayerId(), discordId);
                });
    }

    /**
     * Handles account link events to update Discord roles.
     *
     * @param event The account link event. Must not be null.
     */
    @SubscribeEvent
    public void onDiscordLink(AccountLinkEvent event) {
        Preconditions.checkNotNull(event, "event must not be null");
        Account account = event.getAccount();
        SecurityAuditLogger.logSuccess("DiscordLinkRoleModifierListener: discord role update", null, "Discord role update triggered");
        if (account == null) {
            SecurityAuditLogger.logFailure("DiscordLinkRoleModifierListener", null, "Account is null on discord role update");
            LOGGER.atFine().log("Account is null");
            return;
        }
        LinkType linkType = event.getLinkType();
        if (linkType != DiscordLinkType.getInstance()) {
            LOGGER.atFine().log("Link type is not Discord: %s", linkType.getName());
            return;
        }
        long discordId = event.getIdentificator().asNumber();
        updateAccountDiscordRoles(account, discordId);
        LOGGER.atInfo().log("Triggered role update for account: %s, discordId: %d", account.getPlayerId(), discordId);
    }
    // #endregion

    // #region Role Update Logic
    /**
     * Updates Discord roles for an account based on configuration.
     *
     * @param account   The account to update roles for. Must not be null.
     * @param discordId The Discord user ID.
     */
    private void updateAccountDiscordRoles(Account account, long discordId) {
        Preconditions.checkNotNull(account, "account must not be null");
        if (roleModificationSettings.isEmpty()) {
            LOGGER.atFine().log("No role modification settings defined");
            return;
        }
        Guild guild = discordHook.getJDA().getGuildById(guildId);
        if (guild == null) {
            LOGGER.atSevere().log("Guild not found for ID: %d", guildId);
            throw new IllegalArgumentException("Cannot find guild by id '" + guildId + "', check if guild id is valid");
        }
        guild.retrieveMemberById(discordId).queue(foundMember -> {
            if (foundMember == null) {
                LOGGER.atWarning().log("Member not found for discordId: %d", discordId);
                return;
            }
            Optional<ServerPlayer> playerOptional = account.getPlayer();
            for (RoleModificationSettings roleModification : roleModificationSettings) {
                boolean hasPermissionCheck = !roleModification.getHavePermission().isEmpty() || !roleModification.getAbsentPermission().isEmpty();
                if (hasPermissionCheck && !playerOptional.isPresent()) {
                    LOGGER.atFine().log("Skipping role modification due to missing player for roleId: %s", roleModification.getRoleId());
                    continue;
                }

                if (playerOptional.isPresent()) {
                    ServerPlayer player = playerOptional.get();
                    if (roleModification.getHavePermission().stream().anyMatch(permission -> !player.hasPermission(permission))) {
                        LOGGER.atFine().log("Player lacks required permission for roleId: %s", roleModification.getRoleId());
                        continue;
                    }
                    if (roleModification.getAbsentPermission().stream().anyMatch(player::hasPermission)) {
                        LOGGER.atFine().log("Player has forbidden permission for roleId: %s", roleModification.getRoleId());
                        continue;
                    }
                }

                Role role = guild.getRoleById(roleModification.getRoleId());
                if (role == null) {
                    LOGGER.atSevere().log("Role not found for ID: %s", roleModification.getRoleId());
                    throw new IllegalArgumentException("Cannot find role by id '" + roleModification.getRoleId() + "', check if role id is valid");
                }

                if (roleModification.getType() == RoleModificationSettings.Type.GIVE_ROLE) {
                    guild.addRoleToMember(foundMember, role).queue();
                    LOGGER.atInfo().log("Added role %s to member %d", role.getId(), discordId);
                } else if (roleModification.getType() == RoleModificationSettings.Type.REMOVE_ROLE) {
                    guild.removeRoleFromMember(foundMember, role).queue();
                    LOGGER.atInfo().log("Removed role %s from member %d", role.getId(), discordId);
                }
            }
        }, throwable -> {
            SecurityAuditLogger.logFailure("DiscordLinkRoleModifierListener", null, "Failed to update Discord roles");
            LOGGER.atSevere().withCause(throwable).log("Failed to retrieve member for discordId: %d", discordId);
        });
    }
    // #endregion
}