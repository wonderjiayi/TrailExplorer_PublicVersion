package model.trail;

import java.util.*;

public class TrailHeap<T> implements TrailPriorityQueueInterface<T> {

    private ArrayList<T> heap;
    private Comparator<T> comparator;

    public TrailHeap(Comparator<T> cmp) {
        this.comparator = cmp;
        this.heap = new ArrayList<>();
    }

    /** ------------------ Core Heap Helpers ------------------ */

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i) { return 2 * i + 1; }
    private int right(int i) { return 2 * i + 2; }

    private void swap(int i, int j) {
        T tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }

    /** Bubble up (after add) */
    private void siftUp(int i) {
        while (i > 0) {
            int p = parent(i);
            if (comparator.compare(heap.get(i), heap.get(p)) >= 0)
                break;
            swap(i, p);
            i = p;
        }
    }

    /** Bubble down (after pop/remove) */
    private void siftDown(int i) {
        int n = heap.size();

        while (true) {
            int l = left(i), r = right(i);
            int smallest = i;

            if (l < n && comparator.compare(heap.get(l), heap.get(smallest)) < 0)
                smallest = l;

            if (r < n && comparator.compare(heap.get(r), heap.get(smallest)) < 0)
                smallest = r;

            if (smallest == i) break;

            swap(i, smallest);
            i = smallest;
        }
    }

    /** ------------------ ADT Methods ------------------ */

    @Override
    public void add(T item) {
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    @Override
    public T removeTop() {
        if (heap.isEmpty()) throw new NoSuchElementException();

        T top = heap.get(0);
        T last = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }

        return top;
    }

    @Override
    public T peekTop() {
        if (heap.isEmpty()) throw new NoSuchElementException();
        return heap.get(0);
    }

    @Override
    public boolean remove(T item) {
        int idx = heap.indexOf(item);
        if (idx == -1) return false;

        int lastIdx = heap.size() - 1;
        T last = heap.remove(lastIdx);

        if (idx < heap.size()) {
            heap.set(idx, last);
            siftUp(idx);
            siftDown(idx);
        }

        return true;
    }

    @Override
    public void setComparator(Comparator<T> cmp) {
        this.comparator = cmp;

        // Rebuild heap
        ArrayList<T> old = new ArrayList<>(heap);
        heap.clear();
        for (T x : old) add(x);
    }

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public int getCurrentSize() {
        return heap.size();
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override
    public void clear() {
        heap.clear();
    }

    @Override
    public List<T> toList() {
        return new ArrayList<>(heap);
    }

	@Override
	public int getSize() {
		return heap.size();
	}
}