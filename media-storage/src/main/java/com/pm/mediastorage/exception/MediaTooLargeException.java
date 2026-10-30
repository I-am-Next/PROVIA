package com.pm.mediastorage.exception;

public class MediaTooLargeException extends RuntimeException {

    public MediaTooLargeException(String message) {
        super(message);
    }
}