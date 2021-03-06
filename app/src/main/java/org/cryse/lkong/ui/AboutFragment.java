package org.cryse.lkong.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.cryse.lkong.R;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class AboutFragment extends PreferenceFragment {
    private static final String LOG_TAG = AboutFragment.class.getSimpleName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_about);
    }
}
