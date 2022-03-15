package com.davidread.vinesoundboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * {@link MainActivity} is a soundboard user interface. It retrieves available sounds from
 * {@link #soundboard} and displays them in {@link #soundRecyclerView}. It displays an ad banner
 * beneath the {@link #soundRecyclerView}.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * {@link String} tag for log messages originating from this {@link MainActivity}.
     */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Int identifiers for permission requests to perform some action.
     */
    private final int DOWNLOAD_REQUEST_CODE = 0;
    private final int SET_AS_RINGTONE_REQUEST_CODE = 1;
    private final int SET_AS_NOTIFICATION_TONE_REQUEST_CODE = 2;
    private final int SET_AS_ALARM_TONE_REQUEST_CODE = 3;

    /**
     * {@link Soundboard} for getting sound names and playing sounds.
     */
    private Soundboard soundboard;

    /**
     * {@link RecyclerView} for displaying an item view for each sound name.
     */
    private RecyclerView soundRecyclerView;

    /**
     * {@link String} sound name that opened the context menu in this {@link MainActivity}.
     */
    private String selectedSoundName;

    /**
     * Index of the sound that opened the context menu in this {@link MainActivity}.
     */
    private int selectedSoundIndex;

    /**
     * Invoked when this {@link MainActivity} is initially starting. It simply initializes the
     * member variables of this class. It also initializes the ad banner.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize global Soundboard class reference.
        soundboard = Soundboard.getInstance(this);

        // Initialize RecyclerView.
        soundRecyclerView = findViewById(R.id.sound_recycler_view);

        // Initialize Google Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        // Create ad banner programmatically.
        AdView adView = new AdView(this);
        adView.setAdSize(getAdSize());
        adView.setAdUnitId(BuildConfig.MAIN_ACTIVITY_AD_UNIT_ID);

        // Add ad banner to ad container.
        FrameLayout adContainer = findViewById(R.id.ad_container);
        adContainer.addView(adView);

        // Load ads for ad banner.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    /**
     * Invoked directly before the UI of this {@link MainActivity} comes into the foreground. It
     * sets up {@link #soundRecyclerView} to display the sound names available to play from
     * {@link #soundboard}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Specify the sort order of the sounds in soundboard.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String orderByPreferenceValue = sharedPreferences.getString(getString(R.string.order_by_key), getString(R.string.order_by_default_value));
        soundboard.setSortOrder(orderByPreferenceValue, this);

        // Define the layout manager of soundRecyclerView.
        soundRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));

        // Get sound names and display them in soundRecyclerView.
        List<String> soundNames = soundboard.getSoundNames();
        SoundAdapter soundAdapter = new SoundAdapter(soundNames);
        soundRecyclerView.setAdapter(soundAdapter);
    }

    /**
     * Invoked when this {@link MainActivity} creates its app bar menu. It simply passes the
     * appropriate menu resource for the app bar.
     *
     * @param menu {@link Menu} where the app bar menu should be inflated.
     * @return Whether the app bar should be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu_main, menu);
        return true;
    }

    /**
     * Invoked when an app bar action button is selected.
     *
     * @param item {@link MenuItem} that is invoking this method.
     * @return False to allow normal processing to proceed. True to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // When "Settings" is selected, start SettingsActivity.
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Invoked when a context menu is built in this {@link MainActivity}. It displays a context
     * menu with options to perform on a selected sound.
     *
     * @param menu     The context menu being built.
     * @param v        The view for which the context menu is being built.
     * @param menuInfo Extra information about the item for which the context menu should be shown.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.sound_options_dialog_label, selectedSoundName));
        getMenuInflater().inflate(R.menu.context_menu_main, menu);
    }

    /**
     * Invoked when an item in the context menu is selected in this {@link MainActivity}.
     *
     * @param item The context menu item being selected.
     * @return False to allow normal context menu processing to proceed. True to consume it here.
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        /* If "Download" is selected, download the sound that opened the context menu to the
         * device's downloads directory. */
        if (item.getItemId() == R.id.action_download) {

            // Requires no permissions on Android 10+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Perform action.
                downloadSound();
            }

            // Requires write external storage permission on Android 9-.
            else {
                if (checkDangerousPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        R.string.download_rationale_dialog_message,
                        DOWNLOAD_REQUEST_CODE)) {
                    // Perform action.
                    downloadSound();
                } else {
                    // Show permission required snackbar.
                    Snackbar.make(soundRecyclerView,
                            R.string.write_external_storage_required_message,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            return true;
        }

        /* If "Set as Ringtone" is selected, set the sound that opened the context menu as the
         * device's ringtone. */
        if (item.getItemId() == R.id.action_set_as_ringtone) {

            // Requires write settings permission on Android 10+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (checkWriteSettingsPermission(R.string.set_as_ringtone_write_settings_rationale_dialog_message)) {
                    // Perform action.
                    setAsRingtone();
                } else {
                    // Show permission required snackbar.
                    Snackbar.make(soundRecyclerView, R.string.write_settings_required_message,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            // Requires write external storage and write settings permissions on Android 9-.
            else {
                if (checkDangerousPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        R.string.set_as_ringtone_write_external_storage_rationale_dialog_message,
                        SET_AS_RINGTONE_REQUEST_CODE)
                        && checkWriteSettingsPermission(R.string.set_as_ringtone_write_settings_rationale_dialog_message)) {
                    // Perform action.
                    setAsRingtone();
                } else {
                    // Show permission required snackbar.
                    Snackbar.make(soundRecyclerView,
                            R.string.write_external_storage_and_write_settings_required_message,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            return true;
        }

        /* If "Set as Notification Sound" is selected, set the sound that opened the context menu as
         * the device's notification tone. */
        if (item.getItemId() == R.id.action_set_as_notification_tone) {

            // Requires write settings permission on Android 10+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (checkWriteSettingsPermission(R.string.set_as_notification_tone_write_settings_rationale_dialog_message)) {
                    // Perform action.
                    setAsNotificationTone();
                } else {
                    // Show permission required snackbar.
                    Snackbar.make(soundRecyclerView, R.string.write_settings_required_message,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            // Requires write external storage and write settings permissions on Android 9-.
            else {
                if (checkDangerousPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        R.string.set_as_notification_tone_write_external_storage_rationale_dialog_message,
                        SET_AS_NOTIFICATION_TONE_REQUEST_CODE)
                        && checkWriteSettingsPermission(R.string.set_as_notification_tone_write_settings_rationale_dialog_message)) {
                    // Perform action.
                    setAsNotificationTone();
                } else {
                    // Show permission required snackbar.
                    Snackbar.make(soundRecyclerView,
                            R.string.write_external_storage_and_write_settings_required_message,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            return true;
        }

        /* If "Set as Alarm Sound" is selected, set the sound that opened the context menu as the
         * device's alarm tone. */
        if (item.getItemId() == R.id.action_set_as_alarm_tone) {

            // Requires write settings permission on Android 10+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (checkWriteSettingsPermission(R.string.set_as_alarm_tone_write_settings_rationale_dialog_message)) {
                    // Perform action.
                    setAsAlarmTone();
                } else {
                    // Show permission required snackbar.
                    Snackbar.make(soundRecyclerView, R.string.write_settings_required_message,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            // Requires write external storage and write settings permissions on Android 9-.
            else {
                if (checkDangerousPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        R.string.set_as_alarm_tone_write_external_storage_rationale_dialog_message,
                        SET_AS_ALARM_TONE_REQUEST_CODE)
                        && checkWriteSettingsPermission(R.string.set_as_alarm_tone_write_settings_rationale_dialog_message)) {
                    // Perform action.
                    setAsAlarmTone();
                } else {
                    // Show permission required snackbar.
                    Snackbar.make(soundRecyclerView,
                            R.string.write_external_storage_and_write_settings_required_message,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            return true;
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Invoked after a {@link #requestPermissions(String[], int)} call in this {@link MainActivity}.
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /* For a result that requested permissions to download a sound, complete the download
         * operation. */
        if (requestCode == DOWNLOAD_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadSound();
        }

        /* For a result that requested permissions to set a ringtone, complete the ringtone set
         * operation. */
        else if (requestCode == SET_AS_RINGTONE_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && checkWriteSettingsPermission(R.string.set_as_ringtone_write_settings_rationale_dialog_message)) {
            setAsRingtone();
        }

        /* For a result that requested permissions to set a notification tone, complete the
         * notification tone set operation. */
        else if (requestCode == SET_AS_NOTIFICATION_TONE_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && checkWriteSettingsPermission(R.string.set_as_notification_tone_write_settings_rationale_dialog_message)) {
            setAsNotificationTone();
        }

        /* For a result that requested permissions to set an alarm tone, complete the alarm tone set
         * operation. */
        else if (requestCode == SET_AS_ALARM_TONE_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && checkWriteSettingsPermission(R.string.set_as_alarm_tone_write_settings_rationale_dialog_message)) {
            setAsAlarmTone();
        }
    }

    /**
     * Returns true if the specified dangerous permission has been granted. If false, then a
     * rationale {@link AlertDialog} is shown that explains why this permission is required.
     * Clicking "OK" on the dialog then requests the permission. This method should be used to check
     * for permissions of dangerous protection level only.
     *
     * @param permission         {@link String} name of the permission being checked.
     * @param rationaleMessageId Int id of the string resource to be shown in the rationale
     *                           {@link AlertDialog}. It should explain why this permission is
     *                           required.
     * @param requestCode        Int request code to use for
     *                           {@link #requestPermissions(String[], int)} calls.
     * @return True if the specified permission has already been granted. Will not return true for
     * a permission granted through this method's call.
     */
    private boolean checkDangerousPermission(String permission, int rationaleMessageId, int requestCode) {

        // Return early for non-dangerous protection level permissions.
        try {
            if (getPackageManager().getPermissionInfo(permission, 0).protectionLevel
                    != PermissionInfo.PROTECTION_DANGEROUS) {
                Log.e(LOG_TAG, "Permission " + permission + " is not a dangerous protection level permission.");
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Cannot verify that permission " + permission + " is a dangerous protection level permission.", e);
            return false;
        }

        // Check if permission is already granted.
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission has not been granted. Check if a rationale AlertDialog should be shown.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                // Show rationale AlertDialog explaining why this permission is needed.
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Request permission again.
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{permission},
                                requestCode);
                    }
                };
                showRequestPermissionRationaleDialog(rationaleMessageId, listener);
            }

            // Do not show rationale AlertDialog. Just request permission again.
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission},
                        requestCode);
            }

            return false;
        }

        return true;
    }

    /**
     * Returns true if the {@link Manifest.permission#WRITE_SETTINGS} permission has been granted.
     * If false, then a rationale {@link AlertDialog} is shown that explains why this permission is
     * required. Clicking "OK" on the dialog requests the permission.
     *
     * @param rationaleMessageId String resource to show in the request permission rationale dialog
     *                           if this permission has not yet been granted. It should explain why
     *                           this app needs the permission in question.
     */
    private boolean checkWriteSettingsPermission(int rationaleMessageId) {
        // Return true if the permission is already granted.
        if (Settings.System.canWrite(this)) {
            return true;
        }
        // Show a rationale AlertDialog and request the permission if not already granted.
        else {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            };
            showRequestPermissionRationaleDialog(rationaleMessageId, listener);
            return false;
        }
    }

    /**
     * Shows a rationale {@link AlertDialog} that explains why a particular permission must be
     * granted to this app.
     *
     * @param rationaleMessageId String resource to be shown in the message field of the
     *                           {@link AlertDialog}. Should explain why this app needs the
     *                           permission in question.
     * @param listener           {@link DialogInterface.OnClickListener} to execute when the
     *                           positive button of the {@link AlertDialog} is clicked. Should
     *                           contain code to request the permission in question.
     */
    private void showRequestPermissionRationaleDialog(int rationaleMessageId,
                                                      DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.rationale_dialog_title)
                .setMessage(rationaleMessageId)
                .setPositiveButton(android.R.string.ok, listener)
                .create()
                .show();
    }

    /**
     * Copies the audio resource associated with the sound that most recently opened a context menu
     * to the downloads directory on the device's external storage.
     */
    private void downloadSound() {

        boolean downloadSoundResult = soundboard.downloadSound(selectedSoundIndex);

        // Show result status snackbar.
        if (downloadSoundResult) {
            Snackbar.make(soundRecyclerView, R.string.download_success_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(soundRecyclerView, R.string.download_fail_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    /**
     * Copies the audio resource associated with the sound that most recently opened a context menu
     * to the ringtones directory on the device's external storage. Then, it sets it as the device's
     * ringtone.
     */
    private void setAsRingtone() {

        boolean result = soundboard.setAsTone(selectedSoundIndex, RingtoneManager.TYPE_RINGTONE,
                this);

        // Show result status snackbar.
        if (result) {
            Snackbar.make(soundRecyclerView, R.string.set_as_ringtone_success_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(soundRecyclerView, R.string.set_as_ringtone_fail_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    /**
     * Copies the audio resource associated with the sound that most recently opened a context menu
     * to the notifications directory on the device's external storage. Then, it sets it as the
     * device's notification tone.
     */
    private void setAsNotificationTone() {

        boolean result = soundboard.setAsTone(selectedSoundIndex, RingtoneManager.TYPE_NOTIFICATION,
                this);

        // Show result status snackbar.
        if (result) {
            Snackbar.make(soundRecyclerView, R.string.set_as_notification_tone_success_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(soundRecyclerView, R.string.set_as_notification_tone_fail_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    /**
     * Copies the audio resource associated with the sound that most recently opened a context menu
     * to the alarms directory on the device's external storage. Then, it sets it as the device's
     * alarm tone.
     */
    private void setAsAlarmTone() {

        boolean result = soundboard.setAsTone(selectedSoundIndex, RingtoneManager.TYPE_ALARM,
                this);

        // Show result status snackbar.
        if (result) {
            Snackbar.make(soundRecyclerView, R.string.set_as_alarm_tone_success_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(soundRecyclerView, R.string.set_as_alarm_tone_fail_message,
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    /**
     * Returns an {@link AdSize} to set for {@link AdView#setAdSize(AdSize)} such that the banner
     * will take up the entire width of the device's screen.
     *
     * @return An {@link AdSize} to set on an {@link AdView}.
     */
    private AdSize getAdSize() {

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    /**
     * {@link SoundHolder} is a model class that describes a single sound item view and metadata
     * about its place within a {@link RecyclerView}.
     */
    private class SoundHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        /**
         * {@link ImageButton} for playing the sound.
         */
        private ImageButton playSoundImageButton;

        /**
         * {@link TextView} to display the sound name.
         */
        private TextView soundNameTextView;

        /**
         * Int index of this {@link SoundHolder} in the {@link RecyclerView}.
         */
        private int index;

        /**
         * Constructs a new {@link SoundHolder}.
         *
         * @param inflater For inflating layouts.
         * @param parent   Parent {@link ViewGroup} of the {@link RecyclerView}.
         */
        public SoundHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));
            playSoundImageButton = itemView.findViewById(R.id.play_sound_image_button);
            soundNameTextView = itemView.findViewById(R.id.sound_name_text_view);
            playSoundImageButton.setOnClickListener(this);
            playSoundImageButton.setOnLongClickListener(this);
            registerForContextMenu(playSoundImageButton);
        }

        /**
         * Binds a sound from {@link Soundboard} to this {@link SoundHolder}.
         *
         * @param soundName {@link String} sound name to bind to this {@link SoundHolder}.
         * @param index     Int index of this {@link SoundHolder} in the {@link RecyclerView}.
         */
        public void bind(String soundName, int index) {
            playSoundImageButton.setContentDescription(getString(R.string.play_sound_content_description, soundName));
            soundNameTextView.setText(soundName);
            this.index = index;
        }

        /**
         * Invoked when {@link #playSoundImageButton} is clicked. It plays this
         * {@link SoundHolder}'s associated sound using {@link #soundboard}.
         */
        @Override
        public void onClick(View view) {
            soundboard.playSound(index);
        }

        /**
         * Invoked when {@link #playSoundImageButton} is long clicked. It opens a context menu for
         * this {@link SoundHolder}'s associated sound.
         *
         * @return Whether the click is handled in this callback.
         */
        @Override
        public boolean onLongClick(View view) {
            selectedSoundName = soundNameTextView.getText().toString();
            selectedSoundIndex = index;
            openContextMenu(view);
            return true;
        }
    }

    /**
     * {@link SoundAdapter} provides a binding from a {@link List} of {@link String} sound names to
     * a {@link RecyclerView}.
     */
    private class SoundAdapter extends RecyclerView.Adapter<SoundHolder> {

        /**
         * {@link List} of {@link String} sound names to adapt.
         */
        private List<String> soundNamesList;

        /**
         * Constructs a new {@link SoundAdapter}.
         *
         * @param soundNamesList {@link List} of {@link String} sound names to adapt.
         */
        public SoundAdapter(@NonNull List<String> soundNamesList) {
            this.soundNamesList = soundNamesList;
        }

        /**
         * Callback method invoked when the {@link RecyclerView} needs a new empty
         * {@link SoundHolder} to represent a sound name.
         *
         * @param parent   {@link ViewGroup} into which the new {@link View} will be added after it
         *                 is bound to an adapter position.
         * @param viewType The view type of the new {@link View}.
         * @return A new {@link SoundHolder}.
         */
        @NonNull
        @Override
        public SoundHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            return new SoundHolder(layoutInflater, parent);
        }

        /**
         * Callback method invoked when the {@link RecyclerView} needs to bind data to a
         * {@link SoundHolder} at a certain position index.
         *
         * @param holder   {@link SoundHolder} to be bound.
         * @param position The {@link SoundHolder}'s position index in the adapter.
         */
        @Override
        public void onBindViewHolder(@NonNull SoundHolder holder, int position) {
            holder.bind(soundNamesList.get(position), position);
        }

        /**
         * Returns the total number of items this {@link SoundAdapter} is adapting.
         *
         * @return The total number of items this {@link SoundAdapter} is adapting.
         */
        @Override
        public int getItemCount() {
            return soundNamesList.size();
        }
    }
}