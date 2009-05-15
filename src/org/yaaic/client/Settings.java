package org.yaaic.client;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
