package com.tbse.nano.p2_ss_tablet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.tbse.nano.p2_ss_tablet.Callbacks;
import com.tbse.nano.p2_ss_tablet.activities.TrackListActivity;
import com.tbse.nano.p2_ss_tablet.adapters.TrackResultsAdapter;
import com.tbse.nano.p2_ss_tablet.models.ParcelableTrack;
import com.tbse.nano.p2_ss_tablet.models.TrackResult;

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

public class TrackListFragment extends ListFragment {

    public static String TAG = TrackListActivity.TAG + "-TLFrag";

    private TrackResultsAdapter trackResultsAdapter;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private ArrayList<ParcelableTrack> parcelableTracks;

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };


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

        parcelableTracks = new ArrayList<ParcelableTrack>();

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
                parcelableTracks.clear();
                for (Track t : pager.items) {
                    ParcelableTrack parcelableTrack;
                    parcelableTrack = ParcelableTrack.CREATOR.createFromParcel(null);
                    parcelableTrack.setMyTrack(t);
                    parcelableTracks.add(parcelableTrack);
                    c++;
                }

                populateSearchResultsList(parcelableTracks);

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


    public void populateSearchResultsList(final List<ParcelableTrack> sr) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "populating track list");

                if (sr == null) {
                    Log.e(TAG, "called populate with null list");
                    return;
                }

                // sort by popularity
                Collections.sort(sr, new Comparator<ParcelableTrack>() {
                    @Override
                    public int compare(ParcelableTrack lhs, ParcelableTrack rhs) {
                        return rhs.getMyTrack().popularity - lhs.getMyTrack().popularity;
                    }
                });

                updateAdapter(sr);

            }

           }).start();

    }

    synchronized private void updateAdapter(List<ParcelableTrack> sr) {
        final ListIterator<ParcelableTrack> parcelableTrackListIterator = sr.listIterator();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int id = 0;

                trackResultsAdapter.clear();
                while (parcelableTrackListIterator.hasNext()) {
                    ParcelableTrack parcelableTrack = parcelableTrackListIterator.next();
                    Log.d(TAG, "got " + id + " " + parcelableTrack.getMyTrack().name);

                    TrackResult srItem = new TrackResult(id, parcelableTrack.getMyTrack());

                    trackResultsAdapter.add(srItem);

                    ++id;
                }

            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("TLActivity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(TrackResult.ITEMS.get(position).getId());
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
