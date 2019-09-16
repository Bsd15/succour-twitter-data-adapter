package com.stackroute.twitterapiadapter.exceptions;

public class EmptySearchParametersException extends Exception {
    private static final String message = "Empty SearchParameter given.";
    public EmptySearchParametersException() {
        super(message);
    }
}
