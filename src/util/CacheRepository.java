package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cache repository for storing morphological analysis results
 * Implements time-based expiration
 */
public class CacheRepository {
    private static CacheRepository instance;
    private final Map<String, CacheEntry> cache;
    private final ScheduledExecutorService scheduler;
    private static final long CACHE_EXPIRATION_TIME = 3600000; // 1 hour
    
    private static class CacheEntry {
        Object value;
        long timestamp;
        
        CacheEntry(Object value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_EXPIRATION_TIME;
        }
    }
    
    private CacheRepository() {
        this.cache = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Schedule periodic cleanup
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 0, 30, TimeUnit.MINUTES);
        
        System.out.println("CacheRepository initialized");
    }
    
    public static synchronized CacheRepository getInstance() {
        if (instance == null) {
            instance = new CacheRepository();
        }
        return instance;
    }
    
    /**
     * Store a value in cache
     */
    public void put(String key, Object value) {
        cache.put(key, new CacheEntry(value));
    }
    
    /**
     * Retrieve a value from cache
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return (T) entry.value;
        } else if (entry != null) {
            // Remove expired entry
            cache.remove(key);
        }
        return null;
    }
    
    /**
     * Check if key exists in cache and is not expired
     */
    public boolean contains(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return true;
        } else if (entry != null) {
            cache.remove(key);
        }
        return false;
    }
    
    /**
     * Remove a specific key from cache
     */
    public void remove(String key) {
        cache.remove(key);
    }
    
    /**
     * Clear all cache entries
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Get cache size
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Cleanup expired entries
     */
    private void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Shutdown the cache repository
     */
    public void shutdown() {
        scheduler.shutdown();
        cache.clear();
    }
}






