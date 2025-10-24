package com.matvey.cinema.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String taskId) {
        super("Task with ID '" + taskId + "' not found.");
    }
}