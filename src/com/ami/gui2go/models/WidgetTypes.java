package com.ami.gui2go.models;

import java.util.ArrayList;

import com.ami.gui2go.R;

public class WidgetTypes
{
	public static final String BUTTON_TAG = "Button";
	public static final String CHECKBOX_TAG = "CheckBox";
	public static final String TEXTVIEW_TAG = "TextView";
	public static final String EDITTEXT_TAG = "EditText";
	public static final String RADIOBUTTON_TAG = "RadioButton";
	public static final String SPINNER_TAG = "Spinner";
	public static final String TOGGLEBUTTON_TAG = "ToggleButton";
	public static final String LINEAR_TAG = "LinearLayout";
	public static final String RELATIVE_TAG = "RelativeLayout";
	public static final String FRAME_TAG = "FrameLayout";
	public static final String TABLE_TAG = "TableLayout";
	public static final String IMAGEVIEW_TAG = "ImageView";
	public static final String LISTVIEW_TAG = "ListView";
	public static final String GRIDVIEW_TAG = "GridView";
	public static final String RADIOGROUP_TAG = "RadioGroup";
	public static final String TABLEROW_TAG = "TableRow";
	
	public static ArrayList<Widget> widgetsList = new ArrayList<Widget>();
    static {
        widgetsList.add(new Widget(WidgetTypes.BUTTON_TAG, R.drawable.button));
        widgetsList.add(new Widget(WidgetTypes.IMAGEVIEW_TAG,
                        R.drawable.imageview));
        widgetsList.add(new Widget(WidgetTypes.TEXTVIEW_TAG,
                        R.drawable.textview));
        widgetsList.add(new Widget(WidgetTypes.EDITTEXT_TAG,
                        R.drawable.edit_text));
        widgetsList.add(new Widget(WidgetTypes.CHECKBOX_TAG,
                        R.drawable.checkbox));
        widgetsList.add(new Widget(WidgetTypes.RADIOBUTTON_TAG,
                        R.drawable.radio_button));
        widgetsList.add(new Widget(WidgetTypes.SPINNER_TAG, R.drawable.spinner));
        widgetsList.add(new Widget(WidgetTypes.TOGGLEBUTTON_TAG,
                        R.drawable.toggle_button));
        widgetsList.add(new Widget(WidgetTypes.LISTVIEW_TAG,
                        R.drawable.listview));
        widgetsList.add(new Widget(WidgetTypes.GRIDVIEW_TAG,
                        R.drawable.gridview));
        widgetsList.add(new Widget(WidgetTypes.LINEAR_TAG,
                        R.drawable.linear_layout));
        widgetsList.add(new Widget(WidgetTypes.RELATIVE_TAG,
                        R.drawable.relative_layout));
        widgetsList.add(new Widget(WidgetTypes.FRAME_TAG,
                        R.drawable.frame_layout));
        widgetsList.add(new Widget(WidgetTypes.TABLE_TAG,
                        R.drawable.table_layout));
        widgetsList.add(new Widget(WidgetTypes.RADIOGROUP_TAG,
                        R.drawable.radio_group2));
    }
}
