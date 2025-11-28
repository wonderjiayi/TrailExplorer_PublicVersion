package model.trail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TrailList<T> implements TrailListInterface<T> {

    private T[] data;
    private int size;

    private static final int INIT_CAP = 10;

    @SuppressWarnings("unchecked")
    public TrailList() {
        data = (T[]) new Object[INIT_CAP];
        size = 0;
    }

    /** ---------- Ensure Capacity ---------- */
    @SuppressWarnings("unchecked")
    private void ensureCapacity() {
        if (size < data.length) return;

        T[] newArr = (T[]) new Object[data.length * 2];
        System.arraycopy(data, 0, newArr, 0, data.length);
        data = newArr;
    }

    /** ---------- add(newEntry) ---------- */
    @Override
    public void add(T newEntry) {
        ensureCapacity();
        data[size++] = newEntry;
    }

    /** ---------- remove(entry) ---------- */
    @Override
    public boolean remove(T entry) {
        if (entry == null) return false;

        for (int i = 0; i < size; i++) {
            if (data[i].equals(entry)) {
                // shift left
                for (int j = i; j < size - 1; j++) {
                    data[j] = data[j + 1];
                }
                data[size - 1] = null;
                size--;
                return true;
            }
        }
        return false;
    }

    /** ---------- get(index) ---------- */
    @Override
    public T get(int index) {
        if (index < 0 || index >= size) return null;
        return data[index];
    }

    /** ---------- contains(entry) ---------- */
    @Override
    public boolean contains(T entry) {
        for (int i = 0; i < size; i++) {
            if (data[i].equals(entry)) return true;
        }
        return false;
    }

    /** ---------- size() ---------- */
    @Override
    public int size() {
        return size;
    }

    /** ---------- isEmpty() ---------- */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** ---------- clear() ---------- */
    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        data = (T[]) new Object[INIT_CAP];
        size = 0;
    }

    /** ---------- toList() ---------- */
    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) list.add(data[i]);
        return list;
    }

    @Override
    public void sort(Comparator<T> cmp) {
        // simple quicksort on the internal array
        quickSort(0, size - 1, cmp);
    }

    private void quickSort(int left, int right, Comparator<T> cmp) {
        if (left >= right) return;

        int i = left, j = right;
        T pivot = data[(left + right) / 2];

        while (i <= j) {
            while (cmp.compare(data[i], pivot) < 0) i++;
            while (cmp.compare(data[j], pivot) > 0) j--;

            if (i <= j) {
                T tmp = data[i];
                data[i] = data[j];
                data[j] = tmp;
                i++; 
                j--;
            }
        }

        if (left < j) quickSort(left, j, cmp);
        if (i < right) quickSort(i, right, cmp);
    }
}