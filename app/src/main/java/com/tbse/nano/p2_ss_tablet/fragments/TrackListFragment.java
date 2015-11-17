package com.tbse.nano.p2_ss_tablet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.tbse.nano.p2_ss_tablet.Callbacks;
import com.tbse.nano.p2_ss_tablet.activities.MainActivity;
import com.tbse.nano.p2_ss_tablet.adapters.TrackResultsAdapter;
import com.tbse.nano.p2_ss_tablet.models.TrackResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A list fragment representing a list of TrackResults. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TrackListFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */

public class TrackListFragment extends ListFragment {

    public static String TAG = MainActivity.TAG + "-TLFrag";

    private Activity activity;
    private TrackResultsAdapter trackResultsAdapter;
    private PlayTrackFragment playTrackFragment;
    private int currentlyPlayingTrackNumber;
    private static Handler changeTrackHandler;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public ArrayList<TrackResult> getTrackResults() {
        return trackResults;
    }

    private ArrayList<TrackResult> trackResults;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public TrackListFragment() {
        Log.d(TAG, "TLF constr");
    }

    public static TrackListFragment newInstance(Activity activity) {
        Log.d(TAG, "TLF newInstance");
        TrackListFragment fragment = new TrackListFragment();
        fragment.setActivity(activity);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate with " + savedInstanceState);

        changeTrackHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                Log.d(TAG, "handling message " + msg + " with data " + msg.getData());

                switch (msg.what) {
                    case -1: // prev
                        if (currentlyPlayingTrackNumber != 0)
                            playTrack(getActivity(), currentlyPlayingTrackNumber - 1);
                        break;
                    case 1: // next
                        if (currentlyPlayingTrackNumber != 9)
                            playTrack(getActivity(), currentlyPlayingTrackNumber + 1);
                        break;
                }

                return false;
            }
        });

        trackResultsAdapter = new TrackResultsAdapter(getContext());
        setListAdapter(trackResultsAdapter);

    }

    public void search(@NonNull String artistName) {
        Log.d(TAG, "setting trackResults to new List");
        trackResults = new ArrayList<TrackResult>();

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();
        Log.d(TAG, "Searching with artistname [" + artistName + "]");
        if (!"".equals(artistName))
            spotify.searchTracks("artist:" + artistName, new Callback<TracksPager>() {
                @Override
                public void success(TracksPager tracksPager, Response response) {
                    Log.d(TAG, "search success");
                    Pager<Track> pager = tracksPager.tracks;
                    if (pager.items.size() == 0) {
                        Log.d(TAG, "TODO: clearing list from searchSpotify");
//                          TODO: clearTrackResultsList();
                        return;
                    }

                    int c = 0;
                    Log.d(TAG, "clearing track results");
                    trackResults.clear();
                    for (Track t : pager.items) {
                        Log.d(TAG, "adding track " + t.name);
                        trackResults.add(new TrackResult(c, t));
                        c++;
                    }

                    populateSearchResultsList(trackResults);

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "failure: " + error.getBody());
                }
            });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    public void populateSearchResultsListFromParcelables(@NonNull final List<Parcelable> sr) {
        trackResults = new ArrayList<TrackResult>();
        for (Parcelable p : sr) {
            trackResults.add((TrackResult) p);
        }
        populateSearchResultsList(trackResults);
    }


    public void populateSearchResultsList(@NonNull final List<TrackResult> sr) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "populating track list");

                // sort by popularity
                Collections.sort(sr, new Comparator<TrackResult>() {
                    @Override
                    public int compare(TrackResult lhs, TrackResult rhs) {
                        return rhs.getTrack().popularity - lhs.getTrack().popularity;
                    }
                });

                Log.d(TAG, "updating adapter");
                updateAdapter(sr);

            }

        }).start();

    }

    synchronized private void updateAdapter(final List<TrackResult> sr) {
        Activity activity = getActivity();

        // tablet
        if (activity == null) {
            activity = getParentFragment().getActivity();
        }

        if (sr.size() == 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "No tracks!", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int id = 0;

                TrackResult.ITEMS.clear();
                trackResultsAdapter.clear();
                for (TrackResult parcelableTrack : sr) {
                    Log.d(TAG, "got " + id + " " + parcelableTrack.getTrack().name);

                    TrackResult srItem = new TrackResult(id, parcelableTrack.getTrack());

                    trackResultsAdapter.add(srItem);

                    TrackResult.TrackResultItem trackResultItem =
                            new TrackResult.TrackResultItem(id, parcelableTrack.getTrack());
                    TrackResult.ITEMS.add(trackResultItem);

                    ++id;

                    if (id > 9) return;
                }

            }
        });
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        playTrack(getActivity(), position);

    }

    private void playTrack(Activity activity, final int trackNumber) {
        Log.d(TAG, "playTrack: " + trackNumber + " out of " + trackResults.size());

        currentlyPlayingTrackNumber = trackNumber;

        if (trackNumber >= trackResults.size()) return;

        if (playTrackFragment != null) {
            try {
                Log.d(TAG, "dismissing");
                playTrackFragment.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Excp - couldn't dismiss " + e.getMessage());
            }
        } else {
            Log.e(TAG, "playTrackFragment is null");
        }

        Bundle b = new Bundle();
        TrackResult trackResult = new TrackResult(trackNumber, trackResults.get(trackNumber).getTrack());
        b.putParcelable("trackResult", trackResult);
        b.putString("artist", trackResult.getTrack().artists.get(0).name);
        b.putInt("trackNumber", trackNumber);
        b.putInt("numberOfSearchResults", TrackResult.ITEMS.size());

        Log.d(TAG, "Bundle is " + b);
        playTrackFragment = PlayTrackFragment_.builder().arg("track", (Serializable) trackResult).build();
        Log.d(TAG, "calling pTF.setHandler");
        playTrackFragment.setHandler(changeTrackHandler);
        Log.d(TAG, "pTF.show");
        playTrackFragment.show(activity.getFragmentManager(), "track");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

}
