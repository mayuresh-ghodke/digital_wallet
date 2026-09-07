package com.techpulsetest.digital_wallet.exceptions;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(String msg) {
        super(msg);
    }
}
