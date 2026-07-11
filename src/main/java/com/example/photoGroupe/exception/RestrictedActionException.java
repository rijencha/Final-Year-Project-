package com.example.photoGroupe.exception;

public class RestrictedActionException extends RuntimeException {
    public RestrictedActionException(String message) {
        super(message);
    }
}