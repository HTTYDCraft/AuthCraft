package com.httydcraft.authcraft.core.database.model;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.crypto.CryptoProvider;
import com.httydcraft.authcraft.core.database.persister.CryptoProviderPersister;
import com.httydcraft.authcraft.api.type.IdentifierType;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

// #region Class Documentation
/**
 * Represents an authentication account in the database.
 * Stores player information, password hash, and linked services.
 */
@DatabaseTable(tableName = "mc_auth_accounts")
public class AuthAccount {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    public static final String PLAYER_ID_FIELD_KEY = "player_id";
    public static final String UNIQUE_ID_FIELD_KEY = "unique_id";
    public static final String PLAYER_NAME_FIELD_KEY = "player_name";
    public static final String PASSWORD_HASH_FIELD_KEY = "password_hash";
    public static final String LAST_QUIT_TIMESTAMP_FIELD_KEY = "last_quit";
    public static final String LAST_IP_FIELD_KEY = "last_ip";
    public static final String LAST_SESSION_TIMESTAMP_START_FIELD_KEY = "last_session_start";
    public static final String PLAYER_ID_TYPE_FIELD_KEY = "player_id_type";
    public static final String HASH_TYPE_FIELD_KEY = "hash_type";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName = PLAYER_ID_FIELD_KEY, unique = true, canBeNull = false)
    private String playerId;
    @DatabaseField(columnName = PLAYER_ID_TYPE_FIELD_KEY, canBeNull = false, dataType = DataType.ENUM_NAME)
    private IdentifierType playerIdType;
    @DatabaseField(columnName = HASH_TYPE_FIELD_KEY, canBeNull = false, persisterClass = CryptoProviderPersister.class)
    private CryptoProvider cryptoProvider;
    @DatabaseField(columnName = LAST_IP_FIELD_KEY)
    private String lastIp;
    @DatabaseField(columnName = UNIQUE_ID_FIELD_KEY, canBeNull = false, dataType = DataType.UUID)
    private UUID uniqueId;
    @DatabaseField(columnName = PLAYER_NAME_FIELD_KEY, canBeNull = false)
    private String playerName;
    @DatabaseField(columnName = PASSWORD_HASH_FIELD_KEY)
    private String passwordHash;
    @DatabaseField(columnName = LAST_QUIT_TIMESTAMP_FIELD_KEY, dataType = DataType.LONG)
    private long lastQuitTimestamp;
    @DatabaseField(columnName = LAST_SESSION_TIMESTAMP_START_FIELD_KEY, dataType = DataType.LONG)
    private long lastSessionStartTimestamp;
    @ForeignCollectionField
    private ForeignCollection<AccountLink> links;
    // #endregion

    // #region Constructors
    /**
     * Default constructor required by ORMLite.
     */
    AuthAccount() {
        LOGGER.atFine().log("Created empty AuthAccount instance");
    }

    /**
     * Constructs a new {@code AuthAccount} with minimal fields.
     *
     * @param playerId     The player ID. Must not be null.
     * @param playerIdType The player ID type. Must not be null.
     * @param playerName   The player name. Must not be null.
     * @param uniqueId     The unique ID. Must not be null.
     */
    public AuthAccount(String playerId, IdentifierType playerIdType, String playerName, UUID uniqueId) {
        this.playerId = Preconditions.checkNotNull(playerId, "playerId must not be null");
        this.playerIdType = Preconditions.checkNotNull(playerIdType, "playerIdType must not be null");
        this.playerName = Preconditions.checkNotNull(playerName, "playerName must not be null");
        this.uniqueId = Preconditions.checkNotNull(uniqueId, "uniqueId must not be null");
        LOGGER.atFine().log("Created AuthAccount with playerId: %s", playerId);
    }

    /**
     * Constructs a new {@code AuthAccount} with all fields except ID.
     *
     * @param playerId                  The player ID. Must not be null.
     * @param playerIdType             The player ID type. Must not be null.
     * @param cryptoProvider           The crypto provider. Must not be null.
     * @param lastIp                   The last IP address.
     * @param uniqueId                 The unique ID. Must not be null.
     * @param playerName               The player name. Must not be null.
     * @param passwordHash             The password hash.
     * @param lastQuitTimestamp        The last quit timestamp.
     * @param lastSessionStartTimestamp The last session start timestamp.
     */
    public AuthAccount(String playerId, IdentifierType playerIdType, CryptoProvider cryptoProvider, String lastIp,
                       UUID uniqueId, String playerName, String passwordHash, long lastQuitTimestamp,
                       long lastSessionStartTimestamp) {
        this.playerId = Preconditions.checkNotNull(playerId, "playerId must not be null");
        this.playerIdType = Preconditions.checkNotNull(playerIdType, "playerIdType must not be null");
        this.cryptoProvider = Preconditions.checkNotNull(cryptoProvider, "cryptoProvider must not be null");
        this.lastIp = lastIp;
        this.uniqueId = Preconditions.checkNotNull(uniqueId, "uniqueId must not be null");
        this.playerName = Preconditions.checkNotNull(playerName, "playerName must not be null");
        this.passwordHash = passwordHash;
        this.lastQuitTimestamp = lastQuitTimestamp;
        this.lastSessionStartTimestamp = lastSessionStartTimestamp;
        LOGGER.atFine().log("Created AuthAccount with playerId: %s", playerId);
    }

    /**
     * Constructs a new {@code AuthAccount} with all fields.
     *
     * @param id                        The account ID.
     * @param playerId                  The player ID. Must not be null.
     * @param playerIdType             The player ID type. Must not be null.
     * @param cryptoProvider           The crypto provider. Must not be null.
     * @param lastIp                   The last IP address.
     * @param uniqueId                 The unique ID. Must not be null.
     * @param playerName               The player name. Must not be null.
     * @param passwordHash             The password hash.
     * @param lastQuitTimestamp        The last quit timestamp.
     * @param lastSessionStartTimestamp The last session start timestamp.
     */
    public AuthAccount(long id, String playerId, IdentifierType playerIdType, CryptoProvider cryptoProvider, String lastIp,
                       UUID uniqueId, String playerName, String passwordHash, long lastQuitTimestamp,
                       long lastSessionStartTimestamp) {
        this.id = id;
        this.playerId = Preconditions.checkNotNull(playerId, "playerId must not be null");
        this.playerIdType = Preconditions.checkNotNull(playerIdType, "playerIdType must not be null");
        this.cryptoProvider = Preconditions.checkNotNull(cryptoProvider, "cryptoProvider must not be null");
        this.lastIp = lastIp;
        this.uniqueId = Preconditions.checkNotNull(uniqueId, "uniqueId must not be null");
        this.playerName = Preconditions.checkNotNull(playerName, "playerName must not be null");
        this.passwordHash = passwordHash;
        this.lastQuitTimestamp = lastQuitTimestamp;
        this.lastSessionStartTimestamp = lastSessionStartTimestamp;
        LOGGER.atFine().log("Created AuthAccount with ID: %d, playerId: %s", id, playerId);
    }
    // #endregion

    // #region Getters and Setters
    /**
     * Gets the account ID.
     *
     * @return The account ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the account ID.
     *
     * @param id The account ID.
     */
    public void setId(long id) {
        this.id = id;
        LOGGER.atFine().log("Set ID for AuthAccount: %d", id);
    }

    /**
     * Gets the player ID.
     *
     * @return The player ID.
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets the player ID type.
     *
     * @return The {@link IdentifierType}.
     */
    public IdentifierType getPlayerIdType() {
        return playerIdType;
    }

    /**
     * Gets the crypto provider.
     *
     * @return The {@link CryptoProvider}.
     */
    public CryptoProvider getHashType() {
        return cryptoProvider;
    }

    /**
     * Sets the crypto provider.
     *
     * @param cryptoProvider The crypto provider. Must not be null.
     */
    public void setHashType(CryptoProvider cryptoProvider) {
        this.cryptoProvider = Preconditions.checkNotNull(cryptoProvider, "cryptoProvider must not be null");
        LOGGER.atFine().log("Set crypto provider for AuthAccount ID %d: %s", id, cryptoProvider.getIdentifier());
    }

    /**
     * Gets the last IP address.
     *
     * @return The last IP address, or null if not set.
     */
    public String getLastIp() {
        return lastIp;
    }

    /**
     * Sets the last IP address.
     *
     * @param lastIp The last IP address.
     */
    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
        LOGGER.atFine().log("Set last IP for AuthAccount ID %d: %s", id, lastIp);
    }

    /**
     * Gets the unique ID.
     *
     * @return The {@link UUID}.
     */
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Gets the player name.
     *
     * @return The player name.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the password hash.
     *
     * @return The password hash, or null if not set.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     *
     * @param passwordHash The password hash.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        LOGGER.atFine().log("Set password hash for AuthAccount ID %d", id);
    }

    /**
     * Gets the last quit timestamp.
     *
     * @return The last quit timestamp.
     */
    public long getLastQuitTimestamp() {
        return lastQuitTimestamp;
    }

    /**
     * Sets the last quit timestamp.
     *
     * @param lastQuitTimestamp The last quit timestamp.
     */
    public void setLastQuitTimestamp(long lastQuitTimestamp) {
        this.lastQuitTimestamp = lastQuitTimestamp;
        LOGGER.atFine().log("Set last quit timestamp for AuthAccount ID %d: %d", id, lastQuitTimestamp);
    }

    /**
     * Gets the last session start timestamp.
     *
     * @return The last session start timestamp.
     */
    public long getLastSessionStartTimestamp() {
        return lastSessionStartTimestamp;
    }

    /**
     * Sets the last session start timestamp.
     *
     * @param lastSessionStartTimestamp The last session start timestamp.
     */
    public void setLastSessionStartTimestamp(long lastSessionStartTimestamp) {
        this.lastSessionStartTimestamp = lastSessionStartTimestamp;
        LOGGER.atFine().log("Set last session start timestamp for AuthAccount ID %d: %d", id, lastSessionStartTimestamp);
    }

    /**
     * Gets the collection of links.
     *
     * @return The {@link ForeignCollection} of {@link AccountLink}.
     */
    public ForeignCollection<AccountLink> getLinks() {
        return links;
    }
    // #endregion
}