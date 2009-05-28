package org.yaaic.client;

import android.app.Activity;
import android.os.Bundle;

/**
 * About Activity
 * 
 * @author Sebastian Kaspari <s.kaspari@googlemail.com>
 */
public class About extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}
}
