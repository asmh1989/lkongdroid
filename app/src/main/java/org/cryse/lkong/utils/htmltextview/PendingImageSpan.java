package org.cryse.lkong.utils.htmltextview;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface PendingImageSpan {
    void loadImage(ImageSpanContainer container);
    void loadImage(ImageSpanContainer container, int newMaxWidth, int backgroundColor);
}
