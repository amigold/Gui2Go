package com.ami.gui2go.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.ami.gui2go.models.ProjectInfo;

import android.content.Context;
import android.os.Environment;

public class FileHelper
{
	public static boolean deleteDir(File path)
	{
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDir(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static void copyDirectory(File srcPath, File dstPath)
			throws IOException
	{
		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				dstPath.mkdir();
			}

			String files[] = srcPath.list();
			for (int i = 0; i < files.length; i++) {
				copyDirectory(new File(srcPath, files[i]), new File(dstPath,
						files[i]));
			}
		} else {
			if (!srcPath.exists()) {
//				Log.d("Copy failure", "File or directory does not exist.");
			} else {
				InputStream in = new FileInputStream(srcPath);
				OutputStream out = new FileOutputStream(dstPath);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
		}
		System.out.println("Directory copied.");
	}

	public static void copy(File source, File target) throws IOException
	{

		InputStream in = new FileInputStream(source);
		OutputStream out = new FileOutputStream(target);

		// Copy the bits from instream to outstream
		byte[] buf = new byte[1024];
		int len;

		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}

		in.close();
		out.close();
	}

	public static ArrayList<String> getImageNames(ProjectInfo project,
			Context cont)
	{
		String[] tempRes;
		
		String path = Environment.getExternalStorageDirectory()
				+ "/Gui2Go/Projects/" + project.name + "/Images/";

		File f = new File(path);
		tempRes = f.list();
		return (convertStrArrayToList(tempRes));
	}
	
	public static ArrayList<String> convertStrArrayToList(String[] arr){
		ArrayList<String> res = new ArrayList<String>();
		for (int i = 0; i < arr.length; i++) {
			res.add(arr[i]);
		}
		return res;
	}

	public static void deleteImage(String resName, String projectName)
	{
		String path = Environment.getExternalStorageDirectory()
		+ "/Gui2Go/Projects/" + projectName + "/Images/" + resName;
		File f = new File(path);
		if(f.exists()){
			f.delete();
		}
	}
}
