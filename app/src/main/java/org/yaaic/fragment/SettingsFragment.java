package org.yaaic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.yaaic.R;
import org.yaaic.activity.YaaicActivity;

/**
 * Fragment displaying all settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String TRANSACTION_TAG = "fragment_settings";

    private YaaicActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof YaaicActivity)) {
            throw new IllegalArgumentException("Activity has to implement YaaicActivity interface");
        }

        this.activity = (YaaicActivity) context;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        activity.setToolbarTitle(getString(R.string.navigation_settings));
    }
}
