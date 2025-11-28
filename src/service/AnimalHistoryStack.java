package service;

import java.util.EmptyStackException;

/**
 * Simple stack ADT for tracking animal browsing history.
 * 
 * Demonstrates ADT implementation: push, pop, peek, isEmpty.
 * Stores recent AnimalGroup browsing order.
 */
public class AnimalHistoryStack<T> {

    private Node<T> top;
    private int size = 0;


    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    /** Push  */
    public void push(T item) {
        Node<T> node = new Node<>(item);
        node.next = top;
        top = node;
        size++;
    }

    /** Pop  */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T item = top.data;
        top = top.next;
        size--;
        return item;
    }

    /** Peek  */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return top.data;
    }

    /** */
    public boolean isEmpty() {
        return top == null;
    }

    /** */
    public int size() {
        return size;
    }

    /***/
    public void clear() {
        top = null;
        size = 0;
    }
    
    /** 
     * 
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        Node<T> current = top;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        return current.data;
    }

}
