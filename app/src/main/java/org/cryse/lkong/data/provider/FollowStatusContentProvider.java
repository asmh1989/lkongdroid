package org.cryse.lkong.data.provider;

import android.content.UriMatcher;

import org.cryse.lkong.data.provider.browsehistory.BrowseHistoryColumns;
import org.cryse.lkong.data.provider.cacheobject.CacheObjectColumns;
import org.cryse.lkong.data.provider.followedforum.FollowedForumColumns;
import org.cryse.lkong.data.provider.followedthread.FollowedThreadColumns;
import org.cryse.lkong.data.provider.followeduser.FollowedUserColumns;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class FollowStatusContentProvider extends LKongContentProvider {
    private static final String TAG = FollowStatusContentProvider.class.getSimpleName();
    private static final String FOLLOW_STATUS_AUTHORITY = "org.cryse.lkong.data.provider.followstatus";
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, CacheObjectColumns.TABLE_NAME, URI_TYPE_CACHE_OBJECT);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, CacheObjectColumns.TABLE_NAME + "/#", URI_TYPE_CACHE_OBJECT_ID);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, FollowedForumColumns.TABLE_NAME, URI_TYPE_FOLLOWED_FORUM);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, FollowedForumColumns.TABLE_NAME + "/#", URI_TYPE_FOLLOWED_FORUM_ID);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, FollowedThreadColumns.TABLE_NAME, URI_TYPE_FOLLOWED_THREAD);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, FollowedThreadColumns.TABLE_NAME + "/#", URI_TYPE_FOLLOWED_THREAD_ID);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, FollowedUserColumns.TABLE_NAME, URI_TYPE_FOLLOWED_USER);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, FollowedUserColumns.TABLE_NAME + "/#", URI_TYPE_FOLLOWED_USER_ID);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, BrowseHistoryColumns.TABLE_NAME, URI_TYPE_BROWSE_HISTORY);
        URI_MATCHER.addURI(FOLLOW_STATUS_AUTHORITY, BrowseHistoryColumns.TABLE_NAME + "/#", URI_TYPE_BROWSE_HISTORY_ID);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected UriMatcher getUriMatcher() {
        return URI_MATCHER;
    }
}
