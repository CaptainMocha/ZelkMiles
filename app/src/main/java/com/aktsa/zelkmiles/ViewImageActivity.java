package com.aktsa.zelkmiles;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class ViewImageActivity extends AppCompatActivity {

    private static final String TAG = ViewImageActivity.class.getSimpleName();

    private SubsamplingScaleImageView imageView;
    private ProgressBar progressBar;
    private Context context;
    private String url;
    private Integer width;
    private Integer height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setHomeAsUpIndicator(R.drawable.ic_action_close);
        ab.setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);

        progressBar.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);

        context = getApplicationContext();

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        width = intent.getIntExtra("width", 0);
        height = intent.getIntExtra("height", 0);

        new GetImage().execute();
    }

    private class GetImage extends AsyncTask<Void, Void, File> {

        @Override
        protected File doInBackground(Void... params) {
            FutureTarget<File> future = Glide.with(context)
                    .load(url)
                    .downloadOnly(width, height);
            File cacheFile = null;
            try {
                cacheFile = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return cacheFile;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
//            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            Uri uri = Uri.fromFile(file);

            imageView.setImage(ImageSource.uri(uri));
        }
    }
}
