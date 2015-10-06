package com.tbse.nano.p2_ss_tablet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.tbse.nano.p2_ss_tablet.activities.SearchResultListActivity;
import com.tbse.nano.p2_ss_tablet.adapters.SearchResultsAdapter;
import com.tbse.nano.p2_ss_tablet.models.ParcelableArtist;
import com.tbse.nano.p2_ss_tablet.models.ArtistSearchResult;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * A list fragment representing a list of SearchResults. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link SearchResultDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
@EFragment
public class SearchResultListFragment extends ListFragment {

    public static String TAG = SearchResultListActivity.TAG + "-SRLFrag";

    private SearchResultsAdapter searchResultsAdapter;

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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

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
    public SearchResultListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchResultsAdapter = new SearchResultsAdapter(getContext());
        setListAdapter(searchResultsAdapter);

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

    @Background
    public void populateSearchResultsList(final List<ParcelableArtist> sr) {

        Log.d(TAG, "populating search results");

        if (sr == null) {
            Log.e(TAG, "called populate with null list");
            return;
        }

        // sort by popularity
        Collections.sort(sr, new Comparator<ParcelableArtist>() {
            @Override
            public int compare(ParcelableArtist lhs, ParcelableArtist rhs) {
                return rhs.getArtist().popularity - lhs.getArtist().popularity;
            }
        });

        final ListIterator<ParcelableArtist> parcelableArtistListIterator = sr.listIterator();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int id = 0;

                searchResultsAdapter.clear();
                while (parcelableArtistListIterator.hasNext()) {
                    ParcelableArtist parcelableArtist = parcelableArtistListIterator.next();
                    Log.d(TAG, "got " + id + " " + parcelableArtist.getArtist().name);

                    Artist srArtist = parcelableArtist.getArtist();

                    ArtistSearchResult.SearchResultItem srItem = new ArtistSearchResult.SearchResultItem("" + id, srArtist);

                    searchResultsAdapter.add(srItem);

                    ++id;
                }

            }
        });

        Log.d(TAG, "done populating search results");

    }

    private SearchResultsAdapter getSearchResultsAdapter() {
        return (SearchResultsAdapter) getListAdapter();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
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
        mCallbacks.onItemSelected(ArtistSearchResult.ITEMS.get(position).getId());
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
