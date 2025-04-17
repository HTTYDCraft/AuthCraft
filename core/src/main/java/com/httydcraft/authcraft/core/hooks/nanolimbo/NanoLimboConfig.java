package com.httydcraft.authcraft.core.hooks.nanolimbo;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import ua.nanit.limbo.configuration.LimboConfig;
import ua.nanit.limbo.server.data.BossBar;
import ua.nanit.limbo.server.data.InfoForwarding;
import ua.nanit.limbo.server.data.PingData;
import ua.nanit.limbo.server.data.Title;

import java.net.SocketAddress;
import java.time.Duration;

// #region Class Documentation
/**
 * Configuration for NanoLimbo server.
 * Implements {@link LimboConfig} to provide settings for the limbo server.
 */
@ConfigSerializable
public class NanoLimboConfig implements LimboConfig {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private final PingData pingData;
    private final SocketAddress address;
    private final InfoForwarding forwarding;

    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code NanoLimboConfig}.
     *
     * @param address    The server address. Must not be null.
     * @param forwarding The info forwarding configuration. Must not be null.
     */
    public NanoLimboConfig(SocketAddress address, InfoForwarding forwarding) {
        this.address = Preconditions.checkNotNull(address, "address must not be null");
        this.forwarding = Preconditions.checkNotNull(forwarding, "forwarding must not be null");
        this.pingData = new PingData();
        this.pingData.setDescription("NanoLimbo");
        this.pingData.setVersion("NanoLimbo");
        LOGGER.atFine().log("Initialized NanoLimboConfig with address: %s", address);
    }
    // #endregion

    // #region Configuration Methods
    /**
     * Gets the server address.
     *
     * @return The {@link SocketAddress}.
     */
    @Override
    public SocketAddress getAddress() {
        LOGGER.atFine().log("Retrieved address: %s", address);
        return address;
    }

    /**
     * Gets the maximum number of players.
     *
     * @return The maximum number of players (-1 for unlimited).
     */
    @Override
    public int getMaxPlayers() {
        LOGGER.atFine().log("Retrieved max players: -1");
        return -1;
    }

    /**
     * Gets the ping data.
     *
     * @return The {@link PingData}.
     */
    @Override
    public PingData getPingData() {
        LOGGER.atFine().log("Retrieved ping data");
        return pingData;
    }

    /**
     * Gets the dimension type.
     *
     * @return The dimension type ("the_end").
     */
    @Override
    public String getDimensionType() {
        LOGGER.atFine().log("Retrieved dimension type: the_end");
        return "the_end";
    }

    /**
     * Gets the game mode.
     *
     * @return The game mode (2 for Adventure).
     */
    @Override
    public int getGameMode() {
        LOGGER.atFine().log("Retrieved game mode: 2");
        return 2; // Adventure game mode
    }

    /**
     * Gets the info forwarding configuration.
     *
     * @return The {@link InfoForwarding}.
     */
    @Override
    public InfoForwarding getInfoForwarding() {
        LOGGER.atFine().log("Retrieved info forwarding");
        return forwarding;
    }

    /**
     * Gets the read timeout in milliseconds.
     *
     * @return The read timeout (30 seconds).
     */
    @Override
    public long getReadTimeout() {
        LOGGER.atFine().log("Retrieved read timeout: 30s");
        return Duration.ofSeconds(30).toMillis();
    }

    /**
     * Gets the debug level.
     *
     * @return The debug level (0 for errors only).
     */
    @Override
    public int getDebugLevel() {
        LOGGER.atFine().log("Retrieved debug level: 0");
        return 0; // Display only errors
    }

    /**
     * Checks if brand name is used.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isUseBrandName() {
        LOGGER.atFine().log("Retrieved useBrandName: false");
        return false;
    }

    /**
     * Checks if join message is used.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isUseJoinMessage() {
        LOGGER.atFine().log("Retrieved useJoinMessage: false");
        return false;
    }

    /**
     * Checks if boss bar is used.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isUseBossBar() {
        LOGGER.atFine().log("Retrieved useBossBar: false");
        return false;
    }

    /**
     * Checks if title is used.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isUseTitle() {
        LOGGER.atFine().log("Retrieved useTitle: false");
        return false;
    }

    /**
     * Checks if player list is used.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isUsePlayerList() {
        LOGGER.atFine().log("Retrieved usePlayerList: false");
        return false;
    }

    /**
     * Checks if header and footer are used.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isUseHeaderAndFooter() {
        LOGGER.atFine().log("Retrieved useHeaderAndFooter: false");
        return false;
    }

    /**
     * Gets the brand name.
     *
     * @return {@code null}.
     */
    @Override
    public String getBrandName() {
        LOGGER.atFine().log("Retrieved brandName: null");
        return null;
    }

    /**
     * Gets the join message.
     *
     * @return {@code null}.
     */
    @Override
    public String getJoinMessage() {
        LOGGER.atFine().log("Retrieved joinMessage: null");
        return null;
    }

    /**
     * Gets the boss bar.
     *
     * @return {@code null}.
     */
    @Override
    public BossBar getBossBar() {
        LOGGER.atFine().log("Retrieved bossBar: null");
        return null;
    }

    /**
     * Gets the title.
     *
     * @return {@code null}.
     */
    @Override
    public Title getTitle() {
        LOGGER.atFine().log("Retrieved title: null");
        return null;
    }

    /**
     * Gets the player list username.
     *
     * @return Empty string.
     */
    @Override
    public String getPlayerListUsername() {
        LOGGER.atFine().log("Retrieved playerListUsername: empty");
        return "";
    }

    /**
     * Gets the player list header.
     *
     * @return {@code null}.
     */
    @Override
    public String getPlayerListHeader() {
        LOGGER.atFine().log("Retrieved playerListHeader: null");
        return null;
    }

    /**
     * Gets the player list footer.
     *
     * @return {@code null}.
     */
    @Override
    public String getPlayerListFooter() {
        LOGGER.atFine().log("Retrieved playerListFooter: null");
        return null;
    }

    /**
     * Checks if epoll is used.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isUseEpoll() {
        LOGGER.atFine().log("Retrieved useEpoll: false");
        return false;
    }

    /**
     * Gets the boss group size.
     *
     * @return The boss group size (1).
     */
    @Override
    public int getBossGroupSize() {
        LOGGER.atFine().log("Retrieved bossGroupSize: 1");
        return 1; // Default value
    }

    /**
     * Gets the worker group size.
     *
     * @return The worker group size (4).
     */
    @Override
    public int getWorkerGroupSize() {
        LOGGER.atFine().log("Retrieved workerGroupSize: 4");
        return 4; // Default value
    }

    @Override
    public double getInterval() {
        return 0;
    }

    @Override
    public double getMaxPacketRate() {
        return 0;
    }

}