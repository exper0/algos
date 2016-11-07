package org.github.exper0.subsetsum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Abstract base for specific implementations of SumsetSumIterable.
 * @author Andrei Eliseev (aeg.exper0@gmail.com)
 * @param <T> Object type of being summed.
 * @see SubsetSumIterable
 */
public abstract class AbstractSubsetSumIterable<T>
    implements SubsetSumIterable<T> {
    /**
     * List of objects being summed.
     */
    private List<T> data;


//    private int threshold = Integer.MAX_VALUE;

    /**
     * Total sum of all objects.
     */
    private int sum;

    /**
     * Comparator used to order the objects of this iterable.
     */
    private Comparator<T> comparator;

    /**
     * Function that extracts integer value from object.
     */
    private ToIntFunction<T> extractor;


    /**
     * Copy constructor.
     * @param other Instance of SubsetIterable.
     */
    public AbstractSubsetSumIterable(final AbstractSubsetSumIterable<T> other) {
        this.data = other.data;
        this.sum = other.sum;
        this.extractor = other.extractor;
        this.comparator = other.comparator;
    }

    /**
     * Ctor.
     * @param data Collections of objects being summed
     * @param comparator Comparators used to order objects
     * @param extractor Function used to extract int value from given objects
     */
    public AbstractSubsetSumIterable(final Collection<T> data,
                                     final Comparator<T> comparator,
                                     final ToIntFunction<T> extractor) {
        this(new ArrayList<T>(data), comparator, extractor);
    }

    /**
     * Ctor.
     * @param data Collections of objects being summed
     * @param comparator Comparators used to order objects
     * @param extractor Function used to extract int value from given objects
     */
    public AbstractSubsetSumIterable(final List<T> data,
                                     final Comparator<T> comparator,
                                     final ToIntFunction<T> extractor) {
        this.data = data;
        this.comparator = comparator;
        this.extractor = extractor;
        this.sum = data.stream().mapToInt(extractor).sum();
    }

    public final List<T> getData() {
        return data;
    }

    public final int getSum() {
        return sum;
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    public final ToIntFunction<T> getExtractor() {
        return extractor;
    }
}
