package com.tbse.nano.p2_ss_tablet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.tbse.nano.p2_ss_tablet.Callbacks;
import com.tbse.nano.p2_ss_tablet.activities.MainActivity;
import com.tbse.nano.p2_ss_tablet.adapters.TrackResultsAdapter;
import com.tbse.nano.p2_ss_tablet.models.TrackResult;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

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

@EFragment
public class TrackListFragment extends ListFragment {

    public static String TAG = MainActivity.TAG + "-TLFrag";

    private TrackResultsAdapter trackResultsAdapter;
    private PlayTrackFragment playTrackFragment;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private static ArrayList<TrackResult> trackResults;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trackResultsAdapter = new TrackResultsAdapter(getContext());
        setListAdapter(trackResultsAdapter);

        Bundle args = getArguments();
        String artistName;
        if (args == null) {
            Log.e(TAG, "started TrackListFragment with null args");
            return;
        } else {
            artistName = args.getString("artist");
        }

        trackResults = new ArrayList<TrackResult>();

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();
        spotify.searchTracks("artist:" + artistName, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                Pager<Track> pager = tracksPager.tracks;
                if (pager.items.size() == 0) {
                    Log.d(TAG, "TODO: clearing list from searchSpotify");
//                          TODO: clearTrackResultsList();
                    return;
                }

                int c = 0;
                trackResults.clear();
                for (Track t : pager.items) {
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

    public void populateSearchResultsList(final List<TrackResult> sr) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "populating track list");

                if (sr == null) {
                    Log.e(TAG, "called populate with null list");
                    return;
                }

                // sort by popularity
                Collections.sort(sr, new Comparator<TrackResult>() {
                    @Override
                    public int compare(TrackResult lhs, TrackResult rhs) {
                        return rhs.getTrack().popularity - lhs.getTrack().popularity;
                    }
                });

                updateAdapter(sr);

            }

        }).start();

    }

    synchronized private void updateAdapter(final List<TrackResult> sr) {
        final ListIterator<TrackResult> trackListIterator = sr.listIterator();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int id = 0;

                TrackResult.ITEMS.clear();
                trackResultsAdapter.clear();
                while (trackListIterator.hasNext()) {
                    TrackResult parcelableTrack = trackListIterator.next();
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

        playTrack(position);

    }

    @Receiver(actions = "TLF_playTrack", local = true)
    void playTrack(@Receiver.Extra int trackNumber) {
        Log.d(TAG, "playTrack: " + trackNumber);

        if (trackResults == null) {
            Log.e(TAG, "trackResults is null");
            trackResults = TrackResult.ITEMS;
            return;
        }

        if (trackNumber >= trackResults.size()) return;

        if (playTrackFragment != null) {
            try {
                playTrackFragment.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Excp - couldn't dismiss " + e.getMessage());
            }
        } else {
            Log.e(TAG, "playTrackFragment is null");
        }

        Bundle b = new Bundle();
        TrackResult trackResult = new TrackResult(trackNumber, trackResults.get(trackNumber).getTrack());
        b.putParcelable("track", trackResult);
        b.putInt("trackNumber", trackNumber);
        b.putInt("numberOfSearchResults", TrackResult.ITEMS.size());

        Log.d(TAG, "Bundle is " + b.toString());

        playTrackFragment = new PlayTrackFragment_();
        playTrackFragment.setArguments(b);
        playTrackFragment.show(getActivity().getFragmentManager(), "track");
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
