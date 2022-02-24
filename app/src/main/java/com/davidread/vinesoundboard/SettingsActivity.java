package com.davidread.vinesoundboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

/**
 * {@link SettingsActivity} simply displays a {@link SettingsFragment} in its UI.
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Invoked when this {@link SettingsActivity} is initially starting. It simply inflates the
     * activity's layout.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    /**
     * Invoked when an app bar action button is selected.
     *
     * @param item {@link MenuItem} that is invoking this method.
     * @return False to allow normal processing to proceed. True to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        /* When the up button is selected, replicate the back button behavior so a back animation
         * displays. */
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@link SettingsFragment} represents a settings screen user interface.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {

        /**
         * Invoked when this {@link SettingsFragment} is initially created. It inflates a
         * preferences layout and binds each {@link Preference}'s summary to its current value.
         *
         * @param savedInstanceState {@link Bundle} object where instance state from a previous
         *                           configuration change is stored.
         * @param rootKey            {@link String} that if non-null, this preference fragment
         *                           should be rooted at the
         *                           {@link androidx.preference.PreferenceScreen} with this key.
         */
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

            // Inflate preferences layout.
            setPreferencesFromResource(R.xml.settings, rootKey);

            // Bind each preference's summary to its current value.
            Preference orderByPreference = findPreference(getString(R.string.order_by_key));
            if (orderByPreference != null) {
                bindSummaryToValue(orderByPreference);
            }
        }

        /**
         * Binds the summary of a {@link Preference} to its value.
         *
         * @param preference {@link Preference} to be bound.
         */
        private void bindSummaryToValue(Preference preference) {

            /* Attach an OnPreferenceChangeListener that sets the Preference's summary as the
             * Preference's value each time the value is changed. */
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    String newValueString = newValue.toString();
                    if (preference instanceof ListPreference) {
                        ListPreference listPreference = (ListPreference) preference;
                        int listPreferenceIndex = listPreference.findIndexOfValue(newValueString);
                        CharSequence[] listPreferenceLabels = listPreference.getEntries();
                        preference.setSummary(listPreferenceLabels[listPreferenceIndex]);
                        return true;
                    }
                    return false;
                }
            });

            // Set the Preference's summary as the current Preference's value.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String summary = sharedPreferences.getString(preference.getKey(), "");
            preference.getOnPreferenceChangeListener().onPreferenceChange(preference, summary);
        }
    }
}
