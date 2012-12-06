package com.ami.gui2go;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class GridViewItemClickListener implements OnItemLongClickListener
{
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v,
			int position, long id)
	{
		LinearLayout lay = (LinearLayout) v;
		String tag = (String) ((TextView) lay.getChildAt(0)).getText();
		String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };

		ClipData.Item item = new ClipData.Item(tag);
		ClipData dragData = new ClipData("GridViewItem", mimeTypes, item);

		View.DragShadowBuilder myShadow = new DragShadowBuilder(v);

		v.startDrag(dragData, myShadow, null, 0);
		return true;
	}
}