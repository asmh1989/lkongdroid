package org.cryse.lkong.event;

import org.cryse.lkong.model.NewPostResult;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class NewPostDoneEvent extends AbstractEvent {
    private NewPostResult postResult;

    public NewPostDoneEvent(NewPostResult postResult) {
        this.postResult = postResult;
    }

    public NewPostResult getPostResult() {
        return postResult;
    }

    public void setPostResult(NewPostResult postResult) {
        this.postResult = postResult;
    }
}
