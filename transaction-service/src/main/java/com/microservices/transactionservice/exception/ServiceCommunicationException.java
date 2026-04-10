package com.microservices.transactionservice.exception;

public class ServiceCommunicationException extends RuntimeException {

    public ServiceCommunicationException(String message) {
        super(message);
    }
}
