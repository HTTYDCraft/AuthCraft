
package com.httydcraft.authcraft.core.bucket;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.bucket.Bucket;
import com.httydcraft.authcraft.api.bucket.ModifiableBucket;

/**
 * Provides a base implementation for a list-based bucket.
 * This implementation supports modifiable buckets and can operate on streams.
 *
 * @param <T> Type of elements held in this Bucket
 */
public class BaseListBucket<T> implements Bucket<T> {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    private final List<T> list;
    private final ModifiableBucket<T> modifiableBucket;

    // region Constructors

    /**
     * Constructs a BaseListBucket with a specified list and modifiable bucket.
     *
     * @param list The list to back this bucket
     * @param modifiableBucket Modifiable bucket for element modifications
     */
    public BaseListBucket(List<T> list, ModifiableBucket<T> modifiableBucket) {
        Preconditions.checkNotNull(list, "List cannot be null");
        Preconditions.checkNotNull(modifiableBucket, "ModifiableBucket cannot be null");
        this.list = list;
        this.modifiableBucket = modifiableBucket;
        logger.atInfo().log("BaseListBucket instantiated with custom list and modifiable bucket.");
    }

    /**
     * Constructs a BaseListBucket with a specified list.
     *
     * @param list The list to back this bucket
     */
    public BaseListBucket(List<T> list) {
        this(list, new BaseModifiableListBucket<>(list));
    }

    /**
     * Constructs a BaseListBucket with a default empty list.
     */
    public BaseListBucket() {
        this(ImmutableList.of());
    }
    // endregion

    // region Bucket Operations

    /**
     * Returns an unmodifiable view of the raw collection.
     *
     * @return Unmodifiable collection view
     */
    @Override
    public Collection<T> getUnmodifiableRaw() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns a sequential Stream with this bucket as its source.
     *
     * @return Stream of elements
     */
    @Override
    public Stream<T> stream() {
        return list.stream();
    }

    /**
     * Returns the modifiable view of this bucket.
     *
     * @return Modifiable bucket
     */
    @Override
    public ModifiableBucket<T> modifiable() {
        return modifiableBucket;
    }
    // endregion
}
