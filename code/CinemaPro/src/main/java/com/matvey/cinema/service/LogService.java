package com.matvey.cinema.service;

import com.matvey.cinema.model.entities.LogTask;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface LogService {

    String generateLogFile(String date, String logType);

    LogTask getTaskStatus(String taskId);

    ResponseEntity<Resource> readLogFile(String taskId);

    String viewLogsByDate(String date);
}