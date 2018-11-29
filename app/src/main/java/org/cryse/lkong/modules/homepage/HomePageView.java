package org.cryse.lkong.modules.homepage;

import org.cryse.lkong.model.PunchResult;
import org.cryse.lkong.modules.common.CheckNoticeCountView;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface HomePageView extends CheckNoticeCountView {
    void onPunchUserComplete(PunchResult punchResult);
}
