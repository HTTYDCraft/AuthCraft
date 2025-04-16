package com.httydcraft.authcraft.core.commands;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.database.AccountDatabase;
import com.httydcraft.authcraft.core.commands.annotation.CommandCooldown;
import com.httydcraft.authcraft.core.commands.annotation.CommandKey;
import com.httydcraft.authcraft.core.link.LinkCommandActorWrapper;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.util.CollectionUtil;
import com.httydcraft.multimessenger.core.button.ButtonColor;
import com.httydcraft.multimessenger.core.keyboard.Keyboard;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// #region Class Documentation
/**
 * Command for listing accounts, either for the user or all accounts (admin-only).
 * Supports pagination and displays accounts with a keyboard interface.
 */
@CommandKey(AccountsListCommand.CONFIGURATION_KEY)
public class AccountsListCommand implements OrphanCommand {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String CONFIGURATION_KEY = "accounts";

    @Dependency
    private AccountDatabase accountDatabase;
    @Dependency
    private LinkType linkType;
    // #endregion

    // #region Command Execution
    /**
     * Executes the accounts list command, displaying a paginated list of accounts.
     *
     * @param actorWrapper    The actor executing the command. Must not be null.
     * @param linkType        The link type associated with the command. Must not be null.
     * @param page            The page number to display (default: 1).
     * @param accountsPerPage The number of accounts per page (default: 5).
     * @param type            The type of account list to display (default: MY).
     */
    @DefaultFor("~")
    @CommandCooldown(CommandCooldown.DEFAULT_VALUE)
    public void onAccountsMenu(LinkCommandActorWrapper actorWrapper, LinkType linkType, @Flag("page") @Default("1") Integer page,
                               @Flag("pageSize") @Named("size") @Default("5") Integer accountsPerPage,
                               @Flag("type") @Default("my") AccountListType type) {
        Preconditions.checkNotNull(actorWrapper, "actorWrapper must not be null");
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(type, "type must not be null");
        Preconditions.checkArgument(page > 0, "page must be positive");
        Preconditions.checkArgument(accountsPerPage > 0, "accountsPerPage must be positive");

        LOGGER.atInfo().log("Processing accounts list command for userId: %s, type: %s, page: %d", actorWrapper.userId(), type, page);

        if (!linkType.getSettings().isAdministrator(actorWrapper.userId()) && type.isAdministratorOnly) {
            actorWrapper.reply(linkType.getLinkMessages().getMessage("not-enough-permission"));
            LOGGER.atFine().log("UserId: %s lacks permission for type: %s", actorWrapper.userId(), type);
            return;
        }

        CompletableFuture<Collection<Account>> accountsCollection = type.getAccounts(accountDatabase, linkType, actorWrapper);
        accountsCollection.thenAccept(accounts -> {
            if (accounts.isEmpty()) {
                actorWrapper.reply(linkType.getLinkMessages().getMessage(type.accountsNotFound));
                LOGGER.atFine().log("No accounts found for type: %s", type);
                return;
            }

            List<Account> paginatedAccounts = new CollectionUtil.ArrayPairHashMapAdapter.PaginatedList<>(accountsPerPage, accounts).getPage(page);
            if (paginatedAccounts.isEmpty()) {
                actorWrapper.reply(linkType.getLinkMessages().getMessage("no-page-accounts"));
                LOGGER.atFine().log("No accounts on page %d for type: %s", page, type);
                return;
            }

            Keyboard keyboard = createKeyboard(linkType, page, accountsPerPage, type.name(), paginatedAccounts);
            actorWrapper.send(linkType.newMessageBuilder(linkType.getLinkMessages().getMessage(type.accountsMessage))
                    .keyboard(keyboard)
                    .build());
            LOGGER.atInfo().log("Displayed accounts list for type: %s, page: %d", type, page);
        });
    }
    // #endregion

    // #region Helper Methods
    /**
     * Creates a keyboard for the accounts list with pagination controls.
     *
     * @param linkType        The link type. Must not be null.
     * @param currentPage     The current page number.
     * @param accountsPerPage The number of accounts per page.
     * @param accountsType    The type of accounts list.
     * @param accounts        The list of accounts to display. Must not be null.
     * @return The constructed {@code Keyboard}.
     */
    private Keyboard createKeyboard(LinkType linkType, int currentPage, int accountsPerPage, String accountsType, List<Account> accounts) {
        Preconditions.checkNotNull(linkType, "linkType must not be null");
        Preconditions.checkNotNull(accounts, "accounts must not be null");

        LOGGER.atFine().log("Creating keyboard for accounts list, page: %d, type: %s", currentPage, accountsType);
        int previousPage = currentPage - 1;
        int nextPage = currentPage + 1;
        List<String> placeholdersList = new ArrayList<>(
                Arrays.asList("%next_page%", Integer.toString(nextPage), "%previous_page%", Integer.toString(previousPage), "%prev_page%",
                        Integer.toString(previousPage), "%pageSize%", Integer.toString(accountsPerPage), "%type%", accountsType));

        for (int i = 1; i <= accounts.size(); i++) {
            Account account = accounts.get(i - 1);
            placeholdersList.add("%account_" + i + "%");
            placeholdersList.add(account.getName());
            placeholdersList.add("%account_" + i + "_color%");
            ButtonColor buttonColor = account.getPlayer().isPresent()
                    ? linkType.newButtonColorBuilder().green()
                    : linkType.newButtonColorBuilder().red();
            placeholdersList.add(buttonColor.asJsonValue());
        }

        placeholdersList.add("%account_._color%");
        placeholdersList.add(linkType.newButtonColorBuilder().white().asJsonValue());

        Keyboard keyboard = linkType.getSettings().getKeyboards().createKeyboard("accounts", placeholdersList.toArray(new String[0]));
        keyboard.removeIf(button -> button.getActionData().contains("%account"));
        LOGGER.atFine().log("Keyboard created for accounts list");
        return keyboard;
    }
    // #endregion

    // #region Enum
    /**
     * Enum defining the types of account lists that can be displayed.
     */
    public enum AccountListType {
        ALL(true, "admin-panel-no-accounts", "admin-panel-accounts") {
            @Override
            CompletableFuture<Collection<Account>> getAccounts(AccountDatabase database, LinkType linkType, LinkCommandActorWrapper actorWrapper) {
                return database.getAllAccounts();
            }
        },
        LINKED(true, "admin-panel-no-linked-accounts", "admin-panel-linked-accounts") {
            @Override
            CompletableFuture<Collection<Account>> getAccounts(AccountDatabase database, LinkType linkType, LinkCommandActorWrapper actorWrapper) {
                return database.getAllLinkedAccounts();
            }
        },
        MY(false, "no-accounts", "accounts") {
            @Override
            CompletableFuture<Collection<Account>> getAccounts(AccountDatabase database, LinkType linkType, LinkCommandActorWrapper actorWrapper) {
                return database.getAccountsFromLinkIdentificator(actorWrapper.userId());
            }
        },
        LOCAL_LINKED(true, "admin-panel-no-linked-accounts", "admin-panel-linked-accounts") {
            @Override
            CompletableFuture<Collection<Account>> getAccounts(AccountDatabase database, LinkType linkType, LinkCommandActorWrapper actorWrapper) {
                return database.getAllLinkedAccounts(linkType);
            }
        };

        private final boolean isAdministratorOnly;
        private final String accountsNotFound;
        private final String accountsMessage;

        AccountListType(boolean isAdministratorOnly, String accountsNotFound, String accountsMessage) {
            this.isAdministratorOnly = isAdministratorOnly;
            this.accountsNotFound = accountsNotFound;
            this.accountsMessage = accountsMessage;
        }

        abstract CompletableFuture<Collection<Account>> getAccounts(AccountDatabase database, LinkType linkType, LinkCommandActorWrapper actorWrapper);
    }
    // #endregion
}