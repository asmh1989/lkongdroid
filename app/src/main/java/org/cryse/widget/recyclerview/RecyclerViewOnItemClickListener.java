package org.cryse.widget.recyclerview;

import android.view.View;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface RecyclerViewOnItemClickListener {
    void onItemClick(View view, int position, long id);
}