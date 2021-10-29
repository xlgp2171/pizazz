package org.pizazz2.test;

import org.pizazz2.IRunnable;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ToolException;

import java.time.Duration;

public class BusinessProcessor implements IRunnable {
    private final boolean hasException;
    private final boolean throwable;

    public BusinessProcessor(boolean hasException, boolean throwable) {
        this.hasException = hasException;
        this.throwable = throwable;
    }

    @Override
    public void activate() throws BaseException {
        if (hasException) {
            throw new ToolException("EXCEPTION", null);
        } else {
            System.out.println("BUSINESS PROCESSING");
        }
    }

    @Override
    public void complete() {
        System.out.println("COMPLETE");
    }

    @Override
    public void throwException(Exception e) {
        System.err.println(e.getMessage());
    }

    @Override
    public boolean throwable() {
        return throwable;
    }

    @Override
    public void destroy(Duration timeout) {
        System.out.println("DESTROY: " + timeout);
    }
}
