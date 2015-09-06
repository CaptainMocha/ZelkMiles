package com.aktsa.zelkmiles;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aktsa.zelkmiles.ImgurAPI.ImageResponse;
import com.aktsa.zelkmiles.ImgurAPI.Upload;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ImageFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = ImageFragment.class.getSimpleName();

    private static final int CHOOSE = 1;
    private static final int TAKE = 2;
    private static final int URL = 3;

    public static final int FILE_PICK = 1001;
    public static final int REQUEST_IMAGE_CAPTURE = 1234;

    private Spinner imageTypeSpinner;
    private LinearLayout imageViewContainer;
    private ImageView imageView;
    private LinearLayout linearUrl;
    private EditText urlText;
    private EditText messageText;
    private ProgressBar progressBar;
    private TextView errorText;

    private Upload upload;
    private File chosenFile;
    private static final String path = Environment.getExternalStorageDirectory().getPath();
    private Uri imageUri;

    private String lastUrl;

    public ImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_image, container, false);
        initializeViews(view);

        return view;
    }

    private void initializeViews(View v) {
        imageTypeSpinner = (Spinner) v.findViewById(R.id.image_type);
        imageView = (ImageView) v.findViewById(R.id.image);
        urlText = (EditText) v.findViewById(R.id.url);
        messageText = (EditText) v.findViewById(R.id.message);
        imageViewContainer = (LinearLayout) v.findViewById(R.id.linear_image);
        linearUrl = (LinearLayout) v.findViewById(R.id.linear_url);
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        errorText = (TextView) v.findViewById(R.id.error_text);

        urlText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String urlTextString = urlText.getText().toString();
                    if (!Objects.equals(lastUrl, urlTextString)) {
                        lastUrl = urlTextString;
                        progressBar.setVisibility(View.VISIBLE);
                        errorText.setVisibility(View.GONE);
                        imageViewContainer.setVisibility(View.VISIBLE);
                        Glide.with(ImageFragment.this)
                                .load(urlTextString)
                                .into(new GlideDrawableImageViewTarget(imageView) {
                                    @Override
                                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                        super.onResourceReady(resource, animation);
                                        progressBar.setVisibility(View.GONE);
                                        errorText.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                        super.onLoadFailed(e, errorDrawable);
                                        progressBar.setVisibility(View.GONE);
                                        errorText.setVisibility(View.VISIBLE);
                                    }
                                });
                    }
                }
            }
        });

        imageTypeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FILE_PICK) {
                Uri returnUri;

                returnUri = data.getData();
                String filePath = DocumentHelper.getPath(getActivity(), returnUri);
                //Safety check to prevent null pointer exception
                if (filePath == null || filePath.isEmpty()) {
                    Log.d(TAG, "File path is null/empty");
                    return;
                }
                chosenFile = new File(filePath);

                imageViewContainer.setVisibility(View.VISIBLE);
                Glide.with(getActivity())
                        .load(chosenFile)
                        .into(imageView);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                chosenFile = new File(imageUri.getPath());

                imageViewContainer.setVisibility(View.VISIBLE);
                Glide.with(getActivity())
                        .load(chosenFile)
                        .into(imageView);
            }
        }
    }

    public void uploadImage() {
        CommonModelClass commonModelClass = CommonModelClass.getSingletonObject();
        Activity activity = commonModelClass.getMainActivity();

        ((MainActivity) activity).setSavedMessage(messageText.getText().toString());

        if (chosenFile == null) {
            Log.d(TAG, "Chosen File is null");
            return;
        }
        createUpload(chosenFile);

        new UploadService(getActivity()).Execute(upload, new UiCallback());

        getActivity().finish();
    }

    private void createUpload(File image) {
        upload = new Upload();

        upload.image = image;
        upload.title = null;
        upload.description = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case CHOOSE:
                linearUrl.setVisibility(View.GONE);
                chooseFileIntent();
                break;
            case TAKE:
                linearUrl.setVisibility(View.GONE);
                takePictureIntent();
                break;
            case URL:
                linearUrl.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void chooseFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, FILE_PICK);
    }

    private void takePictureIntent() {
        File file = new File(path, "ZelkMilesPhoto.jpg");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageUri = Uri.fromFile(file);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getActivity(), "Package manager is null", Toast.LENGTH_SHORT).show();
        }
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response) {
            String url = imageResponse.data.link;

            CommonModelClass commonModelClass = CommonModelClass.getSingletonObject();
            Activity activity = commonModelClass.getMainActivity();

            ((MainActivity) activity).sendImageWorkout(url);
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                Snackbar.make(messageText, "No internet connection", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
