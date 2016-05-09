package com.tomatecuite.client;

/**
 * Created by Romain on 08/05/2016.
 * Custom Exception for the wrong message in the protocol
 */
class InvalidAnswerException extends Exception{
    private static final long serialVersionUID = 1L;

    InvalidAnswerException(String error) {
        super(error);
    }
}
