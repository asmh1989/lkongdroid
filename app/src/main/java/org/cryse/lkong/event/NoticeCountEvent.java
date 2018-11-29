package org.cryse.lkong.event;

import org.cryse.lkong.model.NoticeCountModel;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class NoticeCountEvent extends AbstractEvent{
    private NoticeCountModel noticeCount;

    public NoticeCountModel getNoticeCount() {
        return noticeCount;
    }

    public void setNoticeCount(NoticeCountModel noticeCount) {
        this.noticeCount = noticeCount;
    }

    public NoticeCountEvent(NoticeCountModel noticeCount) {
        this.noticeCount = noticeCount;
    }
}
