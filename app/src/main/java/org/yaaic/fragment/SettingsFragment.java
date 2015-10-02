package org.yaaic.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.yaaic.R;
import org.yaaic.activity.YaaicActivity;

/**
 * Fragment displaying all settings.
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String TRANSACTION_TAG = "fragment_settings";

    private YaaicActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof YaaicActivity)) {
            throw new IllegalArgumentException("Activity has to implement YaaicActivity interface");
        }

        this.activity = (YaaicActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        activity.setToolbarTitle(getString(R.string.navigation_settings));
    }
}
