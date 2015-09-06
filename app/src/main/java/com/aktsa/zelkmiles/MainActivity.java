package com.aktsa.zelkmiles;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aktsa.zelkmiles.DailymileAPI.Content;
import com.aktsa.zelkmiles.DailymileAPI.DailymileService;
import com.aktsa.zelkmiles.DailymileAPI.Entry;
import com.aktsa.zelkmiles.DailymileAPI.PostEntry;
import com.aktsa.zelkmiles.DailymileAPI.TokenOwner;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements Callback<Entry> {

    private static final int FRIENDS = 0;
    private static final int NEARBY = 1;
    private static final int POPULAR = 2;
    private static final int ALL = 3;

    public static MainActivity instance = null;
    public DailymileService service;
    private DrawerLayout mDrawerLayout;
    private String savedMessage;

    public void setSavedMessage(String savedMessage) {
        this.savedMessage = savedMessage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonModelClass commonModelClass = CommonModelClass.getSingletonObject();
        commonModelClass.setMainActivity(MainActivity.this);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Constants.API_ENDPOINT)
                .build();
        service = restAdapter.create(DailymileService.class);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SubmitActivity.class);
                startActivity(intent);
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        String oauth_key = prefs.getString("oauth_token", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        Fragment friendsFragment = new WorkoutListFragment();
        Bundle friendsBundle = new Bundle();
        friendsBundle.putInt("type", FRIENDS);
        friendsFragment.setArguments(friendsBundle);

        Fragment nearbyFragment = new WorkoutListFragment();
        Bundle nearbyBundle = new Bundle();
        nearbyBundle.putInt("type", NEARBY);
        nearbyFragment.setArguments(nearbyBundle);

        Fragment popularFragment = new WorkoutListFragment();
        Bundle popularBundle = new Bundle();
        popularBundle.putInt("type", POPULAR);
        popularFragment.setArguments(popularBundle);

        Fragment allFragment = new WorkoutListFragment();
        Bundle allBundle = new Bundle();
        allBundle.putInt("type", ALL);
        allFragment.setArguments(allBundle);

        adapter.addFragment(friendsFragment, "Friends");
        adapter.addFragment(nearbyFragment, "Nearby");
        adapter.addFragment(popularFragment, "Popular");
        adapter.addFragment(allFragment, "All");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount());
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.nav_messages:

                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    public void showSignInRequiredDialog(final Activity activity, String title, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (title != null) {
            builder.setTitle(title);
        }

        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(activity, OAuthToken.class);
                startActivityForResult(intent, 1);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        instance = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        instance = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        finish();
        startActivity(getIntent());
    }

    @Override
    public void success(Entry entry, Response response) {
        final String url = entry.getUrl();
        Snackbar.make(findViewById(R.id.fab), "Post Successful!", Snackbar.LENGTH_LONG)
                .setAction("View", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    }
                }).show();
    }

    @Override
    public void failure(RetrofitError error) {
        Snackbar.make(findViewById(R.id.fab), "Post Failed", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void sendImageWorkout(String url) {
        Content media = new Content();
        media.setType("image");
        media.setUrl(url);

        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        String oauth_key = prefs.getString("oauth_token", null);

        PostEntry entry = new PostEntry();
        entry.setMessage(savedMessage);
        entry.setWorkout(null);
        entry.setMedia(media);

        CommonModelClass commonModelClass = CommonModelClass.getSingletonObject();
        Activity activity = commonModelClass.getMainActivity();

        service.postWorkout(entry, oauth_key, (MainActivity) activity);
    }

    class Adapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments = new ArrayList<>();
        private List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    private class GetSelfInfo extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String oauth_token = params[0];
            service.getSelf(oauth_token, new Callback<TokenOwner>() {
                @Override
                public void success(TokenOwner tokenOwner, Response response) {
                    String photoUrl = tokenOwner.getPhotoUrl();

                    Glide.with(getApplicationContext()).load(photoUrl).into((CircleImageView) findViewById(R.id.header_image));
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
            return null;
        }
    }
}