package com.ami.gui2go;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.ami.gui2go.models.ProjectInfo;
import com.ami.gui2go.utils.FileHelper;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ResourceManagerActivity extends Activity
{
	private ProjectInfo project = null;
	ArrayList<String> images;
	ListView imageListView;
	ImageView previewImage;

	// need a get images method
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resource_manager);

		// Grab info from the intent
		Bundle data = getIntent().getExtras();
		project = data.getParcelable("project");
		this.setTitle("Resource Manager - " + project.name);

		previewImage = (ImageView) findViewById(R.id.previewImage);
		images = FileHelper.getImageNames(project, this);
		imageListView = (ListView) findViewById(R.id.resourceList);
		imageListView.setAdapter(new ImageListAdapter());
		imageListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		imageListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				hideAllActionButtons();
				LinearLayout item = (LinearLayout)view;
				for (int i = 0; i < item.getChildCount(); i++) {
					View temp = item.getChildAt(i);
					if(temp instanceof ImageView){
						temp.setVisibility(View.VISIBLE);
					}
				}
				((ListView) parent).setItemChecked(position, true);
				String info = images.get(position);

				String path = Environment.getExternalStorageDirectory()
						+ "/Gui2Go/Projects/" + project.name + "/Images/"
						+ info;
				Bitmap bmImg = BitmapFactory.decodeFile(path);
				previewImage.setImageBitmap(bmImg);
				FrameLayout previewFrame = (FrameLayout)findViewById(R.id.preview_frame);
				ObjectAnimator.ofFloat(previewFrame, "alpha",1f).setDuration(100).start();
			}
		});
	}

	public void hideAllActionButtons()
	{
		for (int i = 0; i < imageListView.getChildCount(); i++) {
			LinearLayout item = (LinearLayout) imageListView.getChildAt(i);
			for (int j = 0; j < item.getChildCount(); j++) {
				View temp = item.getChildAt(j);
				if(temp instanceof ImageView){
					temp.setVisibility(View.INVISIBLE);
				}
			}
		}
		
	}
	
	private class ImageListAdapter extends ArrayAdapter<String>
	{

		public ImageListAdapter()
		{
			super(ResourceManagerActivity.this, R.layout.image_list_item_row,
					R.id.imageName, images);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			DeleteListener mDeleteListener = new DeleteListener();

			View row = super.getView(position, convertView, parent);

			ImageView deleteBtn = (ImageView) row
					.findViewById(R.id.btn_delete_act);
			deleteBtn.setOnClickListener(mDeleteListener);
			deleteBtn.setTag(position);
			return row;
		}
	}

	private class DeleteListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			int position = (Integer) v.getTag();

			String resName = (String) imageListView.getItemAtPosition(position);
			FileHelper.deleteImage(resName, project.name);
			refreshImageList();
		}
	}
	
	public void refreshImageList()
	{
		images = FileHelper.getImageNames(project, this);
		imageListView.setAdapter(new ImageListAdapter());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return (true);
			case R.id.addResourceBtn:
				showFileOpenDialog();
				return (true);
		}
		return super.onOptionsItemSelected(item);
	}

	private void showFileOpenDialog()
	{
		String path = Environment.getExternalStorageDirectory() + "/";
		FileDialogFragment newFragment = FileDialogFragment.newInstance(path);
		newFragment.show(getFragmentManager(), "dialog");
	}
	
	public void addResource(String path)
	{
		File f = new File(path);
		File toPath = new File(Environment.getExternalStorageDirectory()
				+ "/Gui2Go/Projects/" + project.name + "/" + "Images/"
				+ f.getName());

		Toast.makeText(this,
				"Added: " + f.getName() + " to the resource pool.",
				Toast.LENGTH_SHORT).show();

		try {
			FileHelper.copy(f, toPath);
		} catch (IOException e) {
//			Log.d("file copy", "IS NOT WERK?");
		}
		
		refreshImageList();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.resource_manager_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
