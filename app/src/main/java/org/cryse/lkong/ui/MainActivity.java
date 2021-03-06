package org.cryse.lkong.ui;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.BaseDrawerItem;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import org.cryse.lkong.R;
import org.cryse.lkong.account.UserAccount;
import org.cryse.lkong.application.AppPermissions;
import org.cryse.lkong.application.LKongApplication;
import org.cryse.lkong.account.UserAccountManager;
import org.cryse.lkong.event.AbstractEvent;
import org.cryse.lkong.event.AccountRemovedEvent;
import org.cryse.lkong.event.CurrentAccountChangedEvent;
import org.cryse.lkong.event.NewAccountEvent;
import org.cryse.lkong.logic.restservice.exception.NeedSignInException;
import org.cryse.lkong.modules.browsehistory.BrowseHistoryFragment;
import org.cryse.lkong.modules.favorites.FavoritesFragment;
import org.cryse.lkong.modules.homepage.HomePageFragment;
import org.cryse.lkong.sync.SyncUtils;
import org.cryse.lkong.ui.common.AbstractActivity;
import org.cryse.lkong.ui.navigation.AppNavigation;
import org.cryse.lkong.utils.AnalyticsUtils;
import org.cryse.lkong.utils.UpgradeUtils;
import org.cryse.lkong.application.PreferenceConstant;
import org.cryse.utils.preference.Prefs;
import org.cryse.utils.preference.StringPrefs;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import pub.devrel.easypermissions.EasyPermissions;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class MainActivity extends AbstractActivity implements EasyPermissions.PermissionCallbacks {
  private static final String LOG_TAG = MainActivity.class.getName();
  public static AtomicBoolean Running = new AtomicBoolean(false);
  private static final int ID_HOMEPAGE = 1001;
  private static final int ID_FAVORITES = 1003;
  private static final int ID_BROWSE_HISTORY = 1004;
  private static final int ID_FEEDBACK = 1101;
  private static final int ID_FAQ = 1102;
  private static final int ID_SETTINGS = 1103;
  private static final int ID_ADD_ACCOUNT = -3001;
  private static final int ID_MANAGE_ACCOUNT = -3002;
  AppNavigation mNavigation = new AppNavigation();
  @Inject
  UserAccountManager mUserAccountManager;
  StringPrefs mCheckNoticeDuration;

  AccountHeader mAccountHeader;
  Drawer mNaviagtionDrawer;

  UserAccount mCurrentAccount = null;

  int mCurrentSelection = 0;
  boolean mIsRestorePosition = false;
  AtomicBoolean mDoubleBackToExitPressedOnce = new AtomicBoolean(false);
  List<UserAccount> mUserAccountList;
  /**
   * Used to post delay navigation action to improve UX
   */
  private Handler mHandler = new Handler();
  private Runnable mPendingRunnable = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Running.set(true);
    injectThis();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mCheckNoticeDuration = Prefs.getStringPrefs(
        PreferenceConstant.SHARED_PREFERENCE_CHECK_NOTIFICATION_DURATION,
        PreferenceConstant.SHARED_PREFERENCE_CHECK_NOTIFICATION_DURATION_VALUE
    );
    if(!mUserAccountManager.isSignedIn()) {
      mNavigation.navigateToSignInActivity(this, true);
      closeActivityWithTransition();
      return;
    } else {
      UpgradeUtils.showChangelog(this);
    }
    if(savedInstanceState!=null && savedInstanceState.containsKey("selection_item_position")) {
      mCurrentSelection = savedInstanceState.getInt("selection_item_position");
      mIsRestorePosition = true;
    } else {
      mCurrentSelection = ID_HOMEPAGE;
      mIsRestorePosition = false;
    }
    initDrawer();
    checkStoragePermissions();
  }

  private void initDrawer() {
    DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
      @Override
      public void set(ImageView imageView, Uri uri, Drawable placeholder) {
        Glide.with(MainActivity.this).load(uri).placeholder(placeholder).into(imageView);
      }

      @Override
      public void cancel(ImageView imageView) {
        //Glide.clear(imageView);
      }

      @Override
      public Drawable placeholder(Context ctx) {
        return null;
      }

      @Override
      public Drawable placeholder(Context ctx, String tag) {
        return null;
      }
    });

    // Create the AccountHeader
    AccountHeaderBuilder accountHeaderBuilder = new AccountHeaderBuilder()
        .withActivity(this)
        .withTranslucentStatusBar(true)
        .withHeaderBackground(new ColorDrawable(getAccentColor()));
    accountHeaderBuilder
        .withOnAccountHeaderListener((view, iProfile, b) -> {
          if (iProfile.getIdentifier() == ID_ADD_ACCOUNT) {
            mNavigation.navigateToSignInActivity(MainActivity.this, false);
          } else if(iProfile.getIdentifier() == ID_MANAGE_ACCOUNT) {
            mNavigation.navigateToManageAccount(this);
          } else {
            long uid = iProfile.getIdentifier();
            if (mUserAccountManager.getCurrentUserId() == uid) {
              int[] startingLocation = new int[2];
              view.getLocationOnScreen(startingLocation);
              startingLocation[0] += view.getWidth() / 2;
              mNavigation.openActivityForUserProfile(this, startingLocation, uid);
            } else {
              mUserAccountManager.setCurrentUserAccount(uid);
              getEventBus().sendEvent(new CurrentAccountChangedEvent());
            }
          }
          mNaviagtionDrawer.closeDrawer();
          return false;
        })
        .withCurrentProfileHiddenInList(true)
        .withTextColor(Color.WHITE);
    mAccountHeader = accountHeaderBuilder.build();
    IDrawerItem[] drawerItems = new IDrawerItem[7];
    drawerItems[0] = applyColorToDrawerItem(new PrimaryDrawerItem()
        .withName(R.string.drawer_item_homepage)
        .withIcon(R.drawable.ic_drawer_homepage)
        .withIdentifier(ID_HOMEPAGE));
    drawerItems[1] = applyColorToDrawerItem(new PrimaryDrawerItem()
        .withName(R.string.drawer_item_favorites)
        .withIcon(R.drawable.ic_drawer_favorites)
        .withIdentifier(ID_FAVORITES));
    drawerItems[2] = applyColorToDrawerItem(new PrimaryDrawerItem()
        .withName(R.string.drawer_item_browse_history)
        .withIcon(R.drawable.ic_drawer_browse_history)
        .withIdentifier(ID_BROWSE_HISTORY));
    drawerItems[3] = new DividerDrawerItem();
    drawerItems[4] = applyColorToDrawerItem(new SecondaryDrawerItem()
        .withName(R.string.settings_item_feedback_title)
        .withIdentifier(ID_FEEDBACK)
        .withSelectable(false));
    drawerItems[5] = applyColorToDrawerItem(new SecondaryDrawerItem()
        .withName(R.string.drawer_item_faq)
        .withIdentifier(ID_FAQ)
        .withSelectable(false));
    drawerItems[6] = applyColorToDrawerItem(new SecondaryDrawerItem()
        .withName(R.string.drawer_item_settings)
        .withIdentifier(ID_SETTINGS)
        .withSelectable(false));
    //Now create your drawer and pass the AccountHeader.Result
    mNaviagtionDrawer = new DrawerBuilder()
        .withActivity(this)
        .withAccountHeader(mAccountHeader)
        //.withStatusBarColor(getPrimaryDarkColor())
        //.withSliderBackgroundColor(Config.textColorPrimaryInverse(this, mATEKey))
        .addDrawerItems(
            drawerItems
        )
        .withOnDrawerListener(new Drawer.OnDrawerListener() {
          @Override
          public void onDrawerOpened(View view) {

          }

          @Override
          public void onDrawerClosed(View view) {
            supportInvalidateOptionsMenu();
            // If mPendingRunnable is not null, then add to the message queue
            if (mPendingRunnable != null) {
              mHandler.post(mPendingRunnable);
              mPendingRunnable = null;
            }
          }

          @Override
          public void onDrawerSlide(View view, float v) {

          }
        })
        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
          @Override
          public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
            if (iDrawerItem instanceof PrimaryDrawerItem) {
              mCurrentSelection = (int)iDrawerItem.getIdentifier();
            }
            mPendingRunnable = () ->  onNavigationSelected(iDrawerItem);
            return false;
          }
        })
        .build();
    addAccountProfile();
    if(mCurrentSelection == ID_HOMEPAGE && !mIsRestorePosition) {
      mNaviagtionDrawer.setSelection(ID_HOMEPAGE, false);
      navigateToHomePageFragment();
    } else if(mIsRestorePosition) {
      mNaviagtionDrawer.setSelection(mCurrentSelection, false);
    }
  }

  private BaseDrawerItem applyColorToDrawerItem(BaseDrawerItem drawerItem) {
    drawerItem.withIconTintingEnabled(true);
    return drawerItem;
  }

  private ProfileSettingDrawerItem applyColorToDrawerItem(ProfileSettingDrawerItem drawerItem) {
    drawerItem.withIconTinted(true);
    return drawerItem;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    Account account = mUserAccountManager.getCurrentUserAccount().getAccount();
    SyncUtils.manualSync(account, SyncUtils.SYNC_AUTHORITY_CHECK_NOTICE);
    SyncUtils.setPeriodicSync(
        account,
        SyncUtils.SYNC_AUTHORITY_CHECK_NOTICE,
        false,
        Integer.valueOf(mCheckNoticeDuration.get())
    );
  }

  private void onNavigationSelected(IDrawerItem drawerItem) {
    switch ((int)drawerItem.getIdentifier()) {
      case ID_HOMEPAGE:
        navigateToHomePageFragment();
        break;
      case ID_FAVORITES:
        navigateToFavoritesFragment(null);
        break;
      case ID_BROWSE_HISTORY:
        navigateToBrowseHistoryFragment(null);
        break;
      case ID_FEEDBACK:
        mNavigation.openActivityForPostListByThreadId(this, 1153838L);
        break;
      case ID_FAQ:
        AppNavigation.openActivityForFAQ(this);
        break;
      case ID_SETTINGS:
        mNavigation.navigateToSettingsActivity(MainActivity.this);
        break;
      default:
        throw new IllegalArgumentException("Unknown NavigationDrawerItem position.");
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("selection_item_position", mCurrentSelection);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Running.set(false);
  }

  @Override
  protected void injectThis() {
    LKongApplication.get(this).lKongPresenterComponent().inject(this);
  }

  @Override
  protected void analyticsTrackEnter() {
    AnalyticsUtils.trackFragmentActivityEnter(this, LOG_TAG);
  }

  @Override
  protected void analyticsTrackExit() {
    AnalyticsUtils.trackFragmentActivityExit(this, LOG_TAG);
  }

  public void onSectionAttached(String title) {
    setTitle(title);
  }

  @Override
  protected void onEvent(AbstractEvent event) {
    super.onEvent(event);
    if(event instanceof NewAccountEvent) {
      addAccountProfile();
    } else if(event instanceof AccountRemovedEvent) {
      if(!mUserAccountManager.isSignedIn()) {
        mNavigation.navigateToSignInActivity(this, true);
        closeActivityWithTransition();
      } else {
        addAccountProfile();
      }
    }
  }

  private void addAccountProfile() {
    if(mAccountHeader.getProfiles() != null) {
      int profilesCount = mAccountHeader.getProfiles().size();
      for (int i = 0; i < profilesCount; i++) {
        mAccountHeader.removeProfile(0);
      }
    }
    try {
      mCurrentAccount = mUserAccountManager.getCurrentUserAccount();
      mUserAccountList = mUserAccountManager.getUserAccounts();
      for (UserAccount entity : mUserAccountList) {

        ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem()
            .withIcon(entity.getUserAvatar())
            .withName(entity.getUserName())
            .withEmail(entity.getUserEmail())
            .withIdentifier((int) entity.getUserId());
        mAccountHeader.addProfiles(profileDrawerItem);
      }
      ProfileSettingDrawerItem addAccountDrawerItem = new ProfileSettingDrawerItem()
          .withName(getString(R.string.drawer_item_account_add))
          .withIcon(R.drawable.ic_drawer_account_add)
          .withIdentifier(ID_ADD_ACCOUNT)
          .withSelectable(false);
      ProfileSettingDrawerItem accountManageDrawerItem = new ProfileSettingDrawerItem()
          .withName(getString(R.string.drawer_item_manage_account))
          .withIcon(R.drawable.ic_drawer_settings)
          .withIdentifier(ID_MANAGE_ACCOUNT)
          .withSelectable(false);
      mAccountHeader.addProfiles(
          applyColorToDrawerItem(addAccountDrawerItem),
          applyColorToDrawerItem(accountManageDrawerItem)
      );
    } catch (NeedSignInException ex) {
      mNavigation.navigateToSignInActivity(this, true);
      closeActivityWithTransition();
      return;
    }
    getEventBus().sendEvent(new CurrentAccountChangedEvent());
  }

  @Override
  public void onBackPressed() {
    if(mNaviagtionDrawer != null && mNaviagtionDrawer.isDrawerOpen()) {
      mNaviagtionDrawer.closeDrawer();
      return;
    }
    if (!getSupportFragmentManager().popBackStackImmediate()) {
      if (mDoubleBackToExitPressedOnce.get()) {
        super.onBackPressed();
        return;
      } else {
        mDoubleBackToExitPressedOnce.set(true);
        Toast.makeText(this, R.string.toast_double_tap_to_exit, Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(() -> mDoubleBackToExitPressedOnce.set(false), 2000);
      }
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  public void closeActivityWithTransition() {
    finish();
  }

  public Drawer getNavigationDrawer() {
    return mNaviagtionDrawer;
  }

  public boolean popEntireFragmentBackStack() {
    final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
    // Clear Back Stack
    for (int i = 0; i < backStackCount; i++) {
      getSupportFragmentManager().popBackStack();
    }
    return backStackCount > 0;
  }

  public void switchContentFragment(Fragment targetFragment, String backStackTag) {
    popEntireFragmentBackStack();
    FragmentTransaction fragmentTransaction = getSupportFragmentManager()
        .beginTransaction();
    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
        android.R.anim.fade_out);
    if (backStackTag != null) {
      fragmentTransaction.addToBackStack(backStackTag);
    }
    fragmentTransaction.replace(R.id.container, targetFragment);
    fragmentTransaction.commit();
  }

  public void navigateToFavoritesFragment(Bundle args) {
    Fragment fragment = FavoritesFragment.newInstance(args);
    switchContentFragment(fragment, null);
  }

  public void navigateToBrowseHistoryFragment(Bundle args) {
    Fragment fragment = BrowseHistoryFragment.newInstance(args);
    switchContentFragment(fragment, null);
  }

  public void navigateToHomePageFragment() {
    Bundle args = new Bundle();
    Fragment fragment = HomePageFragment.newInstance(args);
    switchContentFragment(fragment, null);
  }
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  private void checkStoragePermissions() {
    if (EasyPermissions.hasPermissions(this, AppPermissions.PERMISSIONS)) {
      // Already have permission, do the thing
      // ...
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(this, getString(R.string.dialog_title_permission_storage),
          AppPermissions.RC_PERMISSION_STORAGE, AppPermissions.PERMISSIONS);
    }
  }

  @Override
  public void onPermissionsGranted(List<String> permissions) {
    if (AppPermissions.PERMISSIONS_SET.containsAll(permissions)) {
    } else {
      finish();
    }
  }

  @Override
  public void onPermissionsDenied(List<String> perms) {
    finish();
  }

  public int getStatusBarColor() {
    if(mNaviagtionDrawer != null && mNaviagtionDrawer.getDrawerLayout() != null) {
      mNaviagtionDrawer.getDrawerLayout().setStatusBarBackgroundColor(getPrimaryDarkColor());
    }
    return ResourcesCompat.getColor(getResources(), R.color.scrim_inset_color, null);
  }


}
