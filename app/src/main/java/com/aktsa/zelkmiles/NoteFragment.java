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

import com.aktsa.zelkmiles.DailymileAPI.DailymileService;
import com.aktsa.zelkmiles.DailymileAPI.PostEntry;

import retrofit.RestAdapter;

public class NoteFragment extends Fragment {

    private EditText messageText;

    private DailymileService service;

    public NoteFragment() {
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

        View view = inflater.inflate(R.layout.fragment_note, container, false);
        initializeViews(view);

        return view;
    }

    private void initializeViews(View v) {
        messageText = (EditText) v.findViewById(R.id.message);
    }

    public void sendWorkout() {
        String message = messageText.getText().toString();

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        String oauth_key = prefs.getString("oauth_token", null);

        PostEntry entry = new PostEntry();
        entry.setMessage(message);
        entry.setWorkout(null);
        entry.setMedia(null);

        CommonModelClass commonModelClass = CommonModelClass.getSingletonObject();
        Activity activity = commonModelClass.getMainActivity();

        service.postWorkout(entry, oauth_key, (MainActivity) activity);

        getActivity().finish();
    }

}
