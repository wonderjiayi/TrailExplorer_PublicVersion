package model.trail;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

/** Tree-based ordered structure. */
public interface TrailTreeInterface<K extends Comparable<K>, V> extends TrailCollectionInterface<V> {
    void put(K key, V value);
    V get(K key);
    List<V> getRange(K min, K max);
    SortedMap<K, List<V>> headMap(K maxKey);
    SortedMap<K, List<V>> tailMap(K minKey);
	boolean isEmpty();
	Set<K> keySet();
}