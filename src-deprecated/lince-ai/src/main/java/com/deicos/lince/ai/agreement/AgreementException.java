package com.deicos.lince.ai.agreement;

/**
 * com.deicos.lince.ai.agreement
 * Class AgreementException
 * 12/04/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class AgreementException extends Exception {

    private Exception cause;

    public AgreementException() {
        super();
    }

    public AgreementException(Exception e) {
        this.cause = e;
    }

    @Override
    public Exception getCause() {
        return cause;
    }
}
