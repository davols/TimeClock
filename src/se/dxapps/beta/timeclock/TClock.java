package se.dxapps.beta.timeclock;

import android.app.Application;


public class TClock extends Application{
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		//ACRA.init(this); disabled now. 
		super.onCreate();
	}
}
