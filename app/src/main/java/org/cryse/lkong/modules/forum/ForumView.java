package org.cryse.lkong.modules.forum;

import org.cryse.lkong.model.ThreadModel;
import org.cryse.lkong.modules.base.ContentViewEx;

import java.util.List;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface ForumView extends ContentViewEx {
    void showThreadList(List<ThreadModel> threadList, boolean isLoadMore);
    void checkPinnedStatusDone(boolean isPinned);
}
