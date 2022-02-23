package com.davidread.vinesoundboard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * {@link Soundboard} is a singleton class that provides a {@link List} of sound names available for
 * playback and playback of a sound given its index in the {@link List}.
 */
public class Soundboard {

    /**
     * Enum for specifying the sort order of {@link Sound}s in {@link #soundList}.
     */
    public enum SortOrder {ALPHABETICAL_ASCENDING, ALPHABETICAL_DESCENDING}

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
     * {@link SortOrder} specifying what sort order should be applied to {@link #soundList}.
     */
    private SortOrder sortOrder;

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
        AssetManager assetManager = context.getAssets();
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
        setSortOrder(SortOrder.ALPHABETICAL_ASCENDING);
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
     * @param sortOrder {@link SortOrder} specifying what sort order to apply.
     */
    public void setSortOrder(@NonNull SortOrder sortOrder) {

        // Do nothing if this sort order is already applied.
        if (this.sortOrder == sortOrder) {
            return;
        }

        // Apply the specified sort order.
        if (sortOrder == SortOrder.ALPHABETICAL_ASCENDING) {
            soundList.sort(new Comparator<Sound>() {
                @Override
                public int compare(Sound sound1, Sound sound2) {
                    return sound1.getName().compareToIgnoreCase(sound2.getName());
                }
            });
        } else if (sortOrder == SortOrder.ALPHABETICAL_DESCENDING) {
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
}
