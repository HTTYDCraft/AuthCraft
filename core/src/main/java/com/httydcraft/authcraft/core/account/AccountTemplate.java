
package com.httydcraft.authcraft.core.account;

// region Imports
import java.util.concurrent.CompletableFuture;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.AuthPlugin;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.event.AccountNewStepRequestEvent;
import com.httydcraft.authcraft.api.event.AccountStepChangeEvent;
import com.httydcraft.authcraft.api.factory.AuthenticationStepFactory;
import com.httydcraft.authcraft.api.step.AuthenticationStep;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import io.github.revxrsal.eventbus.PostResult;
import com.httydcraft.authcraft.core.step.impl.NullAuthenticationStep;
import com.httydcraft.authcraft.core.step.impl.NullAuthenticationStep.NullAuthenticationStepFactory;
// endregion

/**
 * Template for account operations providing default behavior for authentication steps.
 * Implements account interface and provides helper methods for authentication step management.
 */
public abstract class AccountTemplate implements Account, Comparable<AccountTemplate> {

    private static final AuthPlugin PLUGIN = AuthPlugin.instance();
    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    private Integer currentConfigurationAuthenticationStepCreatorIndex = 0;
    private AuthenticationStep currentAuthenticationStep = new NullAuthenticationStep();

    // region Authentication Management

    /**
     * Advances the account to the next authentication step subject to specific conditions.
     *
     * @param stepContext the context of the current authentication step
     * @return A CompletableFuture for handling asynchronous step management
     */
    @Override
    public CompletableFuture<Void> nextAuthenticationStep(AuthenticationStepContext stepContext) {
        logger.atInfo().log("Processing next authentication step for account");
        Preconditions.checkNotNull(stepContext, "AuthenticationStepContext cannot be null");

        return PLUGIN.getEventBus().publish(AccountNewStepRequestEvent.class, this, false, stepContext)
                .thenAcceptAsync(stepRequestEventPostResult -> {
                    if (stepRequestEventPostResult.getEvent().isCancelled())
                        return;
                    if (currentAuthenticationStep != null && !currentAuthenticationStep.shouldPassToNextStep())
                        return;
                    if (PLUGIN.getConfig().getAuthenticationSteps().size() <= currentConfigurationAuthenticationStepCreatorIndex) {
                        currentConfigurationAuthenticationStepCreatorIndex = 0;
                        return;
                    }
                    String stepCreatorName = PLUGIN.getConfig().getAuthenticationStepName(currentConfigurationAuthenticationStepCreatorIndex);
                    AuthenticationStepFactory stepFactory = PLUGIN.getAuthenticationStepFactoryBucket()
                            .findFirst(creator -> creator.getAuthenticationStepName().equals(stepCreatorName))
                            .orElse(new NullAuthenticationStepFactory());
                    AuthenticationStep newStep = stepFactory.createNewAuthenticationStep(stepContext);
                    PostResult<AccountStepChangeEvent> result = PLUGIN.getEventBus()
                            .publish(AccountStepChangeEvent.class, this, false, stepContext, currentAuthenticationStep, newStep)
                            .join();
                    if (result.getEvent().isCancelled())
                        return;
                    currentAuthenticationStep = newStep;
                    currentConfigurationAuthenticationStepCreatorIndex += 1;
                    if (currentAuthenticationStep.shouldSkip()) {
                        currentAuthenticationStep = new NullAuthenticationStep();
                        nextAuthenticationStep(PLUGIN.getAuthenticationContextFactoryBucket().createContext(this)).join();
                    }
                });
    }

    /**
     * Gets the current authentication step.
     *
     * @return Current authentication step
     */
    @Override
    public AuthenticationStep getCurrentAuthenticationStep() {
        return currentAuthenticationStep;
    }
    // endregion

    // region Session Management

    /**
     * Checks if the session is still active.
     *
     * @param sessionDurability Duration for session validity
     * @return True if session is active, otherwise false
     */
    @Override
    public boolean isSessionActive(long sessionDurability) {
        long sessionEndTime = getLastSessionStartTimestamp() + sessionDurability;
        boolean isActive = sessionEndTime >= System.currentTimeMillis() && !PLUGIN.getAuthenticatingAccountBucket().isAuthenticating(this);
        logger.atInfo().log("Account session active: %b", isActive);
        return isActive;
    }
    // endregion

    // region Getters and Setters

    /**
     * Gets the current index of the authentication step creator.
     *
     * @return current index of authentication step creator
     */
    @Override
    public int getCurrentAuthenticationStepCreatorIndex() {
        return currentConfigurationAuthenticationStepCreatorIndex;
    }

    /**
     * Sets the current index of the authentication step creator.
     *
     * @param index The new index for the authentication step creator
     */
    @Override
    public void setCurrentAuthenticationStepCreatorIndex(int index) {
        Preconditions.checkArgument(index >= 0, "Index must be non-negative");
        String stepName = PLUGIN.getConfig().getAuthenticationStepName(index);
        AuthenticationStepFactory stepFactory = PLUGIN.getAuthenticationStepFactoryBucket()
                .findFirst(creator -> creator.getAuthenticationStepName().equals(stepName))
                .orElse(new NullAuthenticationStepFactory());
        AuthenticationStepContext stepContext = PLUGIN.getAuthenticationContextFactoryBucket().createContext(stepName, this);
        currentConfigurationAuthenticationStepCreatorIndex = index;
        currentAuthenticationStep = stepFactory.createNewAuthenticationStep(stepContext);
        logger.atInfo().log("Authentication step set to index: %d", index);
    }
    // endregion
}
