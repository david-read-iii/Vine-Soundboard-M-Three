package com.davidread.vinesoundboard;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * {@link Soundboard} is a singleton class that provides a {@link List} of sound names available for
 * playback and playback of a sound given its index in the {@link List}.
 */
public class Soundboard {

    /**
     * {@link String} tag for log messages originating from this {@link Soundboard}.
     */
    private static final String LOG_TAG = Soundboard.class.getSimpleName();

    /**
     * Static reference of this {@link Soundboard} to return in {@link #getInstance(Context)}.
     */
    private static Soundboard soundboard;

    /**
     * {@link SoundPool} for retrieving audio resources and playing them.
     */
    private SoundPool soundPool;

    /**
     * {@link List} of {@link Sound}s accessible in this soundboard.
     */
    private List<Sound> soundList;

    /**
     * {@link String} specifying what sort order should be applied to {@link #soundList}. Possible
     * values are at {@link R.array#order_by_values}.
     */
    private String sortOrder;

    /**
     * {@link AssetManager} for accessing audio asset files.
     */
    private AssetManager assetManager;

    /**
     * Constructs a new {@link Soundboard}.
     *
     * @param context {@link Context} for getting audio resources.
     */
    private Soundboard(Context context) {

        // Setup the SoundPool.
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();

        // Get String array of audio asset file names.
        assetManager = context.getAssets();
        String[] assetFileNames = null;
        try {
            assetFileNames = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize soundList with a Sound for each audio asset file.
        soundList = new ArrayList<>();
        if (assetFileNames != null) {
            for (String assetFileName : assetFileNames) {
                try {
                    AssetFileDescriptor assetFileDescriptor = assetManager.openFd(assetFileName);
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(
                            assetFileDescriptor.getFileDescriptor(),
                            assetFileDescriptor.getStartOffset(),
                            assetFileDescriptor.getLength()
                    );
                    Sound sound = new Sound(
                            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                            assetFileName,
                            soundPool.load(assetFileDescriptor, 1)
                    );
                    soundList.add(sound);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Set default sorting of soundList.
        setSortOrder(context.getString(R.string.order_by_default_value), context);
    }

    /**
     * Returns an instance of {@link Soundboard}.
     *
     * @param context {@link Context} for getting audio resources.
     * @return An instance of {@link Soundboard}.
     */
    public static Soundboard getInstance(Context context) {
        if (soundboard == null) {
            soundboard = new Soundboard(context);
        }
        return soundboard;
    }

    /**
     * Returns a {@link List} of {@link String}s where each {@link String} is a name of a sound that
     * may be played.
     *
     * @return A {@link List} of {@link String}s.
     */
    public List<String> getSoundNames() {
        List<String> names = new ArrayList<>();
        for (Sound sound : soundList) {
            names.add(sound.getName());
        }
        return names;
    }

    /**
     * Uses {@link #soundPool} to play the sound in {@link #soundList} at the specified index.
     *
     * @param index The index of the sound in {@link #soundList} to play.
     */
    public void playSound(int index) {
        soundPool.play(soundList.get(index).getSoundPoolId(), 1, 1, 1, 0, 1);
    }

    /**
     * Sets the sort order of {@link Sound}s in {@link #soundList}.
     *
     * @param sortOrder {@link String} specifying what sort order to apply.
     * @param context   {@link Context} for getting string resources.
     */
    public void setSortOrder(@NonNull String sortOrder, @NonNull Context context) {

        // Do nothing if this sort order is already applied.
        if (this.sortOrder != null && this.sortOrder.equals(sortOrder)) {
            return;
        }

        // Apply the specified sort order.
        if (sortOrder.equals(context.getResources().getStringArray(R.array.order_by_values)[0])) {
            // Apply alphabetical ascending sort order.
            soundList.sort(new Comparator<Sound>() {
                @Override
                public int compare(Sound sound1, Sound sound2) {
                    return sound1.getName().compareToIgnoreCase(sound2.getName());
                }
            });
        } else if (sortOrder.equals(context.getResources().getStringArray(R.array.order_by_values)[1])) {
            // Apply alphabetical descending order.
            soundList.sort(new Comparator<Sound>() {
                @Override
                public int compare(Sound sound1, Sound sound2) {
                    return -sound1.getName().compareToIgnoreCase(sound2.getName());
                }
            });
        }

        // Keep track of what sort order was applied in this Soundboard.
        this.sortOrder = sortOrder;
    }

    /**
     * Copies the audio resource associated with the {@link Sound} in {@link #soundList} at the
     * specified index to the downloads directory on the device's external storage.
     *
     * @param index {@link Sound} in {@link #soundList} whose audio resource will be copied.
     * @return True if the file copy operation succeeds.
     */
    public boolean downloadSound(int index) {

        // Return false if the device's external storage is not mounted.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(LOG_TAG, "Device's external storage must be mounted to perform operation.");
            return false;
        }

        // Create a downloads directory if one doesn't already exist.
        File downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs();
        }

        /* Copy the audio asset to the downloads directory. Return false if the copy operation
         * fails. */
        String assetFileName = soundList.get(index).getAudioAssetFileName();
        File outFile = new File(downloadsDirectory, assetFileName);
        try {
            InputStream inputStream = assetManager.open(assetFileName);
            OutputStream outputStream = new FileOutputStream(outFile);
            writeToOutputStream(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to copy data from asset file " + assetFileName
                    + " to directory " + downloadsDirectory, e);
            return false;
        }

        return true;
    }

    /**
     * Copies the audio resource associated with the {@link Sound} in {@link #soundList} at the
     * specified index to the appropriate directory on the device's external storage. Then, it sets
     * that audio resource as the device's ringtone, notification tone, or alarm tone.
     *
     * @param index    {@link Sound} in {@link #soundList} whose audio resource will be copied.
     * @param toneType {@link RingtoneManager#TYPE_RINGTONE},
     *                 {@link RingtoneManager#TYPE_NOTIFICATION}, or
     *                 {@link RingtoneManager#TYPE_ALARM}.
     * @param context  {@link Context} for putting an entry in the {@link MediaStore} database.
     * @return True if the set as ringtone operation succeeds.
     */
    public boolean setAsTone(int index, int toneType, @NonNull Context context) {

        // Return false if the device's external storage is not mounted.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(LOG_TAG, "Device's external storage must be mounted to perform operation.");
            return false;
        }

        // Create an out directory if one doesn't already exist.
        File outDirectory;
        switch (toneType) {
            case RingtoneManager.TYPE_NOTIFICATION:
                outDirectory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_NOTIFICATIONS
                );
                break;
            case RingtoneManager.TYPE_ALARM:
                outDirectory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_ALARMS
                );
                break;
            default:
                outDirectory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_RINGTONES
                );
        }
        if (!outDirectory.exists()) {
            outDirectory.mkdirs();
        }

        // Copy the audio asset to the out directory. Return false if the copy operation fails.
        String outFileName = soundList.get(index).getAudioAssetFileName();
        File outFile = new File(outDirectory, outFileName);
        try {
            InputStream inputStream = assetManager.open(outFileName);
            OutputStream outputStream = new FileOutputStream(outFile);
            writeToOutputStream(inputStream, outputStream);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to copy data from asset file " + outFileName
                    + " to directory " + outDirectory, e);
            return false;
        }

        // ContentValues for the required MediaStore listing.
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.TITLE, soundList.get(index).getName());
        values.put(MediaStore.MediaColumns.SIZE, outFile.length());
        values.put(MediaStore.MediaColumns.MIME_TYPE, getMIMEType(outFile.getAbsolutePath()));

        switch (toneType) {
            case RingtoneManager.TYPE_NOTIFICATION:
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                break;
            case RingtoneManager.TYPE_ALARM:
                values.put(MediaStore.Audio.Media.IS_ALARM, true);
                break;
            default:
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            // Android 10+ exclusive MediaStore ContentValues.
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, outDirectory.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, outFile.getName());

            /* Android 10+ requires the tone to be copied to an OutputStream they provide given
             * the MediaStore listing. MediaStore listing requires a File to get attributes from.
             * This is why we copy-delete-copy the file from assets twice.  */
            outFile.delete();

            Uri newUri = context.getContentResolver().insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

            try {
                InputStream inputStream = assetManager.open(outFileName);
                OutputStream outputStream = context.getContentResolver().openOutputStream(newUri);
                writeToOutputStream(inputStream, outputStream);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to copy data from asset file " + outFileName
                        + " to directory " + outDirectory, e);
                return false;
            }

            RingtoneManager.setActualDefaultRingtoneUri(context, toneType, newUri);

        } else {

            // Android 9- exclusive MediaStore ContentValues.
            values.put(MediaStore.MediaColumns.DATA, outFile.getAbsolutePath());

            // Android 9- way to set the tone.
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(outFile.getAbsolutePath());
            context.getContentResolver().delete(uri,
                    MediaStore.MediaColumns.DATA + "=\"" + outFile.getAbsolutePath() + "\"",
                    null);
            Uri newUri = context.getContentResolver().insert(uri, values);

            RingtoneManager.setActualDefaultRingtoneUri(context, toneType, newUri);
        }

        return true;
    }

    /**
     * Returns the MIME type of the given {@link String} URL. Will return null for URLs with no
     * MIME type.
     *
     * @param url {@link String} URL.
     * @return The MIME type of the given {@link String} URL.
     */
    private String getMIMEType(@NonNull String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Reads data from the given {@link InputStream} and writes it to the given
     * {@link OutputStream}. It closes both streams on finish.
     *
     * @param inputStream  Open {@link InputStream} containing the data to be read from.
     * @param outputStream Open {@link OutputStream} where the data will be written to.
     * @throws IOException May be thrown by either the given {@link InputStream} or
     *                     {@link OutputStream}.
     */
    private void writeToOutputStream(@NonNull InputStream inputStream,
                                     @NonNull OutputStream outputStream) throws IOException {

        // Copy the file.
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        // Cleanup.
        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }
}
