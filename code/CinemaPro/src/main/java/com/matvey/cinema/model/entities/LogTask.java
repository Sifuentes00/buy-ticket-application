package com.matvey.cinema.model.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Objects;

public class LogTask {

    private String taskId;
    private String status;
    private String date;
    private String filePath;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public LogTask() {
        this.createdAt = LocalDateTime.now();
    }

    public LogTask(String taskId, String status, String date) {
        this.taskId = taskId;
        this.status = status;
        this.date = date;
        this.createdAt = LocalDateTime.now();
    }

    public String getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogTask logTask = (LogTask) o;
        return Objects.equals(taskId, logTask.taskId)
                && Objects.equals(status, logTask.status)
                && Objects.equals(date, logTask.date)
                && Objects.equals(filePath, logTask.filePath)
                && Objects.equals(errorMessage, logTask.errorMessage)
                && Objects.equals(createdAt, logTask.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, status, date, filePath, errorMessage, createdAt);
    }

    @Override
    public String toString() {
        return "LogTask{"
                + "taskId='" + taskId + '\''
                + ", status='" + status + '\''
                + ", date='" + date + '\''
                + ", filePath='" + filePath + '\''
                + ", errorMessage='" + errorMessage + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}