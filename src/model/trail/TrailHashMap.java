package model.trail;

import java.util.*;

public class TrailHashMap<K, V> implements TrailMapInterface<K, V> {

    /** ---------- Node for separate chaining ---------- */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;
        Node(K k, V v) { key = k; value = v; }
    }

    /** ---------- Core Storage ---------- */
    private Node<K, V>[] table;
    private int size = 0;

    private static final int INIT_CAP = 16;
    private static final double LOAD_FACTOR = 0.75;

    /** ---------- Constructor ---------- */
    @SuppressWarnings("unchecked")
    public TrailHashMap() {
        table = (Node<K, V>[]) new Node[INIT_CAP];
    }

    /** ---------- Hash Function ---------- */
    private int index(K key) {
        return (key == null ? 0 : Math.abs(key.hashCode())) % table.length;
    }

    /** ---------- Resize / Rehash ---------- */
    @SuppressWarnings("unchecked")
    private void rehash() {
        Node<K, V>[] old = table;
        table = (Node<K, V>[]) new Node[old.length * 2];
        size = 0;

        for (Node<K, V> head : old) {
            while (head != null) {
                put(head.key, head.value);
                head = head.next;
            }
        }
    }

    /** ---------- ADT: put ---------- */
    @Override
    public void put(K key, V value) {
        if (size >= table.length * LOAD_FACTOR)
            rehash();

        int idx = index(key);
        Node<K, V> cur = table[idx];

        while (cur != null) {
            if (Objects.equals(cur.key, key)) {
                cur.value = value;
                return;
            }
            cur = cur.next;
        }

        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = table[idx];
        table[idx] = newNode;

        size++;
    }

    /** ---------- ADT: get ---------- */
    @Override
    public V get(K key) {
        int idx = index(key);
        Node<K, V> cur = table[idx];
        while (cur != null) {
            if (Objects.equals(cur.key, key))
                return cur.value;
            cur = cur.next;
        }
        return null;
    }

    /** ---------- containsKey ---------- */
    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** ---------- remove(key) ---------- */
    @Override
    public boolean removeByKey(K key) {
        int idx = index(key);
        Node<K, V> cur = table[idx], prev = null;

        while (cur != null) {
            if (Objects.equals(cur.key, key)) {
                if (prev == null) table[idx] = cur.next;
                else prev.next = cur.next;
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    /** ---------- remove(value) ---------- */
    @Override
    public boolean remove(V item) {
        for (int i = 0; i < table.length; i++) {
            Node<K, V> cur = table[i], prev = null;

            while (cur != null) {
                if (Objects.equals(cur.value, item)) {
                    if (prev == null) table[i] = cur.next;
                    else prev.next = cur.next;

                    size--;
                    return true;
                }
                prev = cur;
                cur = cur.next;
            }
        }
        return false;
    }

    /** ---------- ADT: keySet ---------- */
    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Node<K, V> head : table) {
            while (head != null) {
                keys.add(head.key);
                head = head.next;
            }
        }
        return keys;
    }

    /** ---------- ADT: values ---------- */
    @Override
    public Collection<V> values() {
        List<V> result = new ArrayList<>();
        for (Node<K, V> head : table) {
            while (head != null) {
                result.add(head.value);
                head = head.next;
            }
        }
        return result;
    }

    /** ---------- ADT: toList ---------- */
    @Override
    public List<V> toList() {
        return new ArrayList<>(values());
    }

    /** ---------- size ---------- */
    @Override
    public int size() {
        return size;
    }

    /** ---------- isEmpty ---------- */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** ---------- clear ---------- */
    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        table = (Node<K, V>[]) new Node[INIT_CAP];
        size = 0;
    }

    /** ---------- unsupported add(V) ---------- */
    @Override
    public void add(V item) {
        throw new UnsupportedOperationException("Use put(key, value) instead.");
    }

    /** ---------- remove(String key) special-case ---------- */
    @Override
    public boolean remove(String key) {
        try {
            return removeByKey((K) key);
        } catch (ClassCastException e) {
            return false;
        }
    }

    /** -------- computeIfAbsent -------- */
    public TrailList<Trail> computeIfAbsent(String topicKey, Object defaultValue) {
        TrailList<Trail> list = (TrailList<Trail>) get((K) topicKey);
        if (list == null) {
            list = new TrailList<>();
            put((K) topicKey, (V) list);
        }
        return list;
    }
    
    public V getOrDefault(K key, V defaultValue) {
        V v = get(key);
        return (v != null) ? v : defaultValue;
    }

    public V computeIfAbsent(K key, V defaultValue) {
        V existing = get(key);
        if (existing == null) {
            put(key, defaultValue);
            return defaultValue;
        }
        return existing;
    }
}