package org.cryse.lkong.modules.base;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface ContentViewEx extends ContentView {
    boolean isLoadingMore();
    void setLoadingMore(boolean value);
}
