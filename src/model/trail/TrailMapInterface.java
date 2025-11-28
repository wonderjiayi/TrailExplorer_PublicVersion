package model.trail;
import java.util.Collection;
import java.util.Set;
public interface TrailMapInterface<K,V> extends TrailCollectionInterface<V> {
    void put(K k, V v); 
    V get(K k); 
    boolean removeByKey(K k); 
    boolean containsKey(K k); Set<K> keySet();
	boolean remove(String string);
	boolean isEmpty();
	Collection<V> values();
}