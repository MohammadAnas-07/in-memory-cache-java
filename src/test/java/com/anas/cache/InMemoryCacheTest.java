package com.anas.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCacheTest {

    @Test
    void shouldStoreAndRetrieveValue() {
        Cache<String, String> cache = new InMemoryCache<>();

        cache.put("user:101", "Anas");

        assertEquals("Anas", cache.get("user:101"));
    }

    @Test
    void shouldReturnNullMissingKey(){
        Cache<String, String> cache = new InMemoryCache<>();

        assertNull(cache.get("user:999"));
    }

    @Test
    void shouldRemoveValue(){
        Cache<String, String> cache = new InMemoryCache<>();

        cache.put("user:101","Anas");
        cache.remove("user:101");

        assertNull(cache.get("user:101"));
    }

    @Test
    void shouldCheckIfKeyExists(){
        Cache<String , String> cache = new InMemoryCache<>();

        cache.put("user:101","Anas");

        assertTrue(cache.containsKey("user:101"));
        assertFalse(cache.containsKey("user:999"));
    }

    @Test
    void shouldExpireValueAfterTtl() throws InterruptedException{
        Cache<String, String> cache = new InMemoryCache<>();

        cache.put("user:101","Anas",200);

        assertEquals("Anas",cache.get("user:101"));

        Thread.sleep(300);

        assertNull(cache.get("user:101"));
    }

    @Test
    void shouldReturnFalseForExpiredKey() throws InterruptedException{
        Cache<String, String> cache = new InMemoryCache<>();

        cache.put("user:101","Anas",200);

        assertTrue(cache.containsKey("user:101"));

        Thread.sleep(300);

        assertFalse(cache.containsKey("user:101"));
    }

    @Test
    void shouldRejectInvalidTtl() {
        Cache<String, String> cache = new InMemoryCache<>();

        assertThrows(
                IllegalArgumentException.class,
                () -> cache.put("user:101","Anas",0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cache.put("user:102","Rahul",-100)
        );
    }
}