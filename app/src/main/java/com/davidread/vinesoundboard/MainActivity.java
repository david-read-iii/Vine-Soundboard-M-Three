package com.davidread.vinesoundboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

/**
 * {@link MainActivity} is a soundboard user interface. It has a {@link Button} for each sound
 * provided by {@link Soundboard}. Clicking a button invokes {@link Soundboard#playSound(int)} to
 * play its corresponding sound.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * {@link Soundboard} for getting sound names and playing sounds.
     */
    private Soundboard soundboard;

    /**
     * Invoked when this {@link MainActivity} is initially created. It initializes
     * {@link #soundboard} and adds a {@link Button} to the activity for each sound name provided
     * by {@link #soundboard}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize soundboard with an instance.
        soundboard = Soundboard.getInstance(this);

        // Add a Button to the buttonLayout for each sound name.
        List<String> names = soundboard.getSoundNames();
        LinearLayout buttonLayout = findViewById(R.id.button_layout);
        for (int index = 0; index < names.size(); index++) {
            Button button = new Button(this);
            button.setText(names.get(index));
            button.setTag(index);
            button.setOnClickListener(this);
            buttonLayout.addView(button);
        }
    }

    /**
     * Invoked when a {@link Button} in {@link MainActivity} is clicked. It plays the sound
     * corresponding to the clicked button.
     *
     * @param view {@link View} invoking this callback.
     */
    @Override
    public void onClick(View view) {
        int index = (int) view.getTag();
        soundboard.playSound(index);
    }
}