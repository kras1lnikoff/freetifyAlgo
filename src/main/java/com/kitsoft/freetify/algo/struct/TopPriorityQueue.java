package com.kitsoft.freetify.algo.struct;

import java.util.*;
import java.util.function.Predicate;

public class TopPriorityQueue<E> extends PriorityQueue<E> {

    public static <K, V extends Comparable<? super V>> TopPriorityQueue<Map.Entry<K, V>> fromValues(Map<K, V> map, int maxSize) {
        return fromValues(map, maxSize, null);
    }

    public static <K, V extends Comparable<? super V>> TopPriorityQueue<Map.Entry<K, V>> fromValues(
            Map<K, V> map, int maxSize, Predicate<? super Map.Entry<K, V>> filter) {
        return from(map.entrySet(), Map.Entry.comparingByValue(), maxSize, filter);
    }

    public static <E> TopPriorityQueue<E> from(Collection<? extends E> collection, Comparator<? super E> comparator, int maxSize) {
        return from(collection, comparator, maxSize, null);
    }

    public static <E> TopPriorityQueue<E> from(Collection<? extends E> collection,
                                               Comparator<? super E> comparator, int maxSize, Predicate<? super E> filter) {
        TopPriorityQueue<E> queue = new TopPriorityQueue<>(comparator, maxSize);
        if (filter == null) queue.addAll(collection);
        else for (E e : collection) if (filter.test(e)) queue.add(e);
        return queue;
    }

    public static <K, V extends Comparable<? super V>> List<K> sortKeysByValues(Map<K, V> map, int maxSize) {
        return sortKeysByValues(map, maxSize, null);
    }

    public static <K, V extends Comparable<? super V>> List<K> sortKeysByValues(
            Map<K, V> map, int maxSize, Predicate<? super Map.Entry<K, V>> filter) {
        return onlyKeys(fromValues(map, maxSize).sorted());
    }

    private static <K, V> List<K> onlyKeys(List<Map.Entry<K, V>> entries) {
        List<K> keys = new ArrayList<>();
        for (Map.Entry<K, V> e : entries) keys.add(e.getKey());
        return keys;
    }

    private final int maxSize;

    public TopPriorityQueue(Comparator<? super E> comparator, int maxSize) {
        super(maxSize, comparator);
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public boolean add(E e) {
        if (!isEmpty() && size() == maxSize && comparator().compare(e, peek()) > 0) poll();
        return super.add(e);
    }

    public List<E> sorted() {
        List<E> list = new ArrayList<>(this);
        list.sort(comparator().reversed());
        return list;
    }
}