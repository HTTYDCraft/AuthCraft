package com.httydcraft.authcraft.core.management;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.LibraryManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.management.LibraryManagement;

import java.util.Collection;
import java.util.List;

// #region Class Documentation
/**
 * Base implementation of {@link LibraryManagement}.
 * Manages loading and configuration of libraries using a {@link LibraryManager}.
 */
public class BaseLibraryManagement implements LibraryManagement {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final String JDA_VERSION = "5.0.0-beta.20";
    public static final Library JDA_LIBRARY = Library.builder()
            .groupId("net{}dv8tion")
            .artifactId("JDA")
            .version(JDA_VERSION)
            .relocate("net{}dv8tion", "com{}httydcraft{}auth{}lib{}net{}dv8tion")
            .relocate("com{}iwebpp", "com{}httydcraft{}auth{}lib{}com{}iwebpp")
            .relocate("org{}apache{}commons", "com{}httydcraft{}auth{}lib{}org{}apache{}commons")
            .relocate("com{}neovisionaries{}ws", "com{}httydcraft{}auth{}lib{}com{}neovisionaries{}ws")
            .relocate("com{}fasterxml{}jackson", "com{}httydcraft{}auth{}lib{}com{}fasterxml{}jackson")
            .relocate("org{}slf4j", "com{}httydcraft{}auth{}lib{}org{}slf4j")
            .relocate("gnu{}trove", "com{}httydcraft{}auth{}gnu{}trove")
            .relocate("okhttp3", "com{}httydcraft{}auth{}lib{}okhttp3")
            .relocate("com{}squareup{}okio", "com{}httydcraft{}auth{}lib{}com{}squareup{}okio")
            .resolveTransitiveDependencies(true)
            .excludeTransitiveDependency("club{}minnced", "opus-java")
            .build();
    private final List<String> customRepositories = Lists.newArrayList();
    private final List<Library> customLibraries = Lists.newArrayList();
    private final LibraryManager libraryManager;
    // #endregion

    // #region Constructor
    /**
     * Constructs a new {@code BaseLibraryManagement}.
     *
     * @param libraryManager The library manager to use. Must not be null.
     */
    public BaseLibraryManagement(LibraryManager libraryManager) {
        this.libraryManager = Preconditions.checkNotNull(libraryManager, "libraryManager must not be null");
        LOGGER.atFine().log("Initialized BaseLibraryManagement");
    }
    // #endregion

    // #region Library Management
    /**
     * Loads all configured libraries and repositories.
     */
    @Override
    public void loadLibraries() {
        customRepositories.forEach(repo -> {
            libraryManager.addRepository(repo);
            LOGGER.atFine().log("Added custom repository: %s", repo);
        });

        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        LOGGER.atFine().log("Added Maven Central and JitPack repositories");

        Collection<Library> libraries = Lists.newArrayList(customLibraries);
        libraries.forEach(library -> {
            libraryManager.loadLibrary(library);
            LOGGER.atFine().log("Loaded library: %s", library.getArtifactId());
        });
    }

    /**
     * Loads a single library.
     *
     * @param library The library to load. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public LibraryManagement loadLibrary(Library library) {
        Preconditions.checkNotNull(library, "library must not be null");
        libraryManager.loadLibrary(library);
        LOGGER.atFine().log("Loaded library: %s", library.getArtifactId());
        return this;
    }

    /**
     * Adds a custom repository.
     *
     * @param repository The repository URL. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public LibraryManagement addCustomRepository(String repository) {
        Preconditions.checkNotNull(repository, "repository must not be null");
        customRepositories.add(repository);
        LOGGER.atFine().log("Added custom repository: %s", repository);
        return this;
    }

    /**
     * Adds a custom library.
     *
     * @param library The library to add. Must not be null.
     * @return This instance for method chaining.
     */
    @Override
    public LibraryManagement addCustomLibrary(Library library) {
        Preconditions.checkNotNull(library, "library must not be null");
        customLibraries.add(library);
        LOGGER.atFine().log("Added custom library: %s", library.getArtifactId());
        return this;
    }

    /**
     * Gets the library manager.
     *
     * @return The {@link LibraryManager}.
     */
    @Override
    public LibraryManager getLibraryManager() {
        LOGGER.atFine().log("Retrieved LibraryManager");
        return libraryManager;
    }
    // #endregion
}