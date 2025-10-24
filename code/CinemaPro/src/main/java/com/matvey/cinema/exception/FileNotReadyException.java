package com.matvey.cinema.exception;

public class FileNotReadyException extends RuntimeException {
    public FileNotReadyException(String message) {
        super(message);
    }
}
