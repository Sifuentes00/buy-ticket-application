package com.matvey.cinema.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCache {

    private final int maxSize;
    private final long ttl;
    private final Map<String, CacheValue<Object>> cache;
    private final ScheduledExecutorService scheduler;

    public InMemoryCache() {
        this.maxSize = 100;
        this.ttl = 600000;
        this.cache = new HashMap<>(maxSize);
        this.scheduler = Executors.newScheduledThreadPool(1);
        startCleanupTask();
    }

    public void put(String key, Object value) {
        if (cache.size() >= maxSize) {
            removeEldestEntry();
        }
        cache.put(key, new CacheValue<>(value, System.currentTimeMillis()));
    }

    public Optional<Object> get(String key) {
        CacheValue<Object> cacheValue = cache.get(key);
        if (cacheValue != null && !isExpired(cacheValue)) {
            cacheValue.timestamp = System.currentTimeMillis();
            return Optional.of(cacheValue.value);
        }
        return Optional.empty();
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    private boolean isExpired(CacheValue<Object> cacheValue) {
        return (System.currentTimeMillis() - cacheValue.timestamp) > ttl;
    }

    private void removeEldestEntry() {
        String eldestKey = null;
        long eldestTimestamp = Long.MAX_VALUE;

        for (Map.Entry<String, CacheValue<Object>> entry : cache.entrySet()) {
            if (entry.getValue().timestamp < eldestTimestamp) {
                eldestTimestamp = entry.getValue().timestamp;
                eldestKey = entry.getKey();
            }
        }

        if (eldestKey != null) {
            cache.remove(eldestKey);
        }
    }

    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
            }
        }, ttl, ttl, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    private static class CacheValue<V> {
        V value;
        long timestamp;

        CacheValue(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
