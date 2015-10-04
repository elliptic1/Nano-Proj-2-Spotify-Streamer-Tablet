package com.tbse.nano.nano_proj_2_spotify_streamer_tablet.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ListView;

import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.R;
import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.adapters.TrackResultsAdapter;
import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.fragments.PlayTrackFragment;
import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.fragments.PlayTrackFragment_;
import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.models.TrackResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hugo.weaving.DebugLog;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@EActivity(R.layout.track_list)
@DebugLog
public class ListTracksActivity extends FragmentActivity {

    private final static String TAG = MainActivity.TAG + "-ListTrackAct";

    private PlayTrackFragment playTrackFragment;

    private boolean hasBeenRestored = false;

    @Override
    protected void onStart() {
        super.onStart();
        hasBeenRestored = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "on save instance");
        outState.putParcelableArrayList("searchResults", searchResults);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "on restore instance");
        searchResults = savedInstanceState.getParcelableArrayList("searchResults");
        hasBeenRestored = true;
    }

    @Bean
    TrackResultsAdapter adapter;

    @ViewById(R.id.listOfTracks)
    ListView listView;

    private ArrayList<TrackResult> searchResults;

    @ItemClick(R.id.listOfTracks)
    public void itemTrackClicked(TrackResult trackResult) {
        Log.d(TAG, "trackResult clicked: " + trackResult.toString());

        playTrack(trackResult.getTrackIndex());
    }

    @Receiver(actions = "action_play_track", local = true)
    void playTrack(@Receiver.Extra("trackNumber") int trackNumber) {
        Log.d(TAG, "got play track intent: " + trackNumber);

        if (trackNumber >= searchResults.size()) return;

        if (playTrackFragment != null) {
            try {
                playTrackFragment.dismiss();
            } catch (Exception e) {
                // ignore
            }
        }

        Bundle b = new Bundle();
        TrackResult trackResult = new TrackResult(searchResults.get(trackNumber).getTrack(), trackNumber);
        b.putParcelable("track", trackResult);
        b.putInt("trackNum", trackNumber);
        b.putInt("numberOfSearchResults", searchResults.size());
        playTrackFragment = new PlayTrackFragment_();
        playTrackFragment.setArguments(b);
        playTrackFragment.show(getFragmentManager(), "track");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasBeenRestored) {
            Log.d(TAG, "hasbeenrestored");
            populateTrackResultsList(searchResults);
        } else {
            Log.d(TAG, "has not beenrestored");
            SpotifyApi api = new SpotifyApi();

            String artist = getIntent().getStringExtra("artist");
            searchSpotify(api, artist);
        }
    }

    @Background
    void searchSpotify(SpotifyApi api, String artist) {
        final SpotifyService spotify = api.getService();
        spotify.searchTracks("artist:" + artist, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                Pager<Track> pager = tracksPager.tracks;
                if (pager.items.size() == 0) {
                    Log.d(TAG, "clearing list from searchSpotify");
                    clearTrackResultsList();
                    return;
                }

                ArrayList<TrackResult> trackResults = new ArrayList<TrackResult>();
                int c = 0;
                for (Track t : pager.items) {
                    TrackResult tr = new TrackResult(t, c);
                    c++;
                    trackResults.add(tr);
                }

                populateTrackResultsList(trackResults);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "failure: " + error.getBody());
            }
        });

    }

    @UiThread
    void clearTrackResultsList() {
        if (adapter != null) {
            Log.d(TAG, "clearing" );
//            adapter.clear();
        }
    }

    @Background
    void populateTrackResultsList(final ArrayList<TrackResult> trackList) {

        // sort by popularity
        Collections.sort(trackList, new Comparator<TrackResult>() {
            @Override
            public int compare(TrackResult lhs, TrackResult rhs) {
                return rhs.getTrack().popularity - lhs.getTrack().popularity;
            }
        });

        searchResults = trackList;

        updateListView(trackList);

    }

    @AfterViews
    void setAdapter() {
        listView.setAdapter(adapter);
    }

    @UiThread
    void updateListView(final List<TrackResult> trackList) {
        Log.d(TAG, "clearing list from updateListView");

        clearTrackResultsList();

        // Add the non-null Albums
        int c = 0;
        for (TrackResult track : trackList) {
            if (track == null) continue;
            if (c == 10) break;
            Log.d(TAG, "adding " + track.getTrack() + " at pos " + c);
            adapter.add(new TrackResult(track.getTrack(), c));
            c++;
        }

    }

}
