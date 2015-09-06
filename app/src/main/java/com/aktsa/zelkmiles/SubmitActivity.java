package com.aktsa.zelkmiles;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by cheek on 8/16/2015.
 */
public class SubmitActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = SubmitActivity.class.getSimpleName();

    private int fragmentType = 0;

    private static final int FRAG_WORKOUT = 0;
    private static final int FRAG_NOTE = 1;
    private static final int FRAG_IMAGE = 2;

    private Spinner submissionTypeSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Submit");

        initializeViews();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            WorkoutFragment workoutFragment = new WorkoutFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, workoutFragment, "FRAG_WORKOUT").commit();
            fragmentType = FRAG_WORKOUT;
        }
    }

    private void initializeViews() {
        submissionTypeSpinner = (Spinner) findViewById(R.id.submission_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.submission_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        submissionTypeSpinner.setAdapter(adapter);
        submissionTypeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                sendWorkout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendWorkout() {
        switch (fragmentType) {
            case FRAG_WORKOUT:
                WorkoutFragment workoutFragment = (WorkoutFragment) getFragmentManager().findFragmentByTag("FRAG_WORKOUT");
                if (workoutFragment != null && workoutFragment.isVisible()) {
                    workoutFragment.sendWorkout();
                } else {
                    Toast.makeText(getApplicationContext(), "Error posting workout", Toast.LENGTH_SHORT).show();
                }
                break;
            case FRAG_NOTE:
                NoteFragment noteFragment = (NoteFragment) getFragmentManager().findFragmentByTag("FRAG_NOTE");
                if (noteFragment != null && noteFragment.isVisible()) {
                    noteFragment.sendWorkout();
                } else {
                    Toast.makeText(getApplicationContext(), "Error posting note", Toast.LENGTH_SHORT).show();
                }
                break;
            case FRAG_IMAGE:
                ImageFragment imageFragment = (ImageFragment) getFragmentManager().findFragmentByTag("FRAG_IMAGE");
                if (imageFragment != null && imageFragment.isVisible()) {
                    imageFragment.uploadImage();
                } else {
                    Toast.makeText(getApplicationContext(), "Error posting image", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                if (fragmentType != FRAG_WORKOUT) {
                    WorkoutFragment workoutFragment = new WorkoutFragment();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, workoutFragment, "FRAG_WORKOUT")
                            .commit();
                    fragmentType = FRAG_WORKOUT;
                }
                break;
            case 1:
                if (fragmentType != FRAG_NOTE) {
                    NoteFragment noteFragment = new NoteFragment();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, noteFragment, "FRAG_NOTE")
                            .commit();
                    fragmentType = FRAG_NOTE;
                }
                break;
            case 2:
                if (fragmentType != FRAG_IMAGE) {
                    ImageFragment imageFragment = new ImageFragment();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, imageFragment, "FRAG_IMAGE")
                            .commit();
                    fragmentType = FRAG_IMAGE;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
