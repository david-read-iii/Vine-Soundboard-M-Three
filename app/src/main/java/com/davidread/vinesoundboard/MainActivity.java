package com.davidread.vinesoundboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * {@link MainActivity} is a soundboard user interface. It retrieves available sounds from
 * {@link #soundboard} and displays them in {@link #soundRecyclerView}.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * {@link Soundboard} for getting sound names and playing sounds.
     */
    private Soundboard soundboard;

    /**
     * {@link RecyclerView} for displaying an item view for each sound name.
     */
    private RecyclerView soundRecyclerView;

    /**
     * Invoked when this {@link MainActivity} is initially starting. It simply initializes the
     * member variables of this class.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        soundboard = Soundboard.getInstance(this);
        soundRecyclerView = findViewById(R.id.sound_recycler_view);
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
        soundboard.setSortOrder(Soundboard.SortOrder.ALPHABETICAL_ASCENDING);

        // Define the layout manager of soundRecyclerView.
        soundRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));

        // Get sound names and display them in soundRecyclerView.
        List<String> soundNames = soundboard.getSoundNames();
        SoundAdapter soundAdapter = new SoundAdapter(soundNames);
        soundRecyclerView.setAdapter(soundAdapter);
    }

    /**
     * {@link SoundHolder} is a model class that describes a single sound item view and metadata
     * about its place within a {@link RecyclerView}.
     */
    private class SoundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
         * Invoked when the {@link View} held by this {@link SoundHolder} is clicked. It plays this
         * {@link SoundHolder}'s associated sound using {@link #soundboard}.
         */
        @Override
        public void onClick(View view) {
            soundboard.playSound(index);
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