package org.cryse.lkong.view;

import org.cryse.lkong.model.ForumModel;

import java.util.List;

public interface ForumListView extends ContentView {
    public void showForumList(List<ForumModel> forumList);
}