package org.cryse.lkong.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class ContentUriPathUtils {

    public static String getRealPathFromUri(Context context, Uri uri){
        Cursor cursor = null;
        try {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = { MediaStore.Images.Media.DATA };

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = context.getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{ id }, null);

            String filePath = "";

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            return filePath;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
