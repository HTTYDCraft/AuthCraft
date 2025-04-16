/* BaseAuthenticationStepContextFactoryBucket.java */
/**
 * A bucket managing AuthenticationStepContextFactoryWrappers.
 */
package com.httydcraft.authcraft.core.bucket;

import java.util.List;
import java.util.Optional;
import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.bucket.AuthenticationStepContextFactoryBucket;
import com.httydcraft.authcraft.api.bucket.AuthenticationStepContextFactoryBucket.AuthenticationStepContextFactoryWrapper;
import com.httydcraft.authcraft.api.factory.AuthenticationStepContextFactory;
import com.httydcraft.authcraft.api.step.AuthenticationStepContext;
import com.httydcraft.authcraft.core.step.context.BaseAuthenticationStepContext;
import com.google.common.flogger.GoogleLogger;

public class BaseAuthenticationStepContextFactoryBucket extends BaseListBucket<AuthenticationStepContextFactoryWrapper> implements AuthenticationStepContextFactoryBucket {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
    private final List<String> stepNames;

    /**
     * Constructs a bucket with specified step names.
     *
     * @param stepNames List of step names
     */
    public BaseAuthenticationStepContextFactoryBucket(List<String> stepNames) {
        this.stepNames = stepNames;
        logger.atInfo().log("BaseAuthenticationStepContextFactoryBucket instantiated");
    }

    /**
     * Creates a context based on the given account.
     *
     * @param account The account for which context is created
     * @return The created AuthenticationStepContext
     */
    public AuthenticationStepContext createContext(Account account) {
        Preconditions.checkNotNull(account, "Account cannot be null");
        String stepName = stepNames.get(0);
        if (stepNames.size() > account.getCurrentAuthenticationStepCreatorIndex())
            stepName = stepNames.get(account.getCurrentAuthenticationStepCreatorIndex());
        AuthenticationStepContext context = createContext(stepName, account);
        logger.atInfo().log("Context created for account: %s", account);
        return context;
    }

    /**
     * Creates a context for a specific step name and account.
     *
     * @param stepName The step name
     * @param account The associated account
     * @return The created AuthenticationStepContext
     */
    public AuthenticationStepContext createContext(String stepName, Account account) {
        Preconditions.checkNotNull(stepName, "Step name cannot be null");
        Preconditions.checkNotNull(account, "Account cannot be null");
        Optional<AuthenticationStepContextFactoryWrapper> wrapperOptional = findFirstByValue(AuthenticationStepContextFactoryWrapper::getIdentifier, stepName);
        AuthenticationStepContextFactory defaultFactory = AuthenticationStepContextFactory.of(new BaseAuthenticationStepContext(account));
        logger.atInfo().log("Context created: StepName=%s, Account=%s", stepName, account);
        return wrapperOptional.orElse(AuthenticationStepContextFactoryWrapper.of(stepName, defaultFactory)).createContext(account);
    }

}