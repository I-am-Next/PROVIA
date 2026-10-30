package com.pm.mediastorage.exception;

public class MediaAccessDeniedException extends RuntimeException {

    public MediaAccessDeniedException(String message) {
        super(message);
    }
}