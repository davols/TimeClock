package se.dxapps.beta.timeclock;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import se.dxapps.timeclock.R;

public class MyPrefsActivity extends PreferenceActivity{
	@Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.preferences);        
    }
}
