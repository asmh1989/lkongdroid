package org.cryse.lkong.logic.restservice.exception;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class SignInExpiredException extends RuntimeException {
    public SignInExpiredException() {
    }

    public SignInExpiredException(String detailMessage) {
        super(detailMessage);
    }

    public SignInExpiredException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SignInExpiredException(Throwable throwable) {
        super(throwable);
    }
}
