package org.github.exper0.subsetsum;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Implementation of SubsetSumIterable that contains fast subset sum algorithm.
 * Algorithm guarantees that if at least one subset sum exists it will be found.
 * However, algorithm may not find all existing subset sums.
 * @author Andrei Eliseev (aeg.exper0@gmail.com)
 * @param <T> Element type.
 */
public class FastSubsetSumIterable<T> extends AbstractSubsetSumIterable<T> {

    public FastSubsetSumIterable(AbstractSubsetSumIterable<T> other) {
        super(other);
    }

    public FastSubsetSumIterable(final Collection<T> data,
                                 final Comparator<T> comparator,
                                 final ToIntFunction<T> extractor) {
        super(data, comparator, extractor);
    }

    public FastSubsetSumIterable(List<T> data, Comparator<T> comparator, ToIntFunction<T> extractor) {
        super(data, comparator, extractor);
    }

    @Override
    public final Iterator<Collection<T>> iterator() {
        return null;
    }
}
