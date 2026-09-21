package com.anas.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class InMemoryCacheTest {

    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new InMemoryCache<>(10);
    }

    @AfterEach
    void tearDown(){
        cache.close();
    }

    @Test
    void shouldStoreAndRetrieveValue() {

        cache.put("user:101", "Anas");

        assertEquals("Anas", cache.get("user:101"));
    }

    @Test
    void shouldReturnNullMissingKey(){

        assertNull(cache.get("user:999"));
    }

    @Test
    void shouldRemoveValue(){

        cache.put("user:101", "Anas");
        cache.remove("user:101");

        assertNull(cache.get("user:101"));
    }

    @Test
    void shouldCheckIfKeyExists(){

        cache.put("user:101","Anas");

        assertTrue(cache.containsKey("user:101"));
        assertFalse(cache.containsKey("user:999"));
    }

    @Test
    void shouldExpireValueAfterTtl() throws InterruptedException{

        cache.put("user:101","Anas",200);

        assertEquals("Anas",cache.get("user:101"));

        Thread.sleep(300);

        assertNull(cache.get("user:101"));
    }

    @Test
    void shouldReturnFalseForExpiredKey() throws InterruptedException{

        cache.put("user:101","Anas",200);

        assertTrue(cache.containsKey("user:101"));

        Thread.sleep(300);

        assertFalse(cache.containsKey("user:101"));
    }

    @Test
    void shouldRejectInvalidTtl() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cache.put("user:101","Anas",0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cache.put("user:102","Rahul",-100)
        );
    }

    @Test
    void shouldAutomaticallyRemoveExpiredEntry() throws InterruptedException {

        cache.put("user:101", "Anas", 200);

        Thread.sleep(1500);

        assertFalse(cache.containsKey("user:101"));
    }

    @Test
    void shouldEvictLeastRecentlyUsedEntryWhenCapacityIsFull() {
         cache = new InMemoryCache<>(2);

         cache.put("user:101","Anas");
         cache.put("user:102","Rahul");
         cache.put("user:103","Aman");

         assertFalse(cache.containsKey("user:101"));
         assertTrue(cache.containsKey("user:102"));
         assertTrue(cache.containsKey("user:103"));
    }

    @Test
    void shouldEvictLeastRecentlyUsedEntryAfterAccess() {

        cache = new InMemoryCache<>(2);

        cache.put("user:101","Anas");
        cache.put("user:102","Rahul");

        // Make user:101 recently used
        assertEquals("Anas", cache.get("user:101"));

        //This should evict user:102
        cache.put("user:103","Aman");

        assertTrue(cache.containsKey("user:101"));
        assertFalse(cache.containsKey("user:102"));
        assertTrue(cache.containsKey("user:103"));
    }

    @Test
    void shouldUpdateExistingValueWithoutIncreasingSize() {

        cache = new InMemoryCache<>(2);

        cache.put("user:101", "Anas");
        cache.put("user:102", "Rahul");

        // Update existing key
        cache.put("user:101", "Mohammad Anas");

        // Add third entry
        cache.put("user:103", "Aman");

        // user:102 should be evicted because user:101 was updated recently
        assertTrue(cache.containsKey("user:101"));
        assertFalse(cache.containsKey("user:102"));
        assertTrue(cache.containsKey("user:103"));

        assertEquals("Mohammad Anas", cache.get("user:101"));

    }
}