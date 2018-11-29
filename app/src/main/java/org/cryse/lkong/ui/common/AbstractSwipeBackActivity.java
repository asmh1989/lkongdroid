package org.cryse.lkong.ui.common;

import android.os.Bundle;
import android.view.View;

import org.cryse.lkong.event.AbstractEvent;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public abstract class AbstractSwipeBackActivity extends AbstractActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        return v;
    }

    @Override
    protected void onEvent(AbstractEvent event) {
        super.onEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            closeActivityWithTransition();
        }
    }

    public void closeActivityWithTransition() {
        supportFinishAfterTransition();
    }
}