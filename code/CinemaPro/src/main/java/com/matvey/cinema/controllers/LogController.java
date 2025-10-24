package com.matvey.cinema.controllers;

import com.matvey.cinema.model.entities.LogTask;
import com.matvey.cinema.service.LogService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateLogFile(
            @RequestParam String date,
            @RequestParam(required = false) String logType) {
        logger.info("Получен запрос на генерацию лог-файла для даты , типа ");
        try {
            String taskId = logService.generateLogFile(date, logType);
            Map<String, String> response = new HashMap<>();
            response.put("taskId", taskId);
            logger.info("Задача генерации лога создана с ID");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            logger.error("Ошибка при инициировании генерации лога для даты", e);
            throw e;
        }
    }

    @GetMapping("/status/{taskId}")
    public ResponseEntity<LogTask> getTaskStatus(@PathVariable String taskId) {
        logger.info("Получен запрос статуса для задачи с ID {}", taskId);

        LogTask task = logService.getTaskStatus(taskId);

        logger.info("Статус для задачи с ID {} успешно получен: {}", taskId, task.getStatus());
        return ResponseEntity.ok(task);
    }

    @GetMapping("/read/{taskId}")
    public ResponseEntity<Resource> downloadLogFile(@PathVariable String taskId) {
        logger.info("Получен запрос на загрузку файла для задачи с ID");
        return logService.readLogFile(taskId);
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewLogs(@RequestParam String date) {
        logger.info("Получен запрос на просмотр логов для даты");
        String logs = logService.viewLogsByDate(date);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(logs);
    }
}