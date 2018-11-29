package org.cryse.lkong.modules.base;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface BasePresenter<T> {
    void bindView(T view);
    void unbindView();
    void destroy();
}
