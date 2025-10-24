package com.matvey.cinema.service.impl;

import com.matvey.cinema.exception.FileNotReadyException;
import com.matvey.cinema.exception.LogFileAccessException;
import com.matvey.cinema.exception.TaskNotFoundException;
import com.matvey.cinema.model.entities.LogTask;
import com.matvey.cinema.service.LogService;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    private static final String outputDirectoryPath =
            "/Users/matveyvasiluk/Documents/java/JavaSpring/Cinema/generated_log_files";
    private static final String mainLogFilePathString =
            "/Users/matveyvasiluk/Documents/java/JavaSpring/Cinema/logs/cinema.log";

    private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(0);

    @Override
    public String generateLogFile(String date, String logType) {
        String taskId = String.valueOf(taskIdCounter.incrementAndGet());
        LogTask task = new LogTask(taskId, "PENDING", date);
        tasks.put(taskId, task);

        log.info("Создана задача на генерацию лога {} для даты {} (тип: {})",
                taskId, date, logType != null ? logType : "любой");

        Thread asyncThread = new Thread(() -> {
            LogTask currentTask = tasks.get(taskId);
            if (currentTask == null) {
                log.error("Задача {} не найдена в Map после старта потока.", taskId);
                return;
            }

            currentTask.setStatus("PROCESSING");
            log.info("Задача {} начала обработку (в новом потоке).", taskId);

            try {
                try {
                    log.info("Задача {} сейчас обрабатывается, ждем 10 секунд...", taskId);
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    log.warn("Задержка обработки задачи {} была прервана.", taskId);
                    Thread.currentThread().interrupt();
                }

                log.info("Задача {} завершила задержку, продолжаем с файловыми операциями.",
                        taskId);

                Path mainLogPath = Paths.get(mainLogFilePathString);
                if (!Files.exists(mainLogPath)) {
                    log.error("Основной лог-файл не найден по указанному пути: {}",
                            mainLogFilePathString);
                    currentTask.setStatus("FAILED");
                    currentTask.setErrorMessage("Основной лог-файл не найден: "
                            + mainLogFilePathString);
                    return;
                }

                Path outputDir = Paths.get(outputDirectoryPath);
                if (!Files.exists(outputDir)) {
                    log.info("Выходная директория не найдена. Создаем: {}",
                            outputDir.toAbsolutePath());
                    Files.createDirectories(outputDir);
                }

                String filteredLogs;
                long lineCount;

                try (Stream<String> lines = Files.lines(mainLogPath, StandardCharsets.UTF_8)) {
                    Stream<String> filteredStream = lines.filter(line ->
                            line.trim().startsWith(date));
                    if (logType != null && !logType.isBlank()) {
                        filteredStream = filteredStream.filter(line -> line.contains(logType));
                    }

                    List<String> filteredLinesList = filteredStream.collect(Collectors.toList());
                    lineCount = filteredLinesList.size();
                    log.info("Задача {}: "
                                    + "Найдено {} строк, соответствующих"
                                    + " дате '{}' (начинается с) и "
                                    + "типу '{}'",
                            taskId, lineCount, date, logType != null ? logType : "любой");

                    filteredLogs = String.join(System.lineSeparator(), filteredLinesList);
                }

                if (filteredLogs.isEmpty()) {
                    log.warn("Задача {}: Логи для даты '{}' и типа '{}' не найдены.",
                            taskId, date, logType != null ? logType : "любой");
                    currentTask.setStatus("COMPLETED");
                    currentTask.setErrorMessage("Логи не найдены для указанных критериев.");
                    return;
                }


                String filename = String.format("logs-%s-%s.log", date, taskId);
                Path outputFile = outputDir.resolve(filename);

                Files.write(outputFile, filteredLogs.getBytes(StandardCharsets.UTF_8));

                currentTask.setStatus("COMPLETED");
                currentTask.setFilePath(outputFile.toAbsolutePath().toString());
                log.info("Задача {} успешно завершена. Выходной файл: {}",
                        taskId, currentTask.getFilePath());

            } catch (FileNotFoundException e) {
                log.error("Задача {} завершилась неудачей: {}", taskId, e.getMessage());
                currentTask.setStatus("FAILED");
                currentTask.setErrorMessage(e.getMessage());
            } catch (IOException e) {
                log.error("Задача {} завершилась неудачей во время обработки файла.", taskId, e);
                currentTask.setStatus("FAILED");
                currentTask.setErrorMessage("Ошибка обработки файла: " + e.getMessage());
            } catch (Exception e) {
                log.error("Задача {} завершилась неудачей с неожиданной ошибкой.", taskId, e);
                currentTask.setStatus("FAILED");
                currentTask.setErrorMessage("Неожиданная ошибка: " + e.getMessage());
            }
        });

        asyncThread.start();

        return taskId;
    }

    @Override
    public LogTask getTaskStatus(String taskId) throws TaskNotFoundException {
        log.debug("Попытка получить статус для задачи с ID");
        LogTask task = tasks.get(taskId);
        if (task == null) {
            log.warn("Задача с ID '{}' не найдена в карте.", taskId);
            throw new TaskNotFoundException(taskId);
        }
        log.debug("Найдена задача {} со статусом {}", taskId, task.getStatus());
        return task;
    }

    @Override
    public ResponseEntity<Resource> readLogFile(String taskId) throws TaskNotFoundException {
        LogTask task = getTaskStatus(taskId);
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new FileNotReadyException("Файл не готов для обработки");
        }

        if (task.getFilePath() == null || task.getFilePath().isBlank()) {
            log.error("Задача {} завершена, но путь к файлу отсутствует.", taskId);
            throw new LogFileAccessException("Путь к файлу для завершенной задачи " + taskId + " не указан.");
        }

        try {
            Path filePath = Paths.get(task.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("Предоставление загрузки для задачи {}, файл {}",
                        taskId, filePath.getFileName());
                String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                        .body(resource);
            } else {
                log.error("Файл не найден или недоступен для завершенной задачи {} по пути {}",
                        taskId, task.getFilePath());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (MalformedURLException e) {
            log.error("Ошибка создания URL для пути файла {} для задачи {}",
                    task.getFilePath(), taskId, e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Ошибка подготовки загрузки файла для задачи {}", taskId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public String viewLogsByDate(String date) {
        Path logPath = Paths.get(mainLogFilePathString);
        if (!Files.exists(logPath)) {
            log.warn("Попытка просмотреть логи, но основной лог-файл не найден по пути: {}",
                    mainLogFilePathString);
            throw new RuntimeException("Основной лог-файл не найден: " + mainLogFilePathString);
        }
        try (Stream<String> lines = Files.lines(logPath, StandardCharsets.UTF_8)) {
            String result = lines
                    .filter(line -> line.trim().startsWith(date))
                    .collect(Collectors.joining(System.lineSeparator()));
            return result.isEmpty() ? "Логи не найдены, начинающиеся с даты: " + date : result;
        } catch (IOException e) {
            log.error("Ошибка чтения основного лог-файла по пути {} для просмотра",
                    mainLogFilePathString, e);
            throw new RuntimeException("Ошибка чтения лог-файла", e);
        }
    }
}