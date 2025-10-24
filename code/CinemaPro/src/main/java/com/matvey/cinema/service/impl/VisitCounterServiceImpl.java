package com.matvey.cinema.service.impl;

import com.matvey.cinema.service.VisitCounterService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterServiceImpl implements VisitCounterService {

    private static final Logger log = LoggerFactory.getLogger(VisitCounterServiceImpl.class);
    private final Map<String, Integer> visitCounts = new HashMap<>();

    public synchronized void writeVisit(String url) {
        try {
            visitCounts.put(url, visitCounts.getOrDefault(url, 0) + 1);
            log.info("Запись посещения: URL = {}, общее количество = {}",
                    url, visitCounts.get(url));
        } catch (Exception e) {
            log.error("Ошибка при записи посещения для URL {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("Ошибка при записи посещения", e);
        }
    }

    public synchronized int getVisitCount(String url) {
        int count = visitCounts.getOrDefault(url, 0);
        log.info("Получение количества посещений для URL {}: {}", url, count);
        return count;
    }
}