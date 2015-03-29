package org.yaaic.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.yaaic.R;

/**
 * Fragment displaying all settings.
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String TRANSACTION_TAG = "fragment_settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
