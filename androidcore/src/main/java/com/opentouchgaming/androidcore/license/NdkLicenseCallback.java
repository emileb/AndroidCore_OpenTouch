package com.opentouchgaming.androidcore.license;

public interface NdkLicenseCallback {
	
	public  static final int GOOD = 54;
	public  static final int NO_GOOD = 50;
	public  static final int ERROR = 90;
	
	
	public class LicStatus
	{
		public int code;
		public String desc;
	};
	
	public void status(LicStatus ret);
	
}
