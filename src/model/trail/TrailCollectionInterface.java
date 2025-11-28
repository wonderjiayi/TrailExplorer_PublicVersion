package model.trail;

import java.util.List;

/**
 * Generic interface for custom trail collections (List, Heap, HashTable, TreeMap).
 */
public interface TrailCollectionInterface<T> {
    void add(T item);
//    boolean addAItem(T item);
    boolean remove(T item);
    int size();
    void clear();
    List<T> toList();
}