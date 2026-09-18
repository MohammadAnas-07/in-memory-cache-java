package com.anas.cache;

public interface Cache<K, V> {

    void put(K key, V value);

    void put(K key, V value, long ttlMillis);

    V get(K key);

    void remove(K key);

    boolean containsKey(K key);

}
