/* BaseModifiableListBucket.java */
/**
 * A modifiable list-based bucket that supports removing and adding elements.
 *
 * @param <T> The type of elements in this bucket
 */
package com.httydcraft.authcraft.core.bucket;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.function.Predicate;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.httydcraft.authcraft.api.bucket.ModifiableBucket;

public class BaseModifiableListBucket<T> implements ModifiableBucket<T> {

    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
    private final List<T> list;

    /**
     * Constructs a modifiable list-based bucket.
     *
     * @param list The list to be modified
     */
    public BaseModifiableListBucket(List<T> list) {
        Preconditions.checkNotNull(list, "List cannot be null");
        this.list = list;
        logger.atInfo().log("BaseModifiableListBucket instantiated");
    }

    @Override
    public Collection<T> getUnmodifiableRaw() {
        return ImmutableList.copyOf(list);
    }

    @Override
    public Stream<T> stream() {
        return list.stream();
    }

    @Override
    public ModifiableBucket<T> modifiable() {
        return this;
    }

    @Override
    public boolean add(T element) {
        Preconditions.checkNotNull(element, "Element cannot be null");
        boolean added = list.add(element);
        logger.atInfo().log("Element added: %s", element);
        return added;
    }

    @Override
    public boolean remove(T element) {
        Preconditions.checkNotNull(element, "Element cannot be null");
        boolean removed = list.remove(element);
        logger.atInfo().log("Element removed: %s", element);
        return removed;
    }

    @Override
    public void removeIf(Predicate<T> predicate) {
        Preconditions.checkNotNull(predicate, "Predicate cannot be null");
        list.removeIf(predicate);
        logger.atInfo().log("Elements removed by predicate: %s", predicate);
    }
}