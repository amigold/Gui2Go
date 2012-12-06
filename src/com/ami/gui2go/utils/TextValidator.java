package com.ami.gui2go.utils;

public class TextValidator
{	
	public static boolean isNameFieldValid(String fieldText)
	{
		boolean result = true;
		if (fieldText.isEmpty() || fieldText.contains(",")
				|| fieldText.contains(".")
				|| fieldText.contains("/")
				|| fieldText.contains("\\")
				|| fieldText.contains("!")
				|| fieldText.contains("@")
				|| fieldText.contains("#")
				|| fieldText.contains("$")
				|| fieldText.contains("%")
				|| fieldText.contains("^")
				|| fieldText.contains("&")
				|| fieldText.contains("*")
				|| fieldText.contains("(")
				|| fieldText.contains(")")
				|| fieldText.contains("\"")
				|| fieldText.contains(":")
				|| fieldText.contains("=")
				|| fieldText.contains("+")
				|| fieldText.contains("?")) {
			result = false;
		}
		
		if(fieldText.length() > 20){
			result = false;
		}
		return result;
	}
}