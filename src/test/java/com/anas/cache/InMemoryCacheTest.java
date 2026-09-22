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

    @Test
    void shouldHandleConcurrentWritesSafely() throws InterruptedException {

        cache = new InMemoryCache<>(100);

        int threadCount = 10;

        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {

            int threadId = i;

            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    cache.put(
                            "user:" + threadId + ":" + j,
                            "Anas"
                    );
                }
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(100, cache.size());
    }

    @Test
    void shouldHandleConcurrentReadsAndWritesSafely() throws InterruptedException {

        cache = new InMemoryCache<>(100);

        for(int i = 0; i < 100; i++){
            cache.put("user:" + i, "Anas");
        }

        Thread writer = new Thread(() -> {
            for(int i = 100; i < 200; i++){
                cache.put("user:" + i, "Anas");
            }
        });

        Thread reader = new Thread(() -> {
            for(int i = 0; i < 200; i++){
                cache.get("user:" + i);
            }
        });

        Thread remover = new Thread(() -> {
            for(int i = 0; i < 50; i++){
                cache.remove("user:" + i);
            }
        });

        Thread checker = new Thread(() -> {
            for(int i = 0; i < 200; i++){
                cache.containsKey("user:" + i);
            }
        });

        writer.start();
        reader.start();
        remover.start();
        checker.start();

        writer.join();
        reader.join();
        remover.join();
        checker.join();

        assertTrue(cache.size() <= 100);
    }

    @Test
    void shouldHandleConcurrentCleanupAndCacheOperationsSafely() throws InterruptedException {

        cache = new InMemoryCache<>(100);

        Thread writer = new Thread(() -> {
            for(int i = 0; i < 100; i++){
                cache.put("user:" + i, "Anas", 500);
            }
        });

        Thread reader = new Thread(() -> {
            for(int i = 0; i < 100; i++){
                cache.get("user:" + i);
            }
        });

        Thread checker = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                cache.containsKey("user:" + i);
            }
        });

        writer.start();
        reader.start();
        checker.start();

        writer.join();
        reader.join();
        checker.join();

        Thread.sleep(1500);

        assertTrue(cache.size() <= 100);
    }
}