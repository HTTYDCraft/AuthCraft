package com.httydcraft.authcraft.api.bucket;

import java.util.Collection;

import com.httydcraft.authcraft.api.link.user.confirmation.LinkConfirmationUser;

public interface LinkConfirmationBucket extends Bucket<LinkConfirmationUser> {

    @Deprecated
    default Collection<LinkConfirmationUser> getConfirmationUsers() {
        return getUnmodifiableRaw();
    }

    @Deprecated
    default void addLinkConfirmation(LinkConfirmationUser user) {
        modifiable().add(user);
    }

    @Deprecated
    default void removeLinkConfirmation(LinkConfirmationUser user) {
        modifiable().remove(user);
    }

}
