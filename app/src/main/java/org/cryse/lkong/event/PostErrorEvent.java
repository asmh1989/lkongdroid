package org.cryse.lkong.event;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class PostErrorEvent extends AbstractEvent {
    String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
