package org.github.exper0.subsetsum;

import com.google.common.collect.LinkedHashMultiset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.function.ToIntFunction;

/**
 * Iterable that provides ability to iterate among all subset sums using arbitrary object type with integer property.
 * @param <T> Object type.
 * @author exper0 (aeg.exper0@gmail.com)
 * @see <a href="https://en.wikipedia.org/wiki/Subset_sum_problem">https://en.wikipedia.org/wiki/Subset_sum_problem</a>
 */
public class SSIterable<T> implements Iterable<T> {
    protected List<T> data;
    protected int threshold = Integer.MAX_VALUE;
    protected int quantity;
    protected Comparator<T> comparator;
    private static final int COL_SIZE = 1024;
    private static final Object marker = new Object();
    private ToIntFunction<T> extractor;


    public SSIterable(List<T> data, Comparator<T> comparator, ToIntFunction<T> extractor) {
        this.data = data;
        this.comparator = comparator;
        this.extractor = extractor;
        this.quantity = data.stream().mapToInt(extractor).sum();
        Collections.sort(this.data, comparator);
    }

    public Comparator<T> comparator() {
        return comparator;
    }

    public int quantity() {
        return quantity;
    }

    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int threshold() {
        return threshold;
    }

    public boolean remove(T entry) {
        if (this.data.remove(entry)) {
            this.quantity -= this.extractor.applyAsInt(entry);
            return true;
        }
        return false;
    }

    public void removeAll(Collection<T> entry) {
        for (T e : entry) {
            remove(e);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return this.data.iterator();
    }

    public Iterator<Collection<T>> fastSSIterator(final int target) {
        return new FastSSIterator(target);
    }

    private class FastSSIterator implements Iterator<Collection<T>> {
        private Iterator<Collection<T>> iterator;
        private final int target;
        private Collection<T> current;

        public FastSSIterator(int target) {
            this.target = target;
        }

        @Override
        public boolean hasNext() {
            if (iterator == null) {
                iterator = find().iterator();
            }
            return iterator.hasNext();
        }

        @Override
        public Collection<T> next() {
            if (hasNext()) {
                current = iterator.next();
                return current;
            }
            throw new NoSuchElementException();
        }

        @SuppressWarnings("unchecked")
        private Collection<Collection<T>> find() {
            Collection<Collection<T>> result = new HashSet<>(SSIterable.COL_SIZE);
            HashMap<Integer, Object> map = new HashMap<>(SSIterable.COL_SIZE);
            map.put(0, marker);
            Collection<T> options = new TreeSet<>(SSIterable.this.comparator);
            for (T e : SSIterable.this.data) {
                for (int j = this.target; j > 0; --j) {
                    if (j >= SSIterable.this.extractor.applyAsInt(e) && map.containsKey(j - SSIterable.this.extractor.applyAsInt(e))) {
                        if (j == target) {
                            options.add(e);
                        }
                        if (!map.containsKey(j)) {
                            map.put(j, e);
                        }
                    }
                }
            }
            if (map.containsKey(target)) {
                for (T option : options) {
                    List<T> optionList = new ArrayList<>(SSIterable.COL_SIZE);
                    map.put(target, option);

                    int i = target;
                    while (i != 0) {
                        Object o = map.get(i);
                        optionList.add((T) map.get(i));
                        if (o == SSIterable.marker) {
                            i -= 1;
                        } else {
                            i -= SSIterable.this.extractor.applyAsInt((T) o);
                        }

                    }
                    result.add(optionList);
                }
            }
            return result;
        }

        @Override
        public void remove() {
            if (iterator == null) {
                throw new IllegalStateException("next should be called at least once");
            }
            iterator.remove();
            SSIterable.this.removeAll(current);
        }
    }

    public class CombinationIterator implements Iterator<Collection<T>> {
        private Collection<T> combination;
        private int outer;
        private Collection<T> workItems;
        private Deque<StackRecord> stack;
        private Collection<T> cached;
        private Collection<T> current;

        public CombinationIterator() {
            this.stack = new ArrayDeque<>(SSIterable.COL_SIZE);
            this.workItems = LinkedHashMultiset.create(SSIterable.this.data);
            this.outer = SSIterable.this.data.size();
        }

        @Override
        public boolean hasNext() {
            if (this.cached == null) {
                this.cached = doGetNext();
            }
            return this.cached != null;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException("next should be called at least once");
            }
            workItems.removeAll(current);
            SSIterable.this.removeAll(current);
        }

        @Override
        public Collection<T> next() {
            if (cached != null) {
                this.current = this.cached;
                this.cached = null;
            } else {
                Collection<T> result = doGetNext();
                if (result == null) {
                    throw new NoSuchElementException();
                }
                this.current = result;
            }
            return this.current;
        }

        private Collection<T> doGetNext() {
            for (; outer > 0; --outer) {
                if (SSIterable.this.data.size() == outer) {
                    --outer;
                    this.combination = new ArrayList<>(SSIterable.this.data);
                    return new ArrayList<>(SSIterable.this.data);
                } else if (SSIterable.this.data.size() < this.outer) {
                    continue;
                }
                if (stack.isEmpty()) {
                    stack.push(new StackRecord(0, outer, 0, null, null));
                    this.combination = LinkedHashMultiset.create(SSIterable.COL_SIZE);
                }
                Collection<T> result = combination();
                if (result != null) {
                    this.combination = result;
                    return new ArrayList<T>(result);
                }
            }
            return null;
        }

        private Collection<T> combination() {
            do {
                StackRecord r = stack.pop();
                int i = r.i;
                T previous = r.previous;
                long aggregate = r.aggregate;
                int currentDepth = r.currentDepth;
                if (currentDepth > 0) {
                    if (r.originalItem != null) {
                        combination.remove(r.originalItem);
                    }
                    for (; i < SSIterable.this.data.size(); ++i) {
                        T originalItem = SSIterable.this.data.get(i);
                        if (previous != null && SSIterable.this.comparator.compare(originalItem, previous) == 0) {
                            continue;
                        }
                        if (aggregate > SSIterable.this.threshold ||
                            !workItems.containsAll(combination)) {
                            break;
                        }
                        combination.add(originalItem);
                        previous = originalItem;
                        stack.push(new StackRecord(i + 1, currentDepth, aggregate, previous, originalItem));
                        stack.push(
                            new StackRecord(
                                i + 1, currentDepth - 1, aggregate + SSIterable.this.extractor.applyAsInt(originalItem), null, null
                            )
                        );
                        break;
                    }
                } else {
                    return combination;
                }
            } while (stack.size() > 0);
            return null;
        }

        private class StackRecord {
            int i;
            int currentDepth;
            long aggregate;
            T previous;
            T originalItem;

            StackRecord(int i, int currentDepth, long aggregatedQuantity, T previous, T originalItem) {
                this.i = i;
                this.currentDepth = currentDepth;
                this.aggregate = aggregatedQuantity;
                this.previous = previous;
                this.originalItem = originalItem;
            }
        }
   }
}
