package com.chaos.sleepcry.busecretary;

import java.util.ArrayList;
import java.util.List;

public enum RepeatCategory {
	NONE(1,"none"),EVERYDAY(2,"every day"),EVERYMONTH(3,"every month"),EVERYYEAR(4,"every year"),
	EVERYHOUR(5,"every hour"),LAST(6,"perid of time");
	private String mDesc;	//description of this category
	private int mId; //the id
	private RepeatCategory(int i,String str){
		this.mDesc = str;
		mId = i;
	}
	public String getDesc() {
		return mDesc;
	}
	public int getId(){
		return mId;
	}
	public static RepeatCategory getInstance(int i){
		switch(i){
		case 1:
			return NONE;
		case 2:
			return EVERYDAY;
		case 3:
			return EVERYMONTH;
		case 4:
			return EVERYYEAR;
		case 5:
			return EVERYHOUR;
		case 6:
			return LAST;
			default:
				return null;
		}
	}
	public List<String> toList(){
		List<String> lstRet = new ArrayList<String>(); 
		lstRet.add("none");
		lstRet.add("every day");
		lstRet.add("every month");
		lstRet.add("every year");
		lstRet.add("every hour");
		lstRet.add("perid of time");
		return lstRet;
	}
}
