package org.cryse.widget.recyclerview;

import android.view.View;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface RecyclerViewOnItemLongClickListener {
    boolean onItemLongClick(View view, int position, long id);
}