package org.github.exper0.subsetsum;

import com.google.common.collect.Ordering;
import com.luxoft.data.Entry;
import com.luxoft.matcher.matchers.price.Price;
import com.luxoft.matcher.matchers.price.PriceMatcher;
import com.luxoft.matcher.matchers.price.StrictPriceMatcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 *
 * Created by ageliseev on 14.04.2016.
 */
public class Data<T> {
    protected List<T> data;
    protected int threshold = Integer.MAX_VALUE;
    protected int quantity;
    protected Comparator<T> comparator;
    private static final int COL_SIZE = 1024;
    private static final Object marker = new Object();
    private ToIntFunction<T> extractor;


    public Data(List<T> data, Comparator<T> comparator, ToIntFunction<T> extractor) {
        this.data = data;
//        this.roData = Collections.unmodifiableList(this.data);
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
                iterator = getSubSet().iterator();
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
        private Collection<Collection<T>> getSubSet() {
            Collection<Collection<T>> result = new HashSet<>(Data.COL_SIZE);
            HashMap<Integer, Object> map = new HashMap<>(Data.COL_SIZE);
            map.put(0, marker);
            Collection<T> options = new TreeSet<>(Data.this.comparator);
            for (T e : Data.this.data) {
                for (int j = this.target; j > 0; --j) {
                    if (j >= Data.this.extractor.applyAsInt(e) && map.containsKey(j - Data.this.extractor.applyAsInt(e))) {
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
                    List<T> optionList = new ArrayList<>(Data.COL_SIZE);
                    map.put(target, option);

                    int i = target;
                    while (i != 0) {
                        Object o = map.get(i);
                        optionList.add((T)map.get(i));
                        if (o == Data.marker) {
                            i -= 1;
                        } else {
                            i -= Data.this.extractor.applyAsInt((T)o);
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
//            set.removeAll(current);
        }
    }
}
