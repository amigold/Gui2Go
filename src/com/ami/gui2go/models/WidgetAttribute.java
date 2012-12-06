package com.ami.gui2go.models;

public class WidgetAttribute
{
	private String attrName;
	private String attrValue;
	
	public WidgetAttribute(String AttrName, String AttrValue)
	{
		attrName = AttrName;
		attrValue = AttrValue;
	}

	public String getAttrName()
	{
		return attrName;
	}

	public void setAttrName(String AttrName)
	{
		attrName = AttrName;
	}

	public String getAttrValue()
	{
		return attrValue;
	}

	public void setAttrValue(String AttrValue)
	{
		attrValue = AttrValue;
	}
}
