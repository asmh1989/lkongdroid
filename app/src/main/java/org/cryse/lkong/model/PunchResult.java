package org.cryse.lkong.model;

import java.util.Date;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class PunchResult {
    private Date punchTime;
    private int punchDay;
    private long userId;

    public Date getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(Date punchTime) {
        this.punchTime = punchTime;
    }

    public int getPunchDay() {
        return punchDay;
    }

    public void setPunchDay(int punchDay) {
        this.punchDay = punchDay;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
