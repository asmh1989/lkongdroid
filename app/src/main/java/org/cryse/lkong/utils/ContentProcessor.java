package org.cryse.lkong.utils;

import android.content.Context;
import android.net.Uri;

import org.cryse.utils.MimeHelper;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class ContentProcessor {
    public static final int IMG_TYPE_LOCAL = 1;
    public static final int IMG_TYPE_URL = 2;
    public static final int IMG_TYPE_EMOJI = 3;
    private Context mContext;
    private String mOriginalContent;
    private String mResultContent;
    private UploadImageCallback mUploadImageCallback;

    public ContentProcessor(Context context, String html) {
        this.mContext = context;
        this.mOriginalContent = html;
    }

    private static final Pattern mImagePattern = Pattern.compile("\\(\\[\\(\\[\\[(\\d)\\]\\[([^\\]]+)\\]\\]\\)\\]\\)");
    private static final Pattern mBookNamePattern = Pattern.compile("《([^》]+)》");
    public void run() throws Exception {
        mResultContent = processImage(mOriginalContent);
        /*mResultContent = processBookName(mResultContent);*/
    }

    private String processImage(String content) throws Exception {
        Matcher matcher = mImagePattern.matcher(content);
        StringBuffer replaceBuffer = new StringBuffer();
        while (matcher.find()) {
            switch(Integer.valueOf(matcher.group(1))) {
                case IMG_TYPE_URL:
                    matcher.appendReplacement(replaceBuffer, matcher.group(2));
                    break;
                case IMG_TYPE_EMOJI:
                    matcher.appendReplacement(replaceBuffer, "http://img.lkong.cn/bq/" + matcher.group(2) + ".gif\"" + " em=\"" + matcher.group(2).substring(2));
                    break;
                case IMG_TYPE_LOCAL:
                    String fileName = matcher.group(2);
                    String mimeType = MimeHelper.getMimeType(mContext, Uri.fromFile(new File(fileName)));
                    String uploadUrl = mUploadImageCallback.uploadImage(fileName, mimeType);
                    matcher.appendReplacement(replaceBuffer, uploadUrl);
                    break;
                default:
                    break;
            }
        }
        matcher.appendTail(replaceBuffer);
        return replaceBuffer.toString();
    }

    public void setUploadImageCallback(UploadImageCallback uploadImageCallback) {
        this.mUploadImageCallback = uploadImageCallback;
    }

    public String getResultContent() {
        return mResultContent;
    }

    public interface UploadImageCallback {
        String uploadImage(String path, String mimeType) throws Exception;
    }
}
