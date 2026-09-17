package com.anas.cache;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache = new HashMap<>();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);

    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }
}