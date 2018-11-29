package org.cryse.lkong.modules.base;

import org.cryse.lkong.utils.snackbar.SnackbarSupport;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface ContentView extends SnackbarSupport {
    void setLoading(Boolean value);
    Boolean isLoading();
}
