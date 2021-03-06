package com.chaos.sleepcry.busecretary;

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
	public static CharSequence[] toArray(){
		CharSequence[] lstRet = new CharSequence[6]; 
		lstRet[0] = "none";
		lstRet[1] = "every day";
		lstRet[2] = "every month";
		lstRet[3] = "every year";
		lstRet[4] = "every hour";
		lstRet[5] = "perid of time";
		return lstRet;
	}
}
