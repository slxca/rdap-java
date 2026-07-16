package com.slxca.rdap;

public class RDAPException extends RuntimeException {
    public RDAPException(String message) {
        super(message);
    }

    public RDAPException(String message, Throwable cause) {
        super(message, cause);
    }
}
