package com.aktsa.zelkmiles;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.aktsa.zelkmiles.DailymileAPI.DailymileService;
import com.aktsa.zelkmiles.DailymileAPI.Distance;
import com.aktsa.zelkmiles.DailymileAPI.PostEntry;
import com.aktsa.zelkmiles.DailymileAPI.Workout;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit.RestAdapter;

public class WorkoutFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Spinner activityTypeSpinner;
    private EditText titleText;
    private EditText distanceText;
    private EditText hourText;
    private EditText minuteText;
    private EditText secondText;
    private EditText messageText;
    private EditText caloriesText;
    private Spinner distanceUnitsSpinner;
    private Spinner feltSpinner;
    private TextView dateText;
    private TextView timeText;

    private DailymileService service;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Constants.API_ENDPOINT)
                .build();
        service = restAdapter.create(DailymileService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        initializeViews(view);

        return view;
    }

    private void initializeViews(View v) {
        activityTypeSpinner = (Spinner) v.findViewById(R.id.activity_type);
        titleText = (EditText) v.findViewById(R.id.title);
        distanceText = (EditText) v.findViewById(R.id.distance);
        hourText = (EditText) v.findViewById(R.id.hour);
        minuteText = (EditText) v.findViewById(R.id.minute);
        secondText = (EditText) v.findViewById(R.id.second);
        messageText = (EditText) v.findViewById(R.id.message);
        caloriesText = (EditText) v.findViewById(R.id.calories);
        distanceUnitsSpinner = (Spinner) v.findViewById(R.id.distance_units);
        feltSpinner = (Spinner) v.findViewById(R.id.felt);
        dateText = (TextView) v.findViewById(R.id.date);
        timeText = (TextView) v.findViewById(R.id.time);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        String date = dateFormat.format(System.currentTimeMillis());
        String time = timeFormat.format(System.currentTimeMillis());
        dateText.setText(date);
        timeText.setText(time);

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        WorkoutFragment.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        WorkoutFragment.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        String minuteString;
        if (minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = String.valueOf(minute);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.US);
        Date time;
        try {
            time = sdf.parse(hourOfDay + ":" + minuteString);
        } catch (ParseException e) {
            e.printStackTrace();
            time = new Date();
        }

        SimpleDateFormat output = new SimpleDateFormat("h:mm a", Locale.US);

        timeText.setText(output.format(time));
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar newDate = Calendar.getInstance();
        newDate.set(year, monthOfYear, dayOfMonth);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);

        dateText.setText(dateFormat.format(newDate.getTime()));
    }

    public void sendWorkout() {
        String title = titleText.getText().toString();
        String message = messageText.getText().toString();
        String activityType = activityTypeSpinner.getSelectedItem().toString();
        String dateCompleted = dateText.getText().toString();
        String timeCompleted = timeText.getText().toString();
        int hourDuration, minuteDuration, secondDuration;
        try {
            hourDuration = Integer.valueOf(hourText.getText().toString());
        } catch (NumberFormatException e) {
            hourDuration = 0;
        }
        try {
            minuteDuration = Integer.valueOf(minuteText.getText().toString());
        } catch (NumberFormatException e) {
            minuteDuration = 0;
        }
        try {
            secondDuration = Integer.valueOf(secondText.getText().toString());
        } catch (NumberFormatException e) {
            secondDuration = 0;
        }
        float distanceValue;
        try {
            distanceValue = Float.valueOf(distanceText.getText().toString());
        } catch (NumberFormatException e) {
            distanceValue = 0;
        }
        String distanceUnits = distanceUnitsSpinner.getSelectedItem().toString();
        String felt = feltSpinner.getSelectedItem().toString().toLowerCase();
        if (felt.equals("i felt…")) {
            felt = null;
        }
        int calories;
        try {
            calories = Integer.valueOf(caloriesText.getText().toString());
        } catch (NumberFormatException e) {
            calories = 0;
        }

        int totalDuration = hourDuration * 3600 + minuteDuration * 60 + secondDuration;
        SimpleDateFormat completedFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        completedFormat.setTimeZone(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date completedDate = null;
        try {
            completedDate = completedFormat.parse(dateCompleted + " " + timeCompleted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String completedAt = sdf.format(completedDate);

        Distance distance = new Distance();
        switch (distanceUnits) {
            case "mi":
                distanceUnits = "miles";
                break;
            case "km":
                distanceUnits = "kilometers";
                break;
            case "yd":
                distanceUnits = "yards";
                break;
            case "m":
                distanceUnits = "meters";
                break;
            default:
                distanceUnits = "miles";
                break;
        }
        distance.setUnits(distanceUnits);
        distance.setValue(distanceValue);

        Workout workout = new Workout();
        workout.setActivity_type(activityType);
        workout.setCompleted_at(completedAt);
        workout.setDistance(distance);
        workout.setDuration(totalDuration);
        workout.setFelt(felt);
        workout.setCalories(calories);
        workout.setTitle(title);

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        String oauth_key = prefs.getString("oauth_token", null);

        PostEntry entry = new PostEntry();
        entry.setMessage(message);
        entry.setWorkout(workout);
        entry.setMedia(null);

        CommonModelClass commonModelClass = CommonModelClass.getSingletonObject();
        Activity activity = commonModelClass.getMainActivity();

        service.postWorkout(entry, oauth_key, (MainActivity) activity);

        getActivity().finish();
    }

}
