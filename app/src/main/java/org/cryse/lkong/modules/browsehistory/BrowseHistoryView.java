package org.cryse.lkong.modules.browsehistory;


import org.cryse.lkong.modules.simplecollection.SimpleCollectionView;
import org.cryse.lkong.modules.common.CheckNoticeCountView;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface BrowseHistoryView<ItemType> extends SimpleCollectionView<ItemType>, CheckNoticeCountView {
    void onClearBrowseHistory();
}
