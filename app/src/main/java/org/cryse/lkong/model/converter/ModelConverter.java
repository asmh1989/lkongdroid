package org.cryse.lkong.model.converter;

import android.text.TextUtils;

import org.cryse.lkong.logic.restservice.model.LKCheckNoticeCountResult;
import org.cryse.lkong.logic.restservice.model.LKDataItemLocation;
import org.cryse.lkong.logic.restservice.model.LKForumThreadItem;
import org.cryse.lkong.logic.restservice.model.LKForumThreadList;
import org.cryse.lkong.logic.restservice.model.LKNoticeItem;
import org.cryse.lkong.logic.restservice.model.LKNoticeRateItem;
import org.cryse.lkong.logic.restservice.model.LKNoticeRateResult;
import org.cryse.lkong.logic.restservice.model.LKNoticeResult;
import org.cryse.lkong.logic.restservice.model.LKPostItem;
import org.cryse.lkong.logic.restservice.model.LKPostList;
import org.cryse.lkong.logic.restservice.model.LKPostRateItem;
import org.cryse.lkong.logic.restservice.model.LKPostUser;
import org.cryse.lkong.logic.restservice.model.LKThreadInfo;
import org.cryse.lkong.logic.restservice.model.LKTimelineData;
import org.cryse.lkong.logic.restservice.model.LKTimelineItem;
import org.cryse.lkong.logic.restservice.model.LKUserInfo;
import org.cryse.lkong.model.DataItemLocationModel;
import org.cryse.lkong.model.NoticeCountModel;
import org.cryse.lkong.model.NoticeModel;
import org.cryse.lkong.model.NoticeRateModel;
import org.cryse.lkong.model.PostModel;
import org.cryse.lkong.model.ThreadInfoModel;
import org.cryse.lkong.model.ThreadModel;
import org.cryse.lkong.model.TimelineModel;
import org.cryse.lkong.model.UserInfoModel;
import org.cryse.lkong.utils.htmltextview.HtmlCleaner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class ModelConverter {
    public static UserInfoModel toUserInfoModel(LKUserInfo lkUserInfo) {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setThreads(lkUserInfo.getThreads());
        userInfoModel.setBlacklists(lkUserInfo.getBlacklists());
        userInfoModel.setCustomStatus(lkUserInfo.getCustomstatus());
        userInfoModel.setDigestPosts(lkUserInfo.getDigestposts());
        userInfoModel.setEmail(lkUserInfo.getEmail());
        userInfoModel.setFansCount(lkUserInfo.getFansnum());
        userInfoModel.setFollowCount(lkUserInfo.getFollowuidnum());
        userInfoModel.setGender(lkUserInfo.getGender());
        userInfoModel.setPhoneNum(lkUserInfo.getPhonenum());
        userInfoModel.setMe(lkUserInfo.getMe());
        userInfoModel.setPosts(lkUserInfo.getPosts());
        userInfoModel.setUid(lkUserInfo.getUid());
        userInfoModel.setUserName(lkUserInfo.getUsername());
        userInfoModel.setUserIcon(uidToAvatarUrl(lkUserInfo.getUid()));
        userInfoModel.setRegDate(lkUserInfo.getRegdate());
        userInfoModel.setSmartMessage(lkUserInfo.getSmartmessage());
        if(!TextUtils.isEmpty(lkUserInfo.getSightml())) {
            userInfoModel.setSigHtml(HtmlCleaner.htmlToPlain(lkUserInfo.getSightml()));
        }
        userInfoModel.setActivePoints(lkUserInfo.getExtcredits1());
        userInfoModel.setDragonMoney(lkUserInfo.getExtcredits2());
        userInfoModel.setDragonCrystal(lkUserInfo.getExtcredits3());
        userInfoModel.setTotalPunchCount(lkUserInfo.getPunchallday());
        userInfoModel.setLongestContinuousPunch(lkUserInfo.getPunchhighestday());
        userInfoModel.setCurrentContinuousPunch(lkUserInfo.getPunchday());
        userInfoModel.setLastPunchTime(new Date(lkUserInfo.getPunchtime() * 1000));
        return userInfoModel;
    }

    public static List<ThreadModel> toForumThreadModel(LKForumThreadList lkForumThreadList, boolean checkNextTimeSortKey) {
        List<ThreadModel> threadList = new ArrayList<ThreadModel>();
        ThreadModel nextSortKeyItem = null;
        if(lkForumThreadList != null && lkForumThreadList.getData() != null) {
            for(LKForumThreadItem item : lkForumThreadList.getData()) {
                ThreadModel threadModel = new ThreadModel();
                threadModel.setSortKey(item.getSortkey());
                threadModel.setUserName(item.getUsername());
                threadModel.setUserIcon(uidToAvatarUrl(item.getUid()));
                threadModel.setUid(item.getUid());
                threadModel.setClosed(item.getClosed());
                threadModel.setDateline(item.getDateline());
                threadModel.setDigest(item.getDigest() > 0);
                threadModel.setFid(item.getFid());
                threadModel.setId(item.getId());
                threadModel.setReplyCount(item.getReplynum());
                threadModel.setSubject(HtmlCleaner.htmlToPlain(item.getSubject()));
                threadModel.setSortKeyTime(new Date(item.getSortkey() * 1000L));
                if(checkNextTimeSortKey && lkForumThreadList.getNexttime() == item.getSortkey()) {
                    nextSortKeyItem = threadModel;
                } else {
                    threadList.add(threadModel);
                }
            }
        }

        if(checkNextTimeSortKey && nextSortKeyItem != null) {
            Collections.sort(threadList, new ThreadModelCompareBySortKeyTime());
            threadList.add(nextSortKeyItem);
        }
        return threadList;
    }

    public static ThreadInfoModel toThreadInfoModel(LKThreadInfo lkThreadInfo) {
        ThreadInfoModel threadInfo = new ThreadInfoModel();
        threadInfo.setFid(lkThreadInfo.getFid());
        threadInfo.setTid(lkThreadInfo.getTid());
        threadInfo.setSubject(lkThreadInfo.getSubject());
        threadInfo.setViews(lkThreadInfo.getViews());
        threadInfo.setReplies(lkThreadInfo.getReplies());
        threadInfo.setForumName(lkThreadInfo.getForumname());
        threadInfo.setDigest(lkThreadInfo.isDigest());

        threadInfo.setTimeStamp(new Date(lkThreadInfo.getTimestamp()));
        threadInfo.setUid(lkThreadInfo.getUid());
        threadInfo.setUserName(lkThreadInfo.getUsername());
        threadInfo.setAuthorId(lkThreadInfo.getAuthorid());
        threadInfo.setAuthorName(lkThreadInfo.getAuthor());
        threadInfo.setDateline(lkThreadInfo.getDateline());
        threadInfo.setId(lkThreadInfo.getId());
        return threadInfo;
    }


    public static List<PostModel> toPostModelList(LKPostList lkPostList) {
        List<PostModel> itemList = new ArrayList<PostModel>();
        if (lkPostList.getData() != null) {
            for (LKPostItem item : lkPostList.getData()) {
                PostModel postModel = new PostModel();
                postModel.setAdmin(item.getIsadmin() != 0);
                postModel.setAuthorId(item.getAuthorid());
                postModel.setAuthorName(item.getAuthor());
                postModel.setAuthorAvatar(uidToAvatarUrl(item.getAuthorid()));
                postModel.setFavorite(item.isFavorite());
                postModel.setDateline(item.getDateline());
                postModel.setFid(item.getFid());
                postModel.setFirst(item.getFirst() != 0);
                postModel.setId(item.getId());
                postModel.setMe(item.getIsme() != 0);
                postModel.setNotGroup(item.getNotgroup() != 0);
                postModel.setOrdinal(item.getLou());
                postModel.setPid(Long.parseLong(item.getPid()));
                postModel.setSortKey(item.getSortkey());
                postModel.setSortKeyTime(new Date(item.getSortkey() * 1000L));
                postModel.setStatus(item.getStatus());
                postModel.setTid(item.getTid());
                postModel.setTsAdmin(item.isTsadmin());

                if (item.getAlluser() != null) {
                    LKPostUser itemUser = item.getAlluser();
                    PostModel.PostAuthor author = new PostModel.PostAuthor(
                            itemUser.getAdminid(),
                            itemUser.getCustomstatus(),
                            itemUser.getGender(),
                            new Date(itemUser.getRegdate()),
                            itemUser.getUid(),
                            itemUser.getUsername(),
                            itemUser.isVerify(),
                            itemUser.getVerifymessage(),
                            itemUser.getColor(),
                            itemUser.getStars(),
                            itemUser.getRanktitle()
                    );
                    postModel.setAuthor(author);
                } else {
                    postModel.setAuthor(new PostModel.PostAuthor());
                }

                if (item.getRatelog() != null) {
                    int score = 0;
                    List<LKPostRateItem> lkRateLog = item.getRatelog();
                    List<PostModel.PostRate> rateList = new ArrayList<PostModel.PostRate>(lkRateLog.size());
                    for (LKPostRateItem rateItem : lkRateLog) {
                        PostModel.PostRate newRate = toPostRate(rateItem);
                        score = score + rateItem.getScore();
                        rateList.add(newRate);
                    }
                    postModel.setRateScore(score);
                    postModel.setRateLog(rateList);
                } else {
                    postModel.setRateLog(new ArrayList<PostModel.PostRate>());
                }

                Document cleanedHtmlDoc = HtmlCleaner.fixTagBalanceAndRemoveEmpty(
                        item.getMessage(),
                        Whitelist.basicWithImages()
                                .addTags("font")
                                .addAttributes(":all", "style", "color")
                );
                for (Element hyperlink: cleanedHtmlDoc.select("blockquote a")) {
                    if(hyperlink.hasAttr("href")) {
                        String href = hyperlink.attr("href");
                        if(href.contains("pid=") && hyperlink.childNodeSize() > 0 &&
                            "i".equalsIgnoreCase(hyperlink.child(0).tagName())) {
                            hyperlink.child(0).html("\u2191\u2191\u2191\u2191");
                            hyperlink.child(0).unwrap();
                        }
                    }
                }
                postModel.setMessage(cleanedHtmlDoc.html());
                itemList.add(postModel);
            }
        }
        return itemList;
    }

    private static final String SMALL_EMOJI_TEXT = "~bq(\\d+)~";
    private static final String SMALL_EMOJI_IMG = "<img src=\"http://img.lkong.cn/bq/em$1.gif\" class=\"smallbq\">";
    private static final String SIMPLE_EMOJI_TEXT = " [表情] ";

    public static List<TimelineModel> toTimelineModel(LKTimelineData timelineData) {
        List<TimelineModel> timelineModels = new ArrayList<>(timelineData.getData().size());
        for(LKTimelineItem item : timelineData.getData()) {
            TimelineModel model = new TimelineModel();
            model.setId(item.getId());
            model.setQuote(item.isIsquote());
            model.setUserId(Long.valueOf(item.getUid()));
            model.setUserName(item.getUsername());
            model.setDateline(new Date(Long.valueOf(item.getDateline())* 1000L));
            model.setThread(item.isIsthread());
            if(item.isIsthread()) {
                model.setTid(Long.valueOf(item.getId().substring(7)));
                model.setThreadReplyCount(item.getReplynum());
                model.setThreadAuthor(item.getUsername());
                model.setThreadAuthorId(Long.valueOf(item.getUid()));
            } else {
                model.setTid(Long.valueOf(item.getTid()));
                model.setThreadReplyCount(item.getT_replynum());
                model.setThreadAuthor(item.getT_author());
                model.setThreadAuthorId(item.getT_authorid());
            }

            model.setMessage(HtmlCleaner.htmlToPlainReplaceImg(item.getMessage().replaceAll(SMALL_EMOJI_TEXT, SIMPLE_EMOJI_TEXT/*SMALL_EMOJI_IMG*/), SIMPLE_EMOJI_TEXT));
            model.setSubject(item.getSubject());
            model.setSortKey(item.getSortkey());
            model.setSortKeyDate(new Date(item.getSortkey() * 1000L));
            if(item.isIsquote()) {
                TimelineModel.ReplyQuote replyQuote = new TimelineModel.ReplyQuote();
                Document document = Jsoup.parseBodyFragment(item.getMessage());
                Elements targetElements = document.select("div > div > div > a");
                if(targetElements.size() > 0) {
                    if(!TextUtils.isEmpty(targetElements.get(0).html()) && targetElements.get(0).html().length() > 2) {
                        replyQuote.setPosterName(targetElements.get(0).html().substring(1));
                    }
                    Node firstTargetContentSibling = targetElements.get(0).nextSibling();
                    StringBuilder targetContentBuilder = new StringBuilder();
                    if(firstTargetContentSibling != null
                            && !TextUtils.isEmpty(firstTargetContentSibling.outerHtml())
                            && firstTargetContentSibling.outerHtml().length() > 4) {
                        targetContentBuilder.append(firstTargetContentSibling.outerHtml().substring(3));
                        Node nextTargetContentSibling = firstTargetContentSibling.nextSibling();
                        while (nextTargetContentSibling != null) {
                            targetContentBuilder.append(nextTargetContentSibling.outerHtml());
                            nextTargetContentSibling = nextTargetContentSibling.nextSibling();
                        }
                    }
                    replyQuote.setPosterMessage(HtmlCleaner.htmlToPlainReplaceImg(targetContentBuilder.toString().replaceAll(SMALL_EMOJI_TEXT, SIMPLE_EMOJI_TEXT/*SMALL_EMOJI_IMG*/), SIMPLE_EMOJI_TEXT));
                }
                Elements divElements = document.select("div");
                if(divElements.size() > 0) {
                    Element rootDiv = divElements.get(0);
                    StringBuilder messageBuilder = new StringBuilder();
                    Node firstMessageSibling = rootDiv.nextSibling();
                    if(firstMessageSibling != null) {
                        messageBuilder.append(firstMessageSibling.outerHtml());
                        Node nextMessageSibling = firstMessageSibling.nextSibling();
                        while (nextMessageSibling != null) {
                            messageBuilder.append(nextMessageSibling.outerHtml());
                            nextMessageSibling = nextMessageSibling.nextSibling();
                        }
                    }
                    replyQuote.setMessage(HtmlCleaner.htmlToPlainReplaceImg(messageBuilder.toString(), SIMPLE_EMOJI_TEXT));
                }
                model.setReplyQuote(replyQuote);
            }


            timelineModels.add(model);
        }
        return timelineModels;
    }

    public static NoticeCountModel toNoticeCountModel(LKCheckNoticeCountResult result) {
        NoticeCountModel model = new NoticeCountModel();
        model.setUpdateTime(utcLongToLocalDate(result.getTime() * 1000));
        model.setFansNotice(result.getNotice().getFans());
        model.setMentionNotice(result.getNotice().getAtme());
        model.setNotice(result.getNotice().getNotice());
        model.setPrivateMessageNotice(result.getNotice().getPm());
        model.setRateNotice(result.getNotice().getRate());
        model.setSuccess(result.isOk());
        return model;
    }

    public static List<NoticeModel> toNoticeModel(LKNoticeResult result) {
        List<NoticeModel> noticeModelList = new ArrayList<NoticeModel>();
        if(result.getData() != null) {
            for(LKNoticeItem item : result.getData()) {
                NoticeModel model = new NoticeModel();
                model.setDateline(item.getDateline());
                model.setNoticeId(Long.valueOf(item.getId().substring(7)));
                model.setSortKey(item.getSortkey());
                model.setUserId(item.getUid());
                model.setUserName(item.getUsername());

                String[] cleanResult = HtmlCleaner.processNoticeData(
                        item.getNote(),
                        Whitelist.basicWithImages()
                                .addTags("font")
                                .addAttributes(":all", "style", "color")
                );
                model.setNoticeNote(cleanResult[0]);
                if(!TextUtils.isEmpty(cleanResult[1])) {
                    model.setThreadId(Long.valueOf(cleanResult[1]));
                }

                noticeModelList.add(model);
            }
        }

        return noticeModelList;
    }

    public static List<NoticeRateModel> toNoticeRateModel(LKNoticeRateResult result) {
        List<NoticeRateModel> noticeModelList = new ArrayList<NoticeRateModel>();
        if(result.getData() != null) {
            for(LKNoticeRateItem item : result.getData()) {
                NoticeRateModel model = new NoticeRateModel();
                model.setDateline(item.getDateline());
                model.setUserId(item.getUid());
                model.setUserName(item.getUsername());
                model.setExtCredits(item.getExtcredits());
                model.setId(item.getId());
                model.setScore(item.getScore());
                model.setMessage(item.getMessage());
                model.setReason(item.getReason());
                model.setPid(item.getPid());
                model.setSortKey(item.getSortkey());
                noticeModelList.add(model);
            }
        }

        return noticeModelList;
    }

    public static DataItemLocationModel toNoticeRateModel(LKDataItemLocation result) {
        DataItemLocationModel locationModel = new DataItemLocationModel();
        locationModel.setLoad(result.isIsload());
        locationModel.setLocation(result.getLocation());
        locationModel.setOrdinal(result.getLou());
        return locationModel;
    }

    public static PostModel.PostRate toPostRate(LKPostRateItem rateItem) {
        PostModel.PostRate newRate = new PostModel.PostRate(
                rateItem.getDateline(),
                rateItem.getExtcredits(),
                rateItem.getPid(),
                rateItem.getReason(),
                rateItem.getScore(),
                rateItem.getUid(),
                rateItem.getUsername()
        );
        return newRate;
    }

    private static Date utcLongToLocalDate(long utcMillisecond) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.setTimeInMillis(utcMillisecond);
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        return calendar.getTime();
    }

    public static String uidToAvatarUrl(long uid) {
        String uidString = String.format("%1$06d", uid);
        String avatarUrl = String.format("http://img.lkong.cn/avatar/000/%s/%s/%s_avatar_middle.jpg",
                    uidString.substring(0,2),
                    uidString.substring(2,4),
                    uidString.substring(4,6)
            );
        return avatarUrl;
    }

    public static String fidToForumIconUrl(long fid) {
        String fidString = String.format("%1$06d", fid);
        String iconUrl = String.format("http://img.lkong.cn/forumavatar/000/%s/%s/%s_avatar_middle.jpg",
                fidString.substring(0, 2),
                fidString.substring(2, 4),
                fidString.substring(4, 6)
        );
        return iconUrl;
    }

    public static class ThreadModelCompareBySortKeyTime implements Comparator<ThreadModel> {

        @Override public int compare(ThreadModel o1, ThreadModel o2) {
            return o1.getSortKeyTime().compareTo(o2.getSortKeyTime());
        }
    }
}
