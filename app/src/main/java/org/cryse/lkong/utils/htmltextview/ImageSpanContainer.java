package org.cryse.lkong.utils.htmltextview;

import android.graphics.drawable.Drawable;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface ImageSpanContainer {
    void notifyImageSpanLoaded(Object tag, Drawable drawable, AsyncDrawableType type);
}
