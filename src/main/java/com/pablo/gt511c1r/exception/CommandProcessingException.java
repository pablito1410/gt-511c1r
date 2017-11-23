package com.pablo.gt511c1r.exception;

import com.pablo.gt511c1r.Error;

import java.util.Objects;
import java.util.Optional;

public class CommandProcessingException extends Throwable {

    private final Optional<Error> error;

    /**
     * @param error
     * @throws NullPointerException if {@code error} is null
     */
    public CommandProcessingException(final Error error) {
        super(error.getMessage());
        this.error = Optional.of(Objects.requireNonNull(error));
    }

    public Optional<Error> getError() {
        return error;
    }
}
