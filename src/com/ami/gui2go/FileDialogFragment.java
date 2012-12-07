package com.ami.gui2go;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileDialogFragment extends DialogFragment
{
	String initialPath = null;
	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";
	TextView pathTextView;
	ListView fileList;
	ArrayList<String> supportedFileExtensions;

	static FileDialogFragment newInstance(String name)
	{
		FileDialogFragment f = new FileDialogFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString("projectName", name);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater
				.inflate(R.layout.file_dialog_layout, container, false);
		pathTextView = (TextView) v.findViewById(R.id.path);
		fileList = (ListView) v.findViewById(R.id.file_list);
		fileList.setOnItemClickListener(new FileItemClickListener());

		// set some dialog options
		getDialog().setTitle("Choose a file:");

		supportedFileExtensions = new ArrayList<String>();
		supportedFileExtensions.add("xml");
		supportedFileExtensions.add("jpg");
		supportedFileExtensions.add("png");
		supportedFileExtensions.add("gif");
		supportedFileExtensions.add("bmp");
		// get the initial dir list
		getDir(initialPath);
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initialPath = getArguments().getString("projectName");
	}

	private void getDir(String dirPath)
	{

		pathTextView.setText("Location: " + dirPath);

		item = new ArrayList<String>();
		path = new ArrayList<String>();

		File f = new File(dirPath);
		File[] files = f.listFiles(new FileFilter()
		{

			@Override
			public boolean accept(File pathname)
			{
				if (pathname.isHidden())
					return false;
				if (!pathname.canRead())
					return false;
				if (pathname.isDirectory())
					return true;

				String fileName = pathname.getName();
				String fileExtension;
				int mid = fileName.lastIndexOf(".");
				fileExtension = fileName.substring(mid + 1, fileName.length());
				for (String s : supportedFileExtensions) {
					if (s.contentEquals(fileExtension)) {
						return true;
					}
				}

				return false;
			}
		});

		if (!dirPath.equals(root)) {

			item.add(root);
			path.add(root);

			item.add("../");
			path.add(f.getParent());

		}

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			path.add(file.getPath());
			if (file.isDirectory())
				item.add(file.getName() + "/");
			else
				item.add(file.getName());
		}

		ArrayAdapter<String> fileListAdapter = new ArrayAdapter<String>(
				getActivity(), R.layout.file_list_item_row, item);
		fileList.setAdapter(fileListAdapter);
	}

	private class FileItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id)
		{
			File file = new File(path.get(position));

			if (file.isDirectory()) {
				getDir(path.get(position));
			} else {
				// if its not a directory
				Activity act = getActivity();
				if (act instanceof HomeActivity) {
					((HomeActivity) act).doFileItemClick(file.getAbsolutePath());
					getDialog().dismiss();
				} else if (act instanceof EditorActivity) {
					((EditorActivity) act).addResource(file.getAbsolutePath());
					getDialog().dismiss();
				} else if (act instanceof ResourceManagerActivity) {
					((ResourceManagerActivity) act).addResource(file.getAbsolutePath());
					getDialog().dismiss();
				}
			}
		}
	}
}
