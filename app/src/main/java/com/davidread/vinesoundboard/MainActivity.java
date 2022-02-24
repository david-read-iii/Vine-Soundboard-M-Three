package com.davidread.vinesoundboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
     * {@link String} sound name that opened the context menu in this {@link MainActivity}.
     */
    private String selectedSoundName;

    /**
     * Index of the sound that opened the context menu in this {@link MainActivity}.
     */
    private int selectedSoundIndex;

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String orderByPreferenceValue = sharedPreferences.getString(getString(R.string.order_by_key), getString(R.string.order_by_default_value));
        soundboard.setSortOrder(orderByPreferenceValue);

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
            soundboard.downloadSound(selectedSoundIndex);
            return true;
        }

        return super.onContextItemSelected(item);
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