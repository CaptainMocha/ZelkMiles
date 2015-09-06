package com.aktsa.zelkmiles;

/**
 * Created by cheek on 8/14/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aktsa.zelkmiles.DailymileAPI.Distance;
import com.aktsa.zelkmiles.DailymileAPI.Entry;
import com.aktsa.zelkmiles.DailymileAPI.Medium;
import com.aktsa.zelkmiles.DailymileAPI.Stream;
import com.aktsa.zelkmiles.DailymileAPI.User;
import com.aktsa.zelkmiles.DailymileAPI.Workout;
import com.bumptech.glide.Glide;
import com.google.common.base.Joiner;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkoutListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = WorkoutListFragment.class.getSimpleName();

    private static final int FRIENDS = 0;
    private static final int NEARBY = 1;
    private static final int POPULAR = 2;
    private static final int ALL = 3;

    private static final int FIRST = 4;
    private static final int RELOAD = 5;
    private static final int MORE = 6;

    private List<StreamItem> streamItems;
    private int streamType;
    private boolean loading = false;

    private SharedPreferences prefs;
    private StreamRVAdapter adapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private EndlessRecyclerOnScrollListener scrollListener;

    private float longitude;
    private float latitude;
    private LocationManager locationManager;
    private boolean locationAttained = false;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitude = (float) location.getLongitude();
            latitude = (float) location.getLatitude();
            Log.d(TAG, "Location: " + latitude + ", " + longitude);
            locationManager.removeUpdates(locationListener);
            locationAttained = true;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private static String getTimeAgo(Date date) {
        long time = date.getTime();

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < DateUtils.MINUTE_IN_MILLIS) {
            return "now";
        } else if (diff < 60 * DateUtils.MINUTE_IN_MILLIS) {
            return diff / DateUtils.MINUTE_IN_MILLIS + "m";
        } else if (diff < 24 * DateUtils.HOUR_IN_MILLIS) {
            return diff / DateUtils.HOUR_IN_MILLIS + "h";
        } else {
            return diff / DateUtils.DAY_IN_MILLIS + "d";
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        streamType = bundle.getInt("type");

        if (streamType == NEARBY) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            requestLocationUpdates();

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                latitude = (float) lastKnownLocation.getLatitude();
                longitude = (float) lastKnownLocation.getLongitude();
            }
        }

        streamItems = new ArrayList<>();
        prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SwipeRefreshLayout srl = (SwipeRefreshLayout) inflater.inflate(
                R.layout.fragment_cheese_list, container, false);

        refreshLayout = srl;

        srl.setOnRefreshListener(this);
        srl.setColorSchemeResources(R.color.accent);

        adapter = new StreamRVAdapter(getActivity(), streamItems, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Log.d(TAG, "clicked position: " + position);
                Integer id = streamItems.get(position).id;
                String username = streamItems.get(position).username;
                Log.d(TAG, "ID: " + id + ", USERNAME: " + username);
            }
        });
        mRecyclerView = (RecyclerView) srl.findViewById(R.id.recyclerview);
        setupRecyclerView(mRecyclerView);

        new RetrieveStream(1, FIRST).execute();
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        return srl;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager llm = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(llm);
        scrollListener = new EndlessRecyclerOnScrollListener(llm) {
            @Override
            public void onLoadMore(int current_page) {
                if (!loading) {
                    loading = true;
                    streamItems.add(null);
                    adapter.notifyItemInserted(streamItems.size());
                    new RetrieveStream(current_page, MORE).execute();
                    Log.d(TAG, "LOAD MORE");
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        if (streamType == NEARBY) {
            locationAttained = false;
            requestLocationUpdates();
        }

        new RetrieveStream(1, RELOAD).execute();
        scrollListener.setCurrent_page(1);
        scrollListener.setLoading(true);
        scrollListener.setPreviousTotal(0);
    }

    private void requestLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, locationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (streamType == NEARBY) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (streamType == NEARBY && !locationAttained) {
            requestLocationUpdates();
        }
    }

    public class StreamRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;

        Context mContext;
        CustomItemClickListener listener;

        List<StreamItem> streamItems;

        StreamRVAdapter(Context mContext, List<StreamItem> streamItems, CustomItemClickListener listener) {
            this.mContext = mContext;
            this.streamItems = streamItems;
            this.listener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i == VIEW_ITEM) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
                final RecyclerView.ViewHolder vh = new ViewHolder(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, vh.getAdapterPosition());
                    }
                });
                return vh;
            } else {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progressbar_item, viewGroup, false);
                return new ProgressViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder instanceof ViewHolder) {
                final StreamItem streamItem = streamItems.get(i);
                ViewHolder vh = (ViewHolder) viewHolder;

                vh.postTitle.setText(streamItem.title);
                vh.postInfo.setText(Html.fromHtml(streamItem.info));
                vh.postExtraInfo.setText(streamItem.extraInfo);
                vh.postMessage.setText(streamItem.message);
                vh.timeSince.setText(streamItem.timeSince);

                vh.postTitle.setVisibility(View.VISIBLE);
                vh.postInfo.setVisibility(View.VISIBLE);
                vh.postExtraInfo.setVisibility(View.VISIBLE);
                vh.postMessage.setVisibility(View.VISIBLE);

                if (streamItem.title == null) {
                    vh.postTitle.setVisibility(View.GONE);
                }
                if (streamItem.info == null) {
                    vh.postInfo.setVisibility(View.GONE);
                }
                if (streamItem.extraInfo.equals("")) {
                    vh.postExtraInfo.setVisibility(View.GONE);
                }
                if (streamItem.message == null) {
                    vh.postMessage.setVisibility(View.GONE);
                }
                Glide.with(getActivity()).load(streamItem.avatarUrl).into(vh.avatar);

                if (streamItem.mediaUrl != null) {
                    vh.mediaView.setVisibility(View.VISIBLE);
                    Glide.with(getActivity()).load(streamItem.mediaUrl).into(vh.mediaView);
                } else {
                    vh.mediaView.setVisibility(View.GONE);
                }

                vh.mediaView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                        Log.d(TAG, streamItem.mediaUrl);
                        intent.putExtra("url", streamItem.mediaUrl);
                        intent.putExtra("width", streamItem.width);
                        intent.putExtra("height", streamItem.height);
                        startActivity(intent);
                    }
                });
            } else {
                ((ProgressViewHolder) viewHolder).progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return streamItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        }

        @Override
        public int getItemCount() {
            return streamItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            View mView;
            TextView postTitle;
            TextView postInfo;
            TextView postExtraInfo;
            TextView postMessage;
            TextView timeSince;
            CircleImageView avatar;
            SquareImageView mediaView;

            ViewHolder(View view) {
                super(view);
                mView = view;
                postTitle = (TextView) view.findViewById(R.id.post_title);
                postInfo = (TextView) view.findViewById(R.id.post_info);
                postExtraInfo = (TextView) view.findViewById(R.id.post_extra_info);
                postMessage = (TextView) view.findViewById(R.id.post_message);
                timeSince = (TextView) view.findViewById(R.id.time_since);
                avatar = (CircleImageView) view.findViewById(R.id.avatar);
                mediaView = (SquareImageView) view.findViewById(R.id.media_view);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }
    }

    private class RetrieveStream extends AsyncTask<Void, List<Entry>, List<Entry>> {

        int page;
        int loadType;

        public RetrieveStream(int page, int loadType) {
            this.page = page;
            this.loadType = loadType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (loadType == RELOAD) {
                streamItems.clear();
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Load Type: RELOAD");
            }
        }

        @Override
        protected List<Entry> doInBackground(Void... params) {
            List<Entry> streamEntries = null;
            switch (streamType) {
                case FRIENDS:
                    String oauth_token = prefs.getString("oauth_token", null);
                    Stream stream;
                    Log.d(TAG, "Oauth Token: " + oauth_token);
                    if (oauth_token == null) {
                        cancel(true);
                        break;
                    } else {
                        stream = ((MainActivity) getActivity()).service.streamFriends(oauth_token, page);
                        streamEntries = stream.getEntries();
                    }
                    break;
                case NEARBY:
                    stream = ((MainActivity) getActivity()).service.streamNearby(latitude, longitude, page);
                    streamEntries = stream.getEntries();
                    break;
                case POPULAR:
                    stream = ((MainActivity) getActivity()).service.streamPopular(page);
                    streamEntries = stream.getEntries();
                    break;
                case ALL:
                    stream = ((MainActivity) getActivity()).service.streamPublic(page);
                    streamEntries = stream.getEntries();
                    break;
            }
            return streamEntries;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ((MainActivity) getActivity()).showSignInRequiredDialog(getActivity(), "Sign-In Required", "Please sign in to access your stream");
        }

        @Override
        protected void onPostExecute(List<Entry> streamEntries) {
            super.onPostExecute(streamEntries);

            if (streamEntries != null) {
                if (loadType == MORE) {
                    streamItems.remove(streamItems.size() - 1);
                    adapter.notifyItemRemoved(streamItems.size());
                }
                for (int i = 0; i < streamEntries.size(); i++) {
                    Entry entry = streamEntries.get(i);
                    addEntry(entry);
                }
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            }
            loading = false;
        }

        private void addEntry(Entry entry) {
            Workout workout;
            Distance distance = null;
            User user;
            String activityType = null, felt = null, title = null, activityTypeInfo = null, paceInfo = null, message = null, photo_url = null;

            DecimalFormat format = new DecimalFormat("0.#");

            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            utc.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateString = entry.getAt();
            Date date;
            try {
                date = utc.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
                date = new Date();
            }
            String relativeTimeStr = getTimeAgo(date);

            workout = entry.getWorkout();
            if (workout != null) {
                distance = workout.getDistance();
                activityType = workout.getActivity_type();
                felt = workout.getFelt();
                title = workout.getTitle();
            }
            user = entry.getUser();

            if (activityType != null) {
                switch (activityType) {
                    case "Fitness":
                        activityTypeInfo = "did fitness";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " for " + format.format(distance.getValue()) + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }

                        break;
                    case "Running":
                        activityTypeInfo = "ran";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + format.format(distance.getValue()) + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Cycling":
                        activityTypeInfo = "biked";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                double unitPerHr = (distance.getValue() / duration) * 3600;
                                if (distance.getUnits().equals("miles")) {
                                    paceInfo = String.format("%.2f", unitPerHr) + " mph";
                                } else {
                                    paceInfo = String.format("%.2f", unitPerHr) + " kph";
                                }
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Swimming":
                        activityTypeInfo = "swam";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                if (distance.getUnits().equals("yards")) {
                                    double unitPerHr = (distance.getValue() / duration) * (3600 / 1760);
                                    paceInfo = String.format("%.2f", unitPerHr) + " mph";
                                } else {
                                    double unitPerHr = (distance.getValue() / duration) * (3600 / 1000);
                                    paceInfo = String.format("%.2f", unitPerHr) + " kph";
                                }
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Walking":
                        activityTypeInfo = "walked";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Commute":
                        activityTypeInfo = "commuted";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Weights":
                        activityTypeInfo = "lifted weights";
                        if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Yoga":
                        activityTypeInfo = "did yoga";
                        if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Cross Training":
                        activityTypeInfo = "cross trained";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " for " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Spinning":
                        activityTypeInfo = "did spinning";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " for " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Rowing":
                        activityTypeInfo = "rowed";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Rock Climbing":
                        activityTypeInfo = "rock climbed";
                        if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Elliptical":
                        activityTypeInfo = "did elliptical";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " for " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Core Fitness":
                        activityTypeInfo = "did core fitness";
                        if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Hiking":
                        activityTypeInfo = "hiked";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "CrossFit":
                        activityTypeInfo = "did CrossFit";
                        if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Cc Skiing":
                        activityTypeInfo = "skied";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    case "Inline Skating":
                        activityTypeInfo = "inline skated";
                        if (distance != null && distance.getValue() != null) {
                            activityTypeInfo += " " + distance.getValue() + " " + distance.getUnits();
                            if (workout.getDuration() != null) {
                                int duration = workout.getDuration();
                                int h = duration / 3600;
                                int m = (duration % 3600) / 60;
                                int s = duration % 60;
                                String timeString = String.format("%d:%02d:%02d", h, m, s);
                                activityTypeInfo += " in " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);

                                int secPerUnit = (int) (duration / distance.getValue());
                                int hPace = secPerUnit / 3600;
                                int mPace = (secPerUnit % 3600) / 60;
                                int sPace = secPerUnit % 60;
                                timeString = String.format("%d:%02d:%02d", hPace, mPace, sPace);
                                paceInfo = (timeString.startsWith("0:") ? String.format("%d:%02d", mPace, sPace) : timeString) + " pace";
                            }
                        } else if (workout.getDuration() != null) {
                            int duration = workout.getDuration();
                            int h = duration / 3600;
                            int m = (duration % 3600) / 60;
                            int s = duration % 60;
                            String timeString = String.format("%d:%02d:%02d", h, m, s);
                            activityTypeInfo += " for " + (timeString.startsWith("0:") ? String.format("%d:%02d", m, s) : timeString);
                        }
                        break;
                    default:
                        activityTypeInfo = "did a workout";
                        break;
                }
            }

            List<Medium> media = entry.getMedia();
            String mediaUrl = null;
            Integer width = null, height = null;
            if (media != null && media.size() > 0) {
                if (media.get(0).getContent().getType().equals("image")) {
                    mediaUrl = media.get(0).getContent().getUrl();
                    width = media.get(0).getContent().getWidth();
                    height = media.get(0).getContent().getHeight();
                }
            }

            Integer id = entry.getId();
            String username = user.getUsername();

            //TODO: add media view
            //TODO: posted an image
            String info;
            if (activityTypeInfo != null) {
                info = "<font color=#673AB7><b>" + user.getDisplayName() + "</b></font><font color=#727272> " + activityTypeInfo + "</font>";
            } else if (mediaUrl != null) {
                info = "<font color=#673AB7><b>" + user.getDisplayName() + "</b></font><font color=#727272> posted an image</font>";
            } else {
                info = "<font color=#673AB7><b>" + user.getDisplayName() + "</b></font><font color=#727272> posted a note</font>";
            }
            String extraInfo = Joiner.on(", ").skipNulls().join(felt == null ? null : "felt " + felt, paceInfo);

            message = entry.getMessage();
            photo_url = user.getPhotoUrl();

            streamItems.add(new StreamItem(title, info, extraInfo, message, photo_url, relativeTimeStr, mediaUrl, id, username, width, height));
            adapter.notifyItemInserted(streamItems.size());
        }
    }
}