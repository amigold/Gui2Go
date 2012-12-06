package com.ami.gui2go;

import android.view.View;
import android.view.View.OnLongClickListener;

public class LongClickListener implements OnLongClickListener
{

	@Override
	public boolean onLongClick(View v)
	{
		v.performClick();
		v.showContextMenu();
		return true;
	}

}
