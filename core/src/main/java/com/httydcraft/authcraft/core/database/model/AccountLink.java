package com.httydcraft.authcraft.core.database.model;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

// #region Class Documentation
/**
 * Represents a link between an account and an external service.
 * Stores link type, user ID, enabled status, and associated account.
 */
@DatabaseTable(tableName = "auth_links")
public class AccountLink {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String LINK_TYPE_FIELD_KEY = "link_type";
    public static final String LINK_USER_ID_FIELD_KEY = "link_user_id";
    public static final String LINK_ENABLED_FIELD_KEY = "link_enabled";
    public static final String ACCOUNT_ID_FIELD_KEY = "account_id";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName = LINK_TYPE_FIELD_KEY, canBeNull = false, uniqueCombo = true)
    private String linkType;
    @DatabaseField(columnName = LINK_USER_ID_FIELD_KEY)
    private String linkUserId;
    @DatabaseField(columnName = LINK_ENABLED_FIELD_KEY, dataType = DataType.BOOLEAN_INTEGER, canBeNull = false, defaultValue = "true")
    private boolean linkEnabled;
    @DatabaseField(foreign = true, columnName = ACCOUNT_ID_FIELD_KEY, uniqueCombo = true)
    private AuthAccount account;
    // #endregion

    // #region Constructors
    /**
     * Default constructor required by ORMLite.
     */
    AccountLink() {
        LOGGER.atFine().log("Created empty AccountLink instance");
    }

    /**
     * Constructs a new {@code AccountLink} with all fields.
     *
     * @param id           The link ID.
     * @param linkType     The link type. Must not be null.
     * @param linkUserId   The link user ID.
     * @param linkEnabled  Whether the link is enabled.
     * @param account      The associated account. Must not be null.
     */
    public AccountLink(long id, String linkType, String linkUserId, boolean linkEnabled, AuthAccount account) {
        this.id = id;
        this.linkType = Preconditions.checkNotNull(linkType, "linkType must not be null");
        this.linkUserId = linkUserId;
        this.linkEnabled = linkEnabled;
        this.account = Preconditions.checkNotNull(account, "account must not be null");
        LOGGER.atFine().log("Created AccountLink with ID: %d, type: %s", id, linkType);
    }

    /**
     * Constructs a new {@code AccountLink} without ID.
     *
     * @param linkType     The link type. Must not be null.
     * @param linkUserId   The link user ID.
     * @param linkEnabled  Whether the link is enabled.
     * @param account      The associated account. Must not be null.
     */
    public AccountLink(String linkType, String linkUserId, boolean linkEnabled, AuthAccount account) {
        this.linkType = Preconditions.checkNotNull(linkType, "linkType must not be null");
        this.linkUserId = linkUserId;
        this.linkEnabled = linkEnabled;
        this.account = Preconditions.checkNotNull(account, "account must not be null");
        LOGGER.atFine().log("Created AccountLink with type: %s", linkType);
    }
    // #endregion

    // #region Getters and Setters
    /**
     * Gets the link ID.
     *
     * @return The link ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the link ID.
     *
     * @param id The link ID.
     */
    public void setId(long id) {
        this.id = id;
        LOGGER.atFine().log("Set ID for AccountLink: %d", id);
    }

    /**
     * Gets the link type.
     *
     * @return The link type.
     */
    public String getLinkType() {
        return linkType;
    }

    /**
     * Gets the link user ID.
     *
     * @return The link user ID, or null if not set.
     */
    public String getLinkUserId() {
        return linkUserId;
    }

    /**
     * Checks if the link is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isLinkEnabled() {
        return linkEnabled;
    }

    /**
     * Gets the associated account.
     *
     * @return The {@link AuthAccount}.
     */
    public AuthAccount getAccount() {
        return account;
    }
    // #endregion

    // #region Equality and HashCode
    /**
     * Checks equality with another object.
     *
     * @param o The object to compare with.
     * @return {@code true} if equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountLink)) return false;
        AccountLink that = (AccountLink) o;
        boolean equals = id == that.id &&
                linkEnabled == that.linkEnabled &&
                Objects.equals(linkType, that.linkType) &&
                Objects.equals(linkUserId, that.linkUserId) &&
                Objects.equals(account.getId(), that.account.getId());
        LOGGER.atFine().log("Checked equality for AccountLink ID %d: %b", id, equals);
        return equals;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(id, linkType, linkUserId, linkEnabled, account.getId());
        LOGGER.atFine().log("Generated hashCode for AccountLink ID %d: %d", id, hash);
        return hash;
    }
    // #endregion
}