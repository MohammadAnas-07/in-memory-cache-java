package com.anas.cache;

public class CacheEntry<V> {

    private final V value;
    private final long expiresAt;

    public CacheEntry(V value, long expiresAt){
        this.value = value;
        this.expiresAt = expiresAt;
    }

    public V getValue(){
        return value;
    }
    public long getExpiresAt(){
        return expiresAt;
    }
}
