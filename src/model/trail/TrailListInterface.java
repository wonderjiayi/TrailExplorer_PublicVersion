package model.trail;

import java.util.Comparator;

/** Sequential list structure (ArrayList / LinkedList style). */
public interface TrailListInterface<T> extends TrailCollectionInterface<T> {
    void add(T newEntry);
    boolean remove(T anEntry);
    T get(int index);
    boolean contains(T anEntry);
	int size();
	boolean isEmpty();
	void sort(Comparator<T> cmp);
}