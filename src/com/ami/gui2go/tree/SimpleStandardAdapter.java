package com.ami.gui2go.tree;

import java.util.Arrays;
import java.util.Set;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ami.gui2go.EditorActivity;
import com.ami.gui2go.R;
import com.ami.gui2go.models.WidgetTypes;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
public class SimpleStandardAdapter extends AbstractTreeViewAdapter<Long>
{
	private final Set<Long> selected;
	private View highlightedID = null;
	private int tempHighlightedId;

	public View getHighlightedID()
	{
		return highlightedID;
	}

	public void setHighlightedID(View highlightedID)
	{
		this.highlightedID = highlightedID;
	}

	private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(final CompoundButton buttonView,
				final boolean isChecked)
		{
			final Long id = (Long) buttonView.getTag();
			changeSelected(isChecked, id);
		}
	};

	private void changeSelected(final boolean isChecked, final Long id)
	{
		if (isChecked) {
			// selected.clear();
			selected.add(id);
		} else {
			selected.remove(id);
		}
		// AMI ADDED
		View myView = activity.findViewById(id.intValue());
		if (myView.getClass() == ListView.class
				|| myView.getClass() == Spinner.class
				|| myView.getClass() == GridView.class) {
			// special treatement for spinners and listviews
			// they can only be selected in the tree
			((EditorActivity) activity).performHightlight(myView);
		} else {
			myView.performClick();
		}

	}

	public SimpleStandardAdapter(final EditorActivity honey,
			final Set<Long> selected,
			final TreeStateManager<Long> treeStateManager,
			final int numberOfLevels)
	{
		super(honey, treeStateManager, numberOfLevels);
		this.selected = selected;
	}

	@SuppressWarnings("unused")
	private String getDescription(final long id)
	{
		final Integer[] hierarchy = getManager().getHierarchyDescription(id);
		return "Node " + id + Arrays.asList(hierarchy);
	}

	@Override
	public View getNewChildView(final TreeNodeInfo<Long> treeNodeInfo)
	{
		final LinearLayout viewLayout = (LinearLayout) getActivity()
				.getLayoutInflater().inflate(R.layout.tree_list_item, null);
		return updateView(viewLayout, treeNodeInfo);
	}

	@Override
	public LinearLayout updateView(final View view,
			final TreeNodeInfo<Long> treeNodeInfo)
	{
		// get handlers to the views
		final LinearLayout viewLayout = (LinearLayout) view;
		final TextView descriptionView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_description);
		final TextView levelView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_level);
		final TextView classView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_class);
		final ImageView classIconView = (ImageView) viewLayout
				.findViewById(R.id.tree_item_class_icon);
		final ImageView lockIconView = (ImageView) viewLayout
				.findViewById(R.id.tree_item_lock_icon);
		final CheckBox box = (CheckBox) viewLayout
				.findViewById(R.id.demo_list_checkbox);

		Long myID = treeNodeInfo.getId();
		// get the view represented by the tree item
		View myView = activity.findViewById(myID.intValue());
		// start setting the view properties
		CharSequence descText = (CharSequence) myView.getTag();
		descriptionView.setText(descText);
		classView.setText("("
				+ (CharSequence) myView.getClass().getSimpleName() + ")");
		levelView.setText(Integer.toString(treeNodeInfo.getLevel()));

		int classIconResourceId = determineClassIconResourceId(myView
				.getClass().getSimpleName());
		classIconView.setImageResource(classIconResourceId);

		if (!descText.equals("LayoutRoot")) {
			ExtrasClickListener mExtrasClickListener = new ExtrasClickListener();
			viewLayout.setOnClickListener(mExtrasClickListener);
			levelView.setOnClickListener(mExtrasClickListener);
			classView.setOnClickListener(mExtrasClickListener);
			descriptionView.setOnClickListener(mExtrasClickListener);
			lockIconView.setOnClickListener(mExtrasClickListener);
			classIconView.setOnClickListener(mExtrasClickListener);

			ExtrasLongClickListener mExtrasLongClickListener = new ExtrasLongClickListener();
			viewLayout.setOnLongClickListener(mExtrasLongClickListener);
			levelView.setOnLongClickListener(mExtrasLongClickListener);
			classView.setOnLongClickListener(mExtrasLongClickListener);
			descriptionView.setOnLongClickListener(mExtrasLongClickListener);
			lockIconView.setOnLongClickListener(mExtrasLongClickListener);
			classIconView.setOnLongClickListener(mExtrasLongClickListener);
		}

		box.setTag(treeNodeInfo.getId());
		if (treeNodeInfo.isWithChildren()) {
			box.setVisibility(View.INVISIBLE);
		} else {
			box.setVisibility(View.INVISIBLE);
			box.setChecked(selected.contains(treeNodeInfo.getId()));
		}
		box.setOnCheckedChangeListener(onCheckedChange);

		if (myView.getClass().getSuperclass() == ViewGroup.class
				|| myView.getClass() == TableLayout.class
				|| myView.getClass() == TableRow.class
				|| myView.getClass() == RadioGroup.class) {
			viewLayout.setOnDragListener(new LayoutDragListener());
		}

		return viewLayout;
	}

	private int determineClassIconResourceId(String classType)
	{
		if (classType.equals(WidgetTypes.BUTTON_TAG)) {
			return R.drawable.mini_button;
		} else if (classType.equals(WidgetTypes.CHECKBOX_TAG)) {
			return R.drawable.mini_checkbox;
		} else if (classType.equals(WidgetTypes.TEXTVIEW_TAG)) {
			return R.drawable.mini_textview;
		} else if (classType.equals(WidgetTypes.IMAGEVIEW_TAG)) {
			return R.drawable.mini_imageview;
		} else if (classType.equals(WidgetTypes.EDITTEXT_TAG)) {
			return R.drawable.mini_edittext;
		} else if (classType.equals(WidgetTypes.RADIOBUTTON_TAG)) {
			return R.drawable.mini_radiobutton;
		} else if (classType.equals(WidgetTypes.SPINNER_TAG)) {
			return R.drawable.mini_spinner;
		} else if (classType.equals(WidgetTypes.TOGGLEBUTTON_TAG)) {
			return R.drawable.mini_togglebutton;
		} else if (classType.equals(WidgetTypes.LINEAR_TAG)) {
			return R.drawable.mini_linearlayout;
		} else if (classType.equals(WidgetTypes.RELATIVE_TAG)) {
			return R.drawable.mini_relativelayout;
		} else if (classType.equals(WidgetTypes.FRAME_TAG)) {
			return R.drawable.mini_framelayout;
		} else if (classType.equals(WidgetTypes.TABLE_TAG)) {
			return R.drawable.mini_tablelayout;
		} else if (classType.equals(WidgetTypes.LISTVIEW_TAG)) {
			return R.drawable.mini_listview;
		} else if (classType.equals(WidgetTypes.GRIDVIEW_TAG)) {
			return R.drawable.mini_gridview;
		} else if (classType.equals(WidgetTypes.RADIOGROUP_TAG)) {
			return R.drawable.mini_radiogroup;
		}
		return R.drawable.icon;
	}

	@Override
	public void handleItemClick(View view, final Object id)
	{
		Long longId;
		if (id.getClass() == Integer.class) {
			Integer temp = (Integer) id;
			longId = Long.valueOf(temp.longValue());
		} else {
			longId = (Long) id;
		}

		final TreeNodeInfo<Long> info = getManager().getNodeInfo(longId);

		if (view.getClass().getSuperclass() != ViewGroup.class) {
			view = (View) view.getParent().getParent();
		}
		final ViewGroup vg = (ViewGroup) view;
		final CheckBox cb = (CheckBox) vg.findViewById(R.id.demo_list_checkbox);
		cb.performClick();

		// if (highlightedID != null)
		// {
		// if (vg == highlightedID)
		// {
		// vg.setBackgroundDrawable(null);
		// highlightedID = null;
		// } else
		// {
		// ((ViewGroup) highlightedID).setBackgroundDrawable(null);
		// vg.setBackgroundResource(R.drawable.hightlight_border);
		// highlightedID = vg;
		// }
		// } else
		// {
		// vg.setBackgroundResource(R.drawable.hightlight_border);
		// highlightedID = vg;
		// }

		if (info.isWithChildren()) {
			// super.handleItemClick(view, id);
		}
	}

	@Override
	public long getItemId(final int position)
	{
		return getTreeId(position);
	}

	public class ExtrasClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			handleItemClick(v, ((View) v.getParent().getParent()).getTag());
		}
	}

	public class ExtrasLongClickListener implements OnLongClickListener
	{
		@Override
		public boolean onLongClick(View v)
		{
			if (v.getClass() != LinearLayout.class) {
				v = (View) v.getParent();
			}
			LinearLayout lay = (LinearLayout) v;
			String tag = ((View) lay.getParent()).getTag().toString();
			String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };

			ClipData.Item item = new ClipData.Item(tag);
			ClipData dragData = new ClipData("TreeItem", mimeTypes, item);

			View.DragShadowBuilder myShadow = new DragShadowBuilder(v);

			v.startDrag(dragData, myShadow, null, 0);
			return true;
		}
	}

	public class LayoutDragListener implements OnDragListener
	{
		@Override
		public boolean onDrag(View v, DragEvent event)
		{
			final int action = event.getAction();
			EditorActivity act = (EditorActivity) getActivity();

			switch (action) {
				case DragEvent.ACTION_DRAG_STARTED:
					// what happens when the event starts
					if (event.getClipDescription().hasMimeType(
							ClipDescription.MIMETYPE_TEXT_PLAIN)) {
						act.showDeleteBtn();
						return (true);
					} else {
						return (false);
					}
				case DragEvent.ACTION_DRAG_ENTERED:
					hightlightTargetView(v, "#66FF00",
							R.drawable.hightlight_border_green, false);
					tempHighlightedId = v.getId();
					return (true);
				case DragEvent.ACTION_DRAG_LOCATION:
					return (false);
				case DragEvent.ACTION_DRAG_EXITED:
					tempHighlightedId = 0;
					removeHighlight(v);
					return (true);
				case DragEvent.ACTION_DROP:
				{
					View viewHolder = null;
					ViewGroup viewParent = null;
					ClipData.Item item = event.getClipData().getItemAt(0);
					final String dragData = (String) item.getText();

					if (!dragData.equals(((View) v.getParent()).getTag()
							.toString())) {
						viewHolder = activity.findViewById(Integer
								.valueOf(dragData));
						int parentId = Integer.valueOf(((ViewGroup) v
								.getParent()).getTag().toString());
						viewParent = (ViewGroup) activity
								.findViewById(parentId);

						if (viewHolder.getId() != ((View) viewParent
								.getParent()).getId()) {
							// delete the widget from its current parent
							if (EditorActivity.mode != null) {
								EditorActivity.mode.finish();
								act.ClearHightlightedViews(false);
							}
							act.DeleteWidget(viewHolder);
							// add it to new parent
							act.InsertViewToParent(viewHolder, viewParent);
						}
						return (true);
					}

				}
				case DragEvent.ACTION_DRAG_ENDED:
					tempHighlightedId = 0;
					removeHighlight(v);
					return (true);
				default:
					Toast.makeText(activity, "Unknown drag received",
							Toast.LENGTH_LONG).show();
					return (false);
			}
		}
	}

	private void hightlightTargetView(View v, String rgbValue, int borderId,
			boolean highlightInTree)
	{
		if (tempHighlightedId != v.getId() && tempHighlightedId != 0) {
			removeHighlight(v);
		}
		if (((EditorActivity) activity).isLayout(v)) {
			v.setBackgroundResource(borderId);
		} else if (v.getClass() == ImageView.class) {
			((ImageView) v).setColorFilter(Color.parseColor(rgbValue),
					Mode.DARKEN);
		} else {
			v.getBackground().setColorFilter(Color.parseColor(rgbValue),
					Mode.DARKEN);
		}
	}

	private void removeHighlight(View v)
	{
		if (((EditorActivity) activity).isLayout(v)) {
			v.setBackgroundDrawable(null);
		} else if (v.getClass() == ImageView.class) {
			((ImageView) v).setColorFilter(null);
		} else {
			v.getBackground().setColorFilter(null);
		}
		v.invalidate();
	}
}