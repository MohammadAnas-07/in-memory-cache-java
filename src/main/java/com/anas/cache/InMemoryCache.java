package com.anas.cache;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, CacheEntry<V>> cache = new HashMap<>();

    @Override
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, 0));
    }

    @Override
    public void put(K key, V value, long ttlMillis) {
        if (ttlMillis <= 0){
            throw new IllegalArgumentException("TTL must be greater than 0");
        }

        long expiresAt = System.currentTimeMillis() + ttlMillis;

        cache.put(key, new CacheEntry<>(value, expiresAt));
    }

    private boolean isExpired(CacheEntry<V> entry){
        return entry.getExpiresAt() != 0
                && System.currentTimeMillis() >= entry.getExpiresAt();
    }

    @Override
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);

        if(entry == null) {
            return null;
        }


        if(isExpired(entry)){
            cache.remove(key);
            return null;
        }

        return entry.getValue();
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public boolean containsKey(K key) {
        CacheEntry<V> entry = cache.get(key);

        if (entry == null) {
            return false;
        }

        if (isExpired(entry)) {
            cache.remove(key);
            return false;
        }

        return true;
    }
}