package com.ami.gui2go;

import java.util.ArrayList;

import com.ami.gui2go.models.ActivityInfo;
import com.ami.gui2go.models.ProjectInfo;
import com.ami.gui2go.models.WidgetTypes;
import com.ami.gui2go.utils.LayoutXMLCreator;
import com.ami.gui2go.utils.ProjectXMLParser;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityManager extends Activity
{    
	private ProjectInfo project = null;
	private ArrayList<ActivityInfo> activityList;
	ArrayList<String> activities;

	// Widgets
	ListView activityListView;
	TextView activityNameTxt;
	TextView activityScreenTxt;

	private class ActivityListAdapter extends ArrayAdapter<String>
	{

		public ActivityListAdapter()
		{
			super(ActivityManager.this, R.layout.activity_list_item_row,
					R.id.activityName, activities);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			DeleteListener mDeleteListener = new DeleteListener();
			EditListener mEditListener = new EditListener();
			SetAsMainListener mSetAsMainListener = new SetAsMainListener();
			ResizeListener mResizeListener = new ResizeListener();
			RenameListener mRenameListener = new RenameListener();
			CloneListener mCloneListener = new CloneListener();

			View row = super.getView(position, convertView, parent);
			ImageView icon = (ImageView) row.findViewById(R.id.homeActIcon);

			if (project.mainActivityName.equals(activities.get(position))) {
				// put the home icon on the main activity
				icon.setImageResource(R.drawable.ic_home);
			} else {
				icon.setImageResource(R.drawable.ic_activity);
			}

			ImageView deleteBtn = (ImageView) row
					.findViewById(R.id.btn_delete_act);
			deleteBtn.setOnClickListener(mDeleteListener);
			deleteBtn.setTag(position);

			ImageView editBtn = (ImageView) row.findViewById(R.id.btn_edit_act);
			editBtn.setOnClickListener(mEditListener);
			editBtn.setTag(position);

			ImageView setAsMainBtn = (ImageView) row
					.findViewById(R.id.btn_set_act_as_main);
			setAsMainBtn.setOnClickListener(mSetAsMainListener);
			setAsMainBtn.setTag(position);

			ImageView cloneBtn = (ImageView) row
					.findViewById(R.id.btn_clone_act);
			cloneBtn.setOnClickListener(mCloneListener);
			cloneBtn.setTag(position);

			ImageView resizeBtn = (ImageView) row
					.findViewById(R.id.btn_resize_act);
			resizeBtn.setOnClickListener(mResizeListener);
			resizeBtn.setTag(position);

			ImageView renameBtn = (ImageView) row
					.findViewById(R.id.btn_rename_act);
			renameBtn.setOnClickListener(mRenameListener);
			renameBtn.setTag(position);

			return row;
		}
	}

	private class CloneListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			final View sizeView = getLayoutInflater().inflate(
					R.layout.new_activity_dialog, null);

			int position = (Integer) v.getTag();

			final String activityName = (String) activityListView
					.getItemAtPosition(position);

			LinearLayout unimportant = (LinearLayout) sizeView
					.findViewById(R.id.LinearLayout01);
			unimportant.setVisibility(ViewGroup.GONE);
			LinearLayout unimportant2 = (LinearLayout) sizeView
					.findViewById(R.id.linearLayout5);
			unimportant2.setVisibility(ViewGroup.GONE);

			new AlertDialog.Builder(ActivityManager.this)
					.setTitle("Clone Activity..")
					.setView(sizeView)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog,
										int whichbutton)
								{
									EditText newActName = (EditText) sizeView
											.findViewById(R.id.new_act_name);

									if (newActName.getText().toString().isEmpty()) {
										Toast.makeText(ActivityManager.this,
												"Invalid activity name!",
												Toast.LENGTH_SHORT).show();
										dialog.cancel();
									}

									cloneActivity(activityName, newActName
											.getText().toString(), project);
								}
							}).setNegativeButton("Cancel", null).show();
		}
	}

	private class DeleteListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (activityList.size() == 1) {
				Toast.makeText(ActivityManager.this,
						"Cannot delete the only activity in a project!",
						Toast.LENGTH_SHORT).show();
			} else {
				int position = (Integer) v.getTag();

				ActivityInfo activity = activityList.get(position);
//				String activityName = (String) activityListView
//				.getItemAtPosition(position);
				// start deleting
				showDeleteConfirmationDialog(activity);
			}
		}

		
	}

	private void deleteActivity(ActivityInfo activity)
	{
		ProjectXMLParser parser = new ProjectXMLParser(project.name,
				ActivityManager.this);
		parser.StartParser();
		parser.DeleteActivityNode(activity.name, project);
		activityList.remove(activity);
		activities = GetActivityNames(activityList);
		refreshActivityList();
	}
	
	private class EditListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			try {
				int position = (Integer) v.getTag();
				ActivityInfo act = activityList.get(position);

				// Load the main activity in the project
				loadEditorActivity(act);
			} catch (Exception e) {
//				Log.d("edit activity", e.getMessage());
				Toast.makeText(ActivityManager.this, "Couldn't load project!",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class RenameListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			final View renameView = getLayoutInflater().inflate(
					R.layout.rename_dialog, null);

			final TextView currName = (TextView) renameView
					.findViewById(R.id.rename_dialog_curr_name);
			int pos = (Integer) v.getTag();
			currName.setText((CharSequence) activityListView
					.getItemAtPosition(pos));

			new AlertDialog.Builder(ActivityManager.this)
					.setTitle("Rename Activity...")
					.setView(renameView)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog,
										int whichbutton)
								{
									EditText newname = (EditText) renameView
											.findViewById(R.id.rename_dialog_edittext);
									CharSequence toSet = (CharSequence) newname
											.getText();
									if (toSet.length() == 0) {
										Toast.makeText(
												ActivityManager.this,
												"Cannot set empty name! Made no changes.",
												Toast.LENGTH_SHORT).show();
									} else if (activities.contains(toSet)) {
										Toast.makeText(ActivityManager.this,
												"Name already exists!",
												Toast.LENGTH_SHORT).show();
									} else {
										changeActivityName(currName.getText()
												.toString(), toSet.toString());
									}
								}
							}).setNegativeButton("Cancel", null).show();
		}
	}

	private class ResizeListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			final View sizeView = getLayoutInflater().inflate(
					R.layout.resize_activity_dialog, null);

			// set window size spinner values
			int pos = (Integer) v.getTag();
			final ActivityInfo act = activityList.get(pos);

			final TextView currSize = (TextView) sizeView
					.findViewById(R.id.activity_size_label2);
			currSize.setText(act.screenSize);

			Spinner windowSize = (Spinner) sizeView
					.findViewById(R.id.new_act_size);
			ArrayAdapter<CharSequence> adapter2 = ArrayAdapter
					.createFromResource(ActivityManager.this,
							R.array.screenSizes,
							android.R.layout.simple_spinner_item);
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			windowSize.setAdapter(adapter2);

			new AlertDialog.Builder(ActivityManager.this)
					.setTitle("Resize Activity..")
					.setView(sizeView)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog,
										int whichbutton)
								{

									Spinner activitySize = (Spinner) sizeView
											.findViewById(R.id.new_act_size);

									act.screenSize = activitySize
											.getSelectedItem().toString();

									changeActivitySize(
											(String) currSize.getText(),
											act.screenSize);
								}
							}).setNegativeButton("Cancel", null).show();
		}
	}

	private class SetAsMainListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			int position = (Integer) v.getTag();

			String activityName = (String) activityListView
					.getItemAtPosition(position);

			ProjectXMLParser parser = new ProjectXMLParser(project.name,
					ActivityManager.this);
			parser.StartParser();
			parser.SetActivityAsMain(activityName);

			ActivityManager.this.project.mainActivityName = activityName;
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> adapter = (ArrayAdapter<String>) activityListView
					.getAdapter();
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manager);

		// Grab info from the intent
		Bundle data = getIntent().getExtras();
		project = data.getParcelable("project");
		this.setTitle("Activity Manager - " + project.name);

		activityList = GetAvailableActivities();

		// Set the list adapter
		activities = GetActivityNames(activityList);

		activityNameTxt = (TextView) findViewById(R.id.activity_name_text);
		activityScreenTxt = (TextView) findViewById(R.id.activity_screen_size_text);

		activityListView = (ListView) findViewById(R.id.activityList);
		activityListView.setAdapter(new ActivityListAdapter());
		activityListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// getView().setBackgroundColor(Color.parseColor("#809fd600"));

		activityListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				hideAllActionButtons();
				LinearLayout item = (LinearLayout) view;
				for (int i = 0; i < item.getChildCount(); i++) {
					View temp = item.getChildAt(i);
					if (temp instanceof ImageView) {
						temp.setVisibility(View.VISIBLE);
					}
				}
				((ListView) parent).setItemChecked(position, true);
				ActivityInfo info = activityList.get(position);
				showData(info.name, info.screenSize);
			}
		});
	}

	public void hideAllActionButtons()
	{
		for (int i = 0; i < activityListView.getChildCount(); i++) {
			LinearLayout item = (LinearLayout) activityListView.getChildAt(i);
			for (int j = 1; j < item.getChildCount(); j++) {
				View temp = item.getChildAt(j);
				if (temp instanceof ImageView) {
					temp.setVisibility(View.INVISIBLE);
				}
			}
		}

	}

	public void showData(String name, String screenSize)
	{
		FrameLayout detailsFrame = (FrameLayout) findViewById(R.id.detailsFrame);
		ObjectAnimator.ofFloat(detailsFrame, "alpha", 1f).setDuration(100)
				.start();
		activityNameTxt.setText(name);
		activityScreenTxt.setText(screenSize);
	}

	protected void AddNewActivity(ActivityInfo activity)
	{
		if (activities.contains(activity.name)) {
			Toast.makeText(ActivityManager.this,
					"An activity with this name already exists!",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// modify the project XML to own another activity
		ProjectXMLParser parser = new ProjectXMLParser(project.name,
				ActivityManager.this);
		parser.StartParser();
		parser.AddNewActivityNode(activity);

		// create the basic activity XML
		LayoutXMLCreator creator = new LayoutXMLCreator(activity.name,
				project.name);
		ViewGroup mainlay = createTempWidget(activity.rootLayoutType);
		creator.StartLayoutNode(mainlay, true);
		creator.FinishLayoutNode(mainlay);
		creator.endDocument();

		activityList.add(activity);
		activities = GetActivityNames(activityList);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) activityListView
				.getAdapter();
		adapter.add(activity.name);
		adapter.notifyDataSetChanged();

	}

	private ViewGroup createTempWidget(String viewType)
	{
		ViewGroup newWidget = null;

		if (viewType.equals(WidgetTypes.LINEAR_TAG)) {
			newWidget = new LinearLayout(this);
		} else if (viewType.equals(WidgetTypes.RELATIVE_TAG)) {
			newWidget = new RelativeLayout(this);
		} else if (viewType.equals(WidgetTypes.FRAME_TAG)) {
			newWidget = new FrameLayout(this);
		} else if (viewType.equals(WidgetTypes.TABLE_TAG)) {
			newWidget = new TableLayout(this);
		}
		newWidget.setTag("LayoutRoot");
		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		newWidget.setLayoutParams(params);
		
		return newWidget;
	}

	private void changeActivityName(String oldName, String newName)
	{
		if (project.mainActivityName == oldName) {
			project.mainActivityName = newName;
		}

		ProjectXMLParser parser = new ProjectXMLParser(project.name,
				ActivityManager.this);
		parser.StartParser();
		parser.renameActivity(oldName, newName);

		refreshActivityList();
	}

	private void changeActivitySize(String oldSize, String newSize)
	{
		if (project.mainActivityName == oldSize) {
			project.mainActivityName = newSize;
			return;
		}

		ProjectXMLParser parser = new ProjectXMLParser(project.name,
				ActivityManager.this);
		parser.StartParser();
		parser.resizeActivity(oldSize, newSize);

		refreshActivityList();
	}

	private void cloneActivity(String activityName, String activity,
			ProjectInfo project)
	{
		ProjectXMLParser parser = new ProjectXMLParser(project.name,
				ActivityManager.this);
		parser.StartParser();
		parser.cloneActivity(activityName, activity);

		refreshActivityList();
	}

	public void enablePersistentSelection()
	{
		activityListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	private ArrayList<String> GetActivityNames(
			ArrayList<ActivityInfo> activityList)
	{
		ArrayList<String> res = new ArrayList<String>();
		for (ActivityInfo activityInfo : activityList) {
			res.add(activityInfo.name);
		}
		return res;
	}

	private ArrayList<ActivityInfo> GetAvailableActivities()
	{
		ProjectXMLParser parser = new ProjectXMLParser(project.name,
				ActivityManager.this);
		parser.StartParser();
		ArrayList<ActivityInfo> activityList = parser.GetActivityList();
		return activityList;
	}

	public void loadEditorActivity(ActivityInfo actInfo)
	{
		Intent i = new Intent(this, EditorActivity.class);
		i.putExtra("project", project);
		i.putExtra("activity", actInfo);
		i.putExtra("projectLoaderFlag", true);
		startActivity(i);
	}

	private void showDeleteConfirmationDialog(final ActivityInfo activity){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this);
		builder.setMessage("Are you sure you want delete this activity?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									int id)
							{
								deleteActivity(activity);
							}
						})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									int id)
							{
								dialog.cancel();
							}
						}).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_manager_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return (true);
			case R.id.add_activity_menu:
				showAddNewActivityDialog(project);
				return (true);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
	}

	public void refreshActivityList()
	{
		activityList = GetAvailableActivities();

		activities = GetActivityNames(activityList);
		activityListView.setAdapter(new ActivityListAdapter());
	}

	public void showAddNewActivityDialog(final ProjectInfo project)
	{
		final View sizeView = getLayoutInflater().inflate(
				R.layout.new_activity_dialog, null);

		// set window size spinner values
		Spinner windowSize = (Spinner) sizeView.findViewById(R.id.new_act_size);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter
				.createFromResource(this, R.array.screenSizes,
						android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		windowSize.setAdapter(adapter2);

		// set root layout types spinner
		final Spinner layoutType = (Spinner) sizeView
				.findViewById(R.id.layout_types_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.rootLayoutTypes,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		layoutType.setAdapter(adapter);

		new AlertDialog.Builder(this).setTitle("New Activity..")
				.setView(sizeView)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int whichbutton)
					{
						EditText projName = (EditText) sizeView
								.findViewById(R.id.new_act_name);
						Spinner activitySize = (Spinner) sizeView
								.findViewById(R.id.new_act_size);

						if (projName.getText().toString().isEmpty()) {
							Toast.makeText(ActivityManager.this,
									"Invalid activity name!",
									Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}

						ActivityInfo activity = new ActivityInfo();
						activity.name = projName.getText().toString();
						activity.screenSize = activitySize.getSelectedItem()
								.toString();
						activity.rootLayoutType = layoutType.getSelectedItem().toString(); 

						AddNewActivity(activity);
					}
				}).setNegativeButton("Cancel", null).show();
	}
}