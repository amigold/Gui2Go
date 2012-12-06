package com.ami.gui2go.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ActivityInfo implements Parcelable
{
	public String name;
	public String screenSize;
	public String rootLayoutType;

	public ActivityInfo(){
		
	}
	
	public ActivityInfo(Parcel in){
		String[] data = new String[2];
		in.readStringArray(data);
		
		this.name = data[0];
		this.screenSize = data[1];
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeStringArray(new String[] {this.name,this.screenSize});
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
	{

		@Override
		public ActivityInfo createFromParcel(Parcel in)
		{
			return new ActivityInfo(in);
		}

		@Override
		public ActivityInfo[] newArray(int size)
		{
			return new ActivityInfo[size];
		}
	};
}
