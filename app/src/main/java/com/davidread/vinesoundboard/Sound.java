package com.davidread.vinesoundboard;

/**
 * {@link Sound} models a single sound the user may play. It simply consists of several metadata
 * used for naming the sound and identifying the audio resources used to play the sound.
 */
public class Sound {

    /**
     * {@link String} for providing a text description of this {@link Sound}.
     */
    private String name;

    /**
     * {@link String} file name identifying the audio asset resource associated with this
     * {@link Sound}.
     */
    private String audioAssetFileName;

    /**
     * Int id identifying the {@link android.media.SoundPool} sound associated with this
     * {@link Sound}.
     */
    private int soundPoolId;

    /**
     * Constructs a new {@link Sound}.
     *
     * @param name               {@link String} for providing a text description of this
     *                           {@link Sound}.
     * @param audioAssetFileName {@link String} file name identifying the audio asset resource
     *                           associated with this {@link Sound}.
     * @param soundPoolId        Int id identifying the {@link android.media.SoundPool} sound
     *                           associated with this {@link Sound}.
     */
    public Sound(String name, String audioAssetFileName, int soundPoolId) {
        this.name = name;
        this.audioAssetFileName = audioAssetFileName;
        this.soundPoolId = soundPoolId;
    }

    public String getName() {
        return name;
    }

    public String getAudioAssetFileName() {
        return audioAssetFileName;
    }

    public int getSoundPoolId() {
        return soundPoolId;
    }
}
