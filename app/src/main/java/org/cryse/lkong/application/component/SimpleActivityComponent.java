package org.cryse.lkong.application.component;

import org.cryse.lkong.application.modules.ContextModule;
import org.cryse.lkong.application.modules.LKongModule;
import org.cryse.lkong.ui.PhotoViewPagerActivity;
import org.cryse.lkong.ui.SettingsActivity;
import org.cryse.lkong.ui.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) @Singleton
@Component(modules = {ContextModule.class, LKongModule.class})
public interface SimpleActivityComponent {
    void inject(PhotoViewPagerActivity activity);
    void inject(SettingsActivity settingsActivity);
    void inject(SettingsFragment settingsFragment);
}
