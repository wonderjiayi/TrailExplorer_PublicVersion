package model.trail;

import java.util.*;

public class TrailTreeMap<K extends Comparable<K>, V> implements TrailTreeInterface<K, V> {

    private static class Node<K, V> {
        K key;
        List<V> values;
        Node<K, V> left, right;

        Node(K key, V value) {
            this.key = key;
            this.values = new ArrayList<>();
            this.values.add(value);
        }
    }

    private Node<K,V> root;
    private int size = 0;

    /** Insert key-value */
    @Override
    public void put(K key, V value) {
        root = insert(root, key, value);
    }

    private Node<K,V> insert(Node<K,V> node, K key, V value) {
        if (node == null) {
            size++;
            return new Node<>(key, value);
        }

        int cmp = key.compareTo(node.key);

        if (cmp < 0) node.left = insert(node.left, key, value);
        else if (cmp > 0) node.right = insert(node.right, key, value);
        else node.values.add(value);

        return node;
    }

    /** Get FIRST matching value */
    @Override
    public V get(K key) {
        Node<K,V> node = root;

        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp == 0) 
                return node.values.get(0);
            else if (cmp < 0) 
                node = node.left;
            else 
                node = node.right;
        }

        return null;
    }

    /** Get all V whose key is in [min, max] */
    @Override
    public List<V> getRange(K min, K max) {
        List<V> list = new ArrayList<>();
        dfsRange(root, min, max, list);
        return list;
    }

    private void dfsRange(Node<K,V> node, K min, K max, List<V> out) {
        if (node == null) return;

        if (min.compareTo(node.key) <= 0)
            dfsRange(node.left, min, max, out);

        if (min.compareTo(node.key) <= 0 && max.compareTo(node.key) >= 0)
            out.addAll(node.values);

        if (max.compareTo(node.key) >= 0)
            dfsRange(node.right, min, max, out);
    }

    @Override
    public SortedMap<K, List<V>> headMap(K max) {
        TreeMap<K,List<V>> map = new TreeMap<>();
        fillHeadMap(root, max, map);
        return map;
    }

    private void fillHeadMap(Node<K,V> node, K max, TreeMap<K,List<V>> map) {
        if (node == null) return;

        int cmp = node.key.compareTo(max);

        if (cmp <= 0)
            map.put(node.key, new ArrayList<>(node.values));

        fillHeadMap(node.left, max, map);
        if (cmp <= 0) fillHeadMap(node.right, max, map);
    }

    @Override
    public SortedMap<K, List<V>> tailMap(K min) {
        TreeMap<K,List<V>> map = new TreeMap<>();
        fillTailMap(root, min, map);
        return map;
    }

    private void fillTailMap(Node<K,V> node, K min, TreeMap<K,List<V>> map) {
        if (node == null) return;

        int cmp = node.key.compareTo(min);

        if (cmp >= 0)
            map.put(node.key, new ArrayList<>(node.values));

        if (cmp >= 0) fillTailMap(node.left, min, map);
        fillTailMap(node.right, min, map);
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new TreeSet<>();
        fillKeys(root, set);
        return set;
    }

    private void fillKeys(Node<K,V> n, Set<K> out) {
        if (n == null) return;
        fillKeys(n.left, out);
        out.add(n.key);
        fillKeys(n.right, out);
    }

    @Override
    public void add(V item) {
        throw new UnsupportedOperationException("Use put(key,value) instead.");
    }

    @Override
    public boolean remove(V item) {
        throw new UnsupportedOperationException("Not required for BST implementation.");
    }

    @Override
    public int size() { return size; }

    @Override
    public boolean isEmpty() { return root == null; }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public List<V> toList() {
        List<V> out = new ArrayList<>();
        collect(root, out);
        return out;
    }

    private void collect(Node<K,V> n, List<V> out) {
        if (n == null) return;
        collect(n.left, out);
        out.addAll(n.values);
        collect(n.right, out);
    }
}