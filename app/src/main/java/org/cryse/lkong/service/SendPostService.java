package org.cryse.lkong.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.commons.lang3.StringEscapeUtils;
import org.cryse.lkong.R;
import org.cryse.lkong.application.LKongApplication;
import org.cryse.lkong.event.EditPostDoneEvent;
import org.cryse.lkong.event.NewPostDoneEvent;
import org.cryse.lkong.event.NewThreadDoneEvent;
import org.cryse.lkong.event.PostErrorEvent;
import org.cryse.lkong.event.RxEventBus;
import org.cryse.lkong.logic.request.EditPostRequest;
import org.cryse.lkong.logic.request.NewReplyRequest;
import org.cryse.lkong.logic.request.NewThreadRequest;
import org.cryse.lkong.logic.request.UploadImageRequest;
import org.cryse.lkong.logic.restservice.exception.UploadImageException;
import org.cryse.lkong.model.EditPostResult;
import org.cryse.lkong.model.NewPostResult;
import org.cryse.lkong.model.NewThreadResult;
import org.cryse.lkong.model.UploadImageResult;
import org.cryse.lkong.service.task.EditPostTask;
import org.cryse.lkong.service.task.SendPostTask;
import org.cryse.lkong.service.task.SendTask;
import org.cryse.lkong.service.task.SendThreadTask;
import org.cryse.lkong.utils.ContentProcessor;
import org.cryse.lkong.account.LKAuthObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class SendPostService extends Service {
    private static final String LOG_TAG = SendPostService.class.getName();

    RxEventBus mEventBus = RxEventBus.getInstance();

    BlockingQueue<SendTask> mTaskQueue = new LinkedBlockingQueue<SendTask>();
    SendTask mCurrentTask = null;
    public static final int NOTIFICATION_START_ID = 150;
    public int notification_count = 0;

    NotificationManager mNotifyManager;
    static final int SENDING_NOTIFICATION_ID = 110;

    boolean stopCurrentTask = false;
    Thread mCachingThread;
    boolean mIsStopingService;

    @Override
    public void onCreate() {
        super.onCreate();
        LKongApplication.get(this).sendServiceComponet().inject(this);
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mCachingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!mIsStopingService) {
                        mCurrentTask = mTaskQueue.take();
                        if (mCurrentTask instanceof SendPostTask) {
                            sendPost((SendPostTask) mCurrentTask);
                        } else if (mCurrentTask instanceof SendThreadTask) {
                            sendThread((SendThreadTask) mCurrentTask);
                        } else if (mCurrentTask instanceof EditPostTask) {
                            editPost((EditPostTask) mCurrentTask);
                        }
                        mCurrentTask = null;
                    }
                } catch (InterruptedException ex) {
                    Log.e(LOG_TAG, "Caching thread exception.", ex);
                }
            }
        });
        mCachingThread.start();
    }

    @Override
    public void onDestroy() {
        mIsStopingService = true;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SendPostServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("type")) {
            if ("cancel_current".compareTo(intent.getStringExtra("type")) == 0) {
                stopCurrentTask = true;
            } else if ("cancel_all".compareTo(intent.getStringExtra("type")) == 0) {
                mTaskQueue.clear();
                stopCurrentTask = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void sendPost(SendPostTask task) {
        NotificationCompat.Builder progressNotificationBuilder;

        progressNotificationBuilder = new NotificationCompat.Builder(SendPostService.this);
        progressNotificationBuilder.setContentTitle(getResources().getString(R.string.notification_title_sending_post))
                .setContentText("")
                .setSmallIcon(R.drawable.ic_action_send)
                .setOngoing(true);

        startForeground(SENDING_NOTIFICATION_ID, progressNotificationBuilder.build());
        mNotifyManager.notify(SENDING_NOTIFICATION_ID, progressNotificationBuilder.build());
        NewPostResult postResult = null;
        String replaceResult;
        try {
            replaceResult = preprocessContent(task.getAuthObject(), task.getContent());
            NewReplyRequest request = new NewReplyRequest(task.getAuthObject(), task.getTid(), task.getPid(), replaceResult);
            postResult = request.execute();
            if(postResult != null && postResult.isSuccess()) {
                mEventBus.sendEvent(new NewPostDoneEvent(postResult));
            } else {
                PostErrorEvent errorEvent = new PostErrorEvent();
                if(postResult == null) {
                    errorEvent.setErrorMessage("[NETWORK_ERROR]");
                } else {
                    errorEvent.setErrorMessage(postResult.getErrorMessage());
                }
                mEventBus.sendEvent(errorEvent);
            }
        } catch (Exception e) {
            PostErrorEvent errorEvent = new PostErrorEvent();
            errorEvent.setErrorMessage(e.getMessage());
            mEventBus.sendEvent(errorEvent);
        } finally {
            mNotifyManager.cancel(SENDING_NOTIFICATION_ID);
            stopForeground(true);
            // showSendPostTaskResultNotification(postResult);

        }
    }

    public void sendThread(SendThreadTask task) {
        Log.d(LOG_TAG, "sendThread");
        NotificationCompat.Builder progressNotificationBuilder;

        progressNotificationBuilder = new NotificationCompat.Builder(SendPostService.this);
        progressNotificationBuilder.setContentTitle(getResources().getString(R.string.notification_title_sending_post))
                .setContentText("")
                .setSmallIcon(R.drawable.ic_action_send)
                .setOngoing(true);

        startForeground(SENDING_NOTIFICATION_ID, progressNotificationBuilder.build());
        mNotifyManager.notify(SENDING_NOTIFICATION_ID, progressNotificationBuilder.build());
        NewThreadResult threadResult = null;
        String replaceResult = null;
        try {
            replaceResult = preprocessContent(task.getAuthObject(), task.getContent());
            NewThreadRequest request = new NewThreadRequest(task.getAuthObject(), task.getTitle(), task.getFid(), replaceResult, task.isFollow());
            threadResult = request.execute();
            if(threadResult != null && threadResult.isSuccess()) {
                mEventBus.sendEvent(new NewThreadDoneEvent(threadResult));
            } else {
                PostErrorEvent errorEvent = new PostErrorEvent();
                if(threadResult == null) {
                    errorEvent.setErrorMessage("[NETWORK_ERROR]");
                } else {
                    errorEvent.setErrorMessage(threadResult.getErrorMessage());
                }
                mEventBus.sendEvent(errorEvent);
            }
        } catch (Exception e) {
            PostErrorEvent errorEvent = new PostErrorEvent();
            errorEvent.setErrorMessage(e.getMessage());
            mEventBus.sendEvent(errorEvent);
        } finally {
            mNotifyManager.cancel(SENDING_NOTIFICATION_ID);
            stopForeground(true);
            // showSendThreadTaskResultNotification(threadResult);
        }
    }

    public void editPost(EditPostTask task) {
        Log.d(LOG_TAG, "editPost");
        NotificationCompat.Builder progressNotificationBuilder;

        progressNotificationBuilder = new NotificationCompat.Builder(SendPostService.this);
        progressNotificationBuilder.setContentTitle(getResources().getString(R.string.notification_title_sending_post))
                .setContentText("")
                .setSmallIcon(R.drawable.ic_action_send)
                .setOngoing(true);

        startForeground(SENDING_NOTIFICATION_ID, progressNotificationBuilder.build());
        mNotifyManager.notify(SENDING_NOTIFICATION_ID, progressNotificationBuilder.build());
        EditPostResult editPostResult = null;
        try {
            String replaceResult = preprocessContent(task.getAuthObject(), task.getContent());
            EditPostRequest request = new EditPostRequest(task.getAuthObject(), task.getTid(), task.getPid(), task.getAction(), task.getTitle(), replaceResult);
            editPostResult = request.execute();
            if(editPostResult != null && editPostResult.isSuccess()) {
                mEventBus.sendEvent(new EditPostDoneEvent(editPostResult));
            } else{
                PostErrorEvent errorEvent = new PostErrorEvent();
                if(editPostResult == null) {
                    errorEvent.setErrorMessage("[NETWORK_ERROR]");
                } else {
                    errorEvent.setErrorMessage(editPostResult.getErrorMessage());
                }
                mEventBus.sendEvent(errorEvent);
            }
        } catch (Exception e) {
            PostErrorEvent errorEvent = new PostErrorEvent();
            errorEvent.setErrorMessage(e.getMessage());
            mEventBus.sendEvent(errorEvent);
        } finally {
            mNotifyManager.cancel(SENDING_NOTIFICATION_ID);
            stopForeground(true);
        }
    }

    private String preprocessContent(LKAuthObject authObject, String content) throws Exception{
        String unescapedContent = StringEscapeUtils.unescapeHtml4(content);
        ContentProcessor contentProcessor = new ContentProcessor(this, unescapedContent);
        contentProcessor.setUploadImageCallback((path, mime) -> {
            UploadImageRequest request = new UploadImageRequest(authObject, path, mime);
            UploadImageResult result = request.execute();
            if(result.isSuccess()) {
                return result.getImageUrl();
            } else {
                throw new UploadImageException(result.getErrorMessage());
            }
        });
        contentProcessor.run();
        return contentProcessor.getResultContent();
    }

    private void showSendPostTaskResultNotification(NewPostResult newPostResult) {
        notification_count = notification_count + 1;
        NotificationCompat.Builder mResultBuilder = new NotificationCompat.Builder(this);
        Bundle extras = new Bundle();
        if (newPostResult != null && newPostResult.isSuccess()) {
            extras.putLong("tid", newPostResult.getTid());
            extras.putLong("reply_count", newPostResult.getReplyCount());
            mResultBuilder.setContentTitle(getString(R.string.notification_title_sending_post_successfully))
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_notification_done)
                    .setExtras(extras)
                    .setAutoCancel(true);
        } else {
            mResultBuilder.setContentTitle(getString(R.string.notification_title_sending_post_failed))
                    .setContentText(newPostResult != null ? newPostResult.getErrorMessage() : getString(R.string.notification_content_network_error))
                    .setSmallIcon(R.drawable.ic_notification_error)
                    .setExtras(extras)
                    .setAutoCancel(true);
        }

        mNotifyManager.notify(NOTIFICATION_START_ID + notification_count, mResultBuilder.build());
    }

    private void showSendThreadTaskResultNotification(NewThreadResult newThreadResult) {
        notification_count = notification_count + 1;
        NotificationCompat.Builder mResultBuilder = new NotificationCompat.Builder(this);
        Bundle extras = new Bundle();
        if (newThreadResult != null && newThreadResult.isSuccess()) {
            mResultBuilder.setContentTitle(getString(R.string.notification_title_sending_thread_successfully))
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_notification_done)
                    .setExtras(extras)
                    .setAutoCancel(true);
        } else {
            mResultBuilder.setContentTitle(getString(R.string.notification_title_sending_thread_failed))
                    .setContentText(newThreadResult != null ? newThreadResult.getErrorMessage() : getString(R.string.notification_content_network_error))
                    .setSmallIcon(R.drawable.ic_notification_error)
                    .setExtras(extras)
                    .setAutoCancel(true);
        }

        mNotifyManager.notify(NOTIFICATION_START_ID + notification_count, mResultBuilder.build());
    }

    public class SendPostServiceBinder extends Binder {
        public boolean hasSendingTask() {
            return mCurrentTask != null;
        }

        public void sendPost(LKAuthObject authObject, long tid, Long pid, String content) {
            SendPostTask task = new SendPostTask();
            task.setAuthObject(authObject);
            task.setTid(tid);
            task.setPid(pid);
            task.setContent(content);
            mTaskQueue.add(task);
        }

        public void sendThread(LKAuthObject authObject, String title, long fid, String content, boolean follow) {
            SendThreadTask task = new SendThreadTask();
            task.setAuthObject(authObject);
            task.setTitle(title);
            task.setFid(fid);
            task.setContent(content);
            task.setFollow(follow);
            mTaskQueue.add(task);
        }

        public void editThread(LKAuthObject authObject, long tid, long pid, String title, String content) {
            EditPostTask task = new EditPostTask();
            task.setAuthObject(authObject);
            task.setAction("thread");
            task.setTid(tid);
            task.setPid(pid);
            task.setTitle(title);
            task.setContent(content);
            mTaskQueue.add(task);
        }

        public void editPost(LKAuthObject authObject, long tid, long pid, String content) {
            EditPostTask task = new EditPostTask();
            task.setAuthObject(authObject);
            task.setAction("post");
            task.setTid(tid);
            task.setPid(pid);
            task.setContent(content);
            mTaskQueue.add(task);
        }
    }


}
