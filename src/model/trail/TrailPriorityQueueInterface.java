package model.trail;

import java.util.Comparator;
import java.util.NoSuchElementException;

/** Priority queue (heap) structure. */
public interface TrailPriorityQueueInterface<T> extends TrailCollectionInterface<T> {
    void add(T newEntry);
    T removeTop() throws NoSuchElementException;
    T peekTop() throws NoSuchElementException;
    void setComparator(Comparator<T> comparator);
	int getSize();
	int getCurrentSize();
	boolean isEmpty();
}