package com.anas.cache;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, CacheEntry<V>> cache = new LinkedHashMap<>(16, 0.75f, true);

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final int capacity;

    private synchronized void runCleanup() {
        cleanupExpiredEntries();
    }

    public InMemoryCache(int capacity) {

        if(capacity <= 0){
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        this.capacity = capacity;

        scheduler.scheduleAtFixedRate(
                this::runCleanup,
                1,
                1,
                TimeUnit.SECONDS
        );
    }

    @Override
    public synchronized void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, 0));
        cleanupExpiredEntries();
        evictIfNeeded();
    }

    @Override
    public synchronized void put(K key, V value, long ttlMillis) {
        if (ttlMillis <= 0){
            throw new IllegalArgumentException("TTL must be greater than 0");
        }

        long expiresAt = System.currentTimeMillis() + ttlMillis;

        cache.put(key, new CacheEntry<>(value, expiresAt));
        cleanupExpiredEntries();
        evictIfNeeded();
    }

    private boolean isExpired(CacheEntry<V> entry){
        return entry.getExpiresAt() != 0
                && System.currentTimeMillis() >= entry.getExpiresAt();
    }

    private void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private void evictIfNeeded(){
        if(cache.size() <= capacity){
            return;
        }

        K oldestKey = cache.keySet().iterator().next();

        cache.remove(oldestKey);
    }

    @Override
    public synchronized V get(K key) {
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
    public synchronized void remove(K key) {
        cache.remove(key);
    }

    @Override
    public synchronized boolean containsKey(K key) {
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

    @Override
    public synchronized void close(){
        scheduler.shutdown();
    }

    @Override
    public synchronized int size() {
        return cache.size();
    }
}