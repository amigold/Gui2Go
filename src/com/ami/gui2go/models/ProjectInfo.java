package com.ami.gui2go.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectInfo implements Parcelable
{
	public String name;
	public String targetSDK;
	public String mainActivityName;
	public String author;
	
	public ProjectInfo(){
		
	}
	
	public ProjectInfo(Parcel in)
	{
		String[] data = new String[4];

		in.readStringArray(data);
		this.name = data[0];
		this.targetSDK = data[1];
		this.mainActivityName = data[2];
		this.author = data[3];
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeStringArray(new String[] { this.name, this.targetSDK,
				this.mainActivityName, this.author});
	}
	
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
	{

		@Override
		public ProjectInfo createFromParcel(Parcel in)
		{
			return new ProjectInfo(in);
		}

		@Override
		public ProjectInfo[] newArray(int size)
		{
			return new ProjectInfo[size];
		}
	};
}
