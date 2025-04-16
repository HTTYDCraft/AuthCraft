
package com.httydcraft.authcraft.core.account.factory;

// region Imports
import java.util.UUID;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.core.account.AuthAccountAdapter;
import com.httydcraft.authcraft.core.database.model.AuthAccount;
import com.httydcraft.authcraft.api.type.IdentifierType;
import com.google.common.collect.Lists;
// endregion

/**
 * Factory for creating {@link AuthAccountAdapter} instances.
 * This class extends {@link AccountFactoryTemplate} to provide the logic
 * for creating a new {@link Account} with customized account handling.
 */
public class AuthAccountFactory extends AccountFactoryTemplate {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    // region Account Creation

    /**
     * Creates a new {@link AuthAccountAdapter} instance.
     *
     * @param id The unique identifier for the account
     * @param identifierType The type of identifier (e.g., UUID or name)
     * @param uniqueId The universally unique identifier
     * @param name The name associated with the account
     * @return A new {@link Account} instance
     * @throws NullPointerException if any of the parameters are null
     */
    @Override
    protected Account newAccount(String id, IdentifierType identifierType, UUID uniqueId, String name) {
        // Preconditions to validate method arguments
        Preconditions.checkNotNull(id, "ID cannot be null");
        Preconditions.checkNotNull(identifierType, "IdentifierType cannot be null");
        Preconditions.checkNotNull(uniqueId, "UUID cannot be null");
        Preconditions.checkNotNull(name, "Name cannot be null");

        logger.atInfo().log("Creating new account with ID: %s", id);

        AuthAccount authAccount = new AuthAccount(id, identifierType, name, uniqueId);

        // Using Guava's Lists.newArrayList for better performance and readability
        return new AuthAccountAdapter(authAccount, Lists.newArrayList());
    }
    // endregion
}
