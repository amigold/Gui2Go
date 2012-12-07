package com.ami.gui2go.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlSerializer;

import com.ami.gui2go.EditorActivity;

import android.os.Environment;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LayoutXMLCreator
{
	private static XmlSerializer serializer = Xml.newSerializer();
	private static File newxmlfile;
	private static FileOutputStream fileos;

	public LayoutXMLCreator(String filename, String projectName)
	{
		String path = Environment.getExternalStorageDirectory()
				+ "/Gui2Go/Projects/" + projectName + "/";
		newxmlfile = new File(path);
		try {
			newxmlfile.mkdirs();
			path += filename + ".xml";
			newxmlfile = new File(path);
			newxmlfile.createNewFile();
		} catch (Exception e) {
//			Log.e("IOException", "exception in createNewFile() method");
		}
		// we have to bind the new file with a FileOutputStream
		fileos = null;
		try {
			fileos = new FileOutputStream(newxmlfile);
		} catch (FileNotFoundException e) {
//			Log.e("FileNotFoundException", "can't create FileOutputStream");
		}

		try {
			serializer.setOutput(fileos, "utf-8");
			serializer.startDocument(null, null);
			serializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true);

		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}
	}

	public void AddWidgetNode(View v, EditorActivity act)
	{
		String widgetType = v.getClass().getSimpleName();
		String widgetID = v.getTag().toString();
		LayoutParams params = v.getLayoutParams();
		// int grav;
		try {
			serializer.startTag(null, widgetType);
			serializer.attribute(null, "android:id", "@+id/" + widgetID);

			AddLayoutParams(params, false);

			if (v.getParent().getClass() == RelativeLayout.class)
			// handle relative layout as parent
			{
				int[] rules = ((RelativeLayout.LayoutParams) params).getRules();
				AddRelativeLayoutTags(rules, v);
			}

			if (v.getParent().getClass() == LinearLayout.class)
			// handle relative layout as parent
			{
				// grav = ((android.widget.LinearLayout.LayoutParams)
				// params).gravity;
			}

			if (v.getClass() == TextView.class // handle widgets with text attr
					// TODO remember to handle AutoCompleteTextView and other
					// TODO stuff that might inherit from textview
					|| v.getClass() == EditText.class
					|| v.getClass() == Button.class
					|| v.getClass() == RadioButton.class
					|| v.getClass() == CheckBox.class) {
				serializer.attribute(null, "android:text", ((TextView) v)
						.getText().toString());

				AddGravityValue((TextView) v); // only widgets with text seem to
												// have gravity
			}
			if (v instanceof ImageView) {
				if(act.imageDictionary.size()>0){
					if(act.imageDictionary.containsKey(v.getId())){
						serializer.attribute(null, "android:src", "@drawable/"
								+ act.imageDictionary.get(v.getId()).split("\\.")[0]);						
					}						
				}else{
					
				}
				
			}

			serializer.endTag(null, widgetType);
		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}
	}

	public void endDocument()
	{
		try {
			serializer.endDocument();
			// write xml data into the FileOutputStream
			serializer.flush();
			// finally we close the file stream
			fileos.close();
		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}
	}

	public void AddRelativeLayoutTags(int[] rules, View v)
	{
		try {
			if (rules[0] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[0]).getTag().toString();
				serializer.attribute(null, "android:layout_toLeftOf", "@id/"
						+ target);
			}
			if (rules[1] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[1]).getTag().toString();
				serializer.attribute(null, "android:layout_toRightOf", "@id/"
						+ target);
			}
			if (rules[2] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[2]).getTag().toString();
				serializer.attribute(null, "android:layout_above", "@id/"
						+ target);
			}
			if (rules[3] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[3]).getTag().toString();
				serializer.attribute(null, "android:layout_below", "@id/"
						+ target);
			}
			if (rules[4] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[4]).getTag().toString();
				serializer.attribute(null, "android:layout_alignBaseline",
						"@id/" + target);
			}
			if (rules[5] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[5]).getTag().toString();
				serializer.attribute(null, "android:layout_alignLeft", "@id/"
						+ target);
			}
			if (rules[6] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[6]).getTag().toString();
				serializer.attribute(null, "android:layout_alignTop", "@id/"
						+ target);
			}
			if (rules[7] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[7]).getTag().toString();
				serializer.attribute(null, "android:layout_alignRight", "@id/"
						+ target);
			}
			if (rules[8] != 0) {
				String target = ((ViewGroup) v.getParent())
						.findViewById(rules[8]).getTag().toString();
				serializer.attribute(null, "android:layout_alignBottom", "@id/"
						+ target);
			}
			if (rules[9] == -1) {
				serializer.attribute(null, "android:layout_alignParentLeft",
						"true");
			}
			if (rules[10] == -1) {
				serializer.attribute(null, "android:layout_alignParentTop",
						"true");
			}
			if (rules[11] == -1) {
				serializer.attribute(null, "android:layout_alignParentRight",
						"true");
			}
			if (rules[12] == -1) {
				serializer.attribute(null, "android:layout_alignParentBottom",
						"true");
			}
			if (rules[13] != 0) {
				serializer.attribute(null, "android:layout_centerInParent",
						"true");
			}
			if (rules[14] != 0) {
				serializer.attribute(null, "android:layout_centerHorizontal",
						"true");
			}
			if (rules[15] != 0) {
				serializer.attribute(null, "android:layout_centerVertical",
						"true");
			}
		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}
	}

	public void AddGravityValue(TextView v)
	{
		int grav = v.getGravity();

		try {
			if (grav == Gravity.CENTER) {
				serializer.attribute(null, "android:gravity", "center");
			} else if (grav == Gravity.BOTTOM) {
				serializer.attribute(null, "android:gravity", "bottom");
			} else if (grav == Gravity.CENTER_HORIZONTAL) {
				serializer.attribute(null, "android:gravity",
						"center_horizontal");
			} else if (grav == Gravity.CENTER_VERTICAL) {
				serializer
						.attribute(null, "android:gravity", "center_vertical");
			} else if (grav == Gravity.LEFT) {
				serializer.attribute(null, "android:gravity", "left");
			} else if (grav == Gravity.RIGHT) {
				serializer.attribute(null, "android:gravity", "right");
			} else if (grav == Gravity.TOP) {
				serializer.attribute(null, "android:gravity", "top");
			}
		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}
	}

	public void AddLayoutParams(LayoutParams params, boolean isRootLayout)
	{
		int height = params.height;
		int width = params.width;

		try {
			if (height == -2) {
				serializer.attribute(null, "android:layout_height",
						"wrap_content");
			} else if (height == -1) {
				serializer.attribute(null, "android:layout_height",
						"match_parent");
			} else {
				serializer.attribute(null, "android:layout_height",
						String.valueOf(height) + "dip");
			}

			if (width == -2) {
				serializer.attribute(null, "android:layout_width",
						"wrap_content");
			} else if (width == -1) {
				serializer.attribute(null, "android:layout_width",
						"match_parent");
			} else {
				serializer.attribute(null, "android:layout_width",
						String.valueOf(width) + "dip");
			}

			if (!isRootLayout) {
				MarginLayoutParams margins = (MarginLayoutParams) params;
				if (margins.topMargin != 0) {
					serializer.attribute(null, "android:layout_marginTop",
							String.valueOf(margins.topMargin) + "dip");
				}
				if (margins.leftMargin != 0) {
					serializer.attribute(null, "android:layout_marginLeft",
							String.valueOf(margins.leftMargin) + "dip");
				}
				if (margins.rightMargin != 0) {
					serializer.attribute(null, "android:layout_marginRight",
							String.valueOf(margins.rightMargin) + "dip");
				}
				if (margins.bottomMargin != 0) {
					serializer.attribute(null, "android:layout_marginBottom",
							String.valueOf(margins.bottomMargin) + "dip");
				}
			}

		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}

	}

	public void StartLayoutNode(ViewGroup vg, boolean isRoot)
	{
		String widgetID = vg.getTag().toString();
		String widgetType = vg.getClass().getSimpleName();
		

		LayoutParams params = vg.getLayoutParams();
		try {
			serializer.startTag(null, widgetType);

			if (isRoot) {
				serializer.attribute(null, "xmlns:android",
						"http://schemas.android.com/apk/res/android");
			}

			serializer.attribute(null, "android:id", "@+id/" + widgetID);
			AddLayoutParams(params, isRoot);

			if (vg.getParent() != null && vg.getParent().getClass() == RelativeLayout.class)
			// handle relative layout as parent
			{
				int[] rules = ((android.widget.RelativeLayout.LayoutParams) params)
						.getRules();
				AddRelativeLayoutTags(rules, vg);
			}

			// /////////////HANDLE DIFFERENT LAYOUT TYPES HERE
			if (vg.getClass() == LinearLayout.class) {
				int orientation = ((LinearLayout) vg).getOrientation();

				if (orientation == LinearLayout.VERTICAL) {
					serializer.attribute(null, "android:orientation",
							"vertical");
				} else if (orientation == LinearLayout.HORIZONTAL) {
					serializer.attribute(null, "android:orientation",
							"horizontal");
				}
			}

		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}
	}

	public void FinishLayoutNode(ViewGroup vg)
	{
		String widgetType = vg.getClass().getSimpleName();

		try {
			serializer.endTag(null, widgetType);
		} catch (Exception e) {
//			Log.e("Exception", "error occurred while creating xml file");
		}
	}
}
