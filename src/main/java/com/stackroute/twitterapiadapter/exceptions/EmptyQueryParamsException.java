package com.stackroute.twitterapiadapter.exceptions;

/**
 * Thrown when queryParameters list is null or empty
 */
public class EmptyQueryParamsException extends Exception {
    private static final String message = "Empty Query parameters. Please give at least one query param.";

    public EmptyQueryParamsException() {
        super(message);
    }
}
