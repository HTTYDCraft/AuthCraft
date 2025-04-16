package com.httydcraft.authcraft.api.link.user.confirmation;

import com.httydcraft.authcraft.api.account.Account;
import com.httydcraft.authcraft.api.link.LinkType;
import com.httydcraft.authcraft.api.link.user.info.LinkUserIdentificator;
import com.httydcraft.authcraft.api.type.LinkConfirmationType;

public interface LinkConfirmationUser {
    String getConfirmationCode();

    LinkConfirmationType getLinkConfirmationType();

    Account getLinkTarget();

    long getLinkTimeoutTimestamp();

    LinkType getLinkType();

    LinkUserIdentificator getLinkUserIdentificator();

    void setLinkUserIdentificator(LinkUserIdentificator linkUserIdentificator);
}
