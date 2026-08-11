package io.github.khram0v.gymcrm.exception;

public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }
}
