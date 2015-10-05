package com.tbse.nano.p2_ss_tablet.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.adapters.SearchResultsAdapter;
import com.tbse.nano.p2_ss_tablet.fragments.PlayTrackFragment;
import com.tbse.nano.p2_ss_tablet.fragments.PlayTrackFragment_;
import com.tbse.nano.p2_ss_tablet.fragments.SearchResultDetailFragment;
import com.tbse.nano.p2_ss_tablet.fragments.SearchResultListFragment;
import com.tbse.nano.p2_ss_tablet.fragments.SearchResultListFragment_;
import com.tbse.nano.p2_ss_tablet.models.ParcelableArtist;
import com.tbse.nano.p2_ss_tablet.models.SearchResult;
import com.tbse.nano.p2_ss_tablet.models.TrackResult;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * An activity representing a list of SearchResults. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SearchResultDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SearchResultListFragment} and the item details
 * (if present) is a {@link SearchResultDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link SearchResultListFragment.Callbacks} interface
 * to listen for item selections.
 */
@EActivity
public class SearchResultListActivity extends AppCompatActivity
        implements SearchResultListFragment.Callbacks {

    public static String TAG = "Nano2";
    private static MediaPlayer mediaPlayer;
    //    InputMethodManager inputMethodManager;
    SearchView searchView;
//    ListView listView;
//    private PlayTrackFragment playTrackFragment;
//    SearchResultsAdapter adapter;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private boolean hasBeenRestored;
    private ArrayList<ParcelableArtist> parcelableArtists;
    private String searchText = "";
    SearchResultListFragment searchResultListFragment;
    //    private ArrayList<TrackResult> searchResults;
    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

            Log.d(TAG, "Enter was pressed!");
//            adapter.clear();

            searchText = query;

            hideKeyboard();

            SpotifyApi api = new SpotifyApi();
            final SpotifyService spotify = api.getService();
            spotify.searchArtists("*" + query + "*", new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    Pager<Artist> pager = artistsPager.artists;
                    if (pager.items.size() == 0) {
                        showNoSearchResultsToast();
                        return;
                    }

                    parcelableArtists = new ArrayList<ParcelableArtist>();
                    for (Artist artist : pager.items) {
                        ParcelableArtist parcelableArtist = new ParcelableArtist(artist);
                        parcelableArtists.add(parcelableArtist);
                    }

                    searchResultListFragment.populateSearchResultsList(parcelableArtists);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "failure: " + error.getBody());
                    showBadNetworkToast();
                }
            });

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            searchText = newText;
            return false;
        }
    };

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static void setMediaPlayer(MediaPlayer mediaPlayer) {
        SearchResultListActivity.mediaPlayer = mediaPlayer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult_app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        searchResultListFragment = ((SearchResultListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.searchresult_list));

        if (findViewById(R.id.searchresult_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.

            searchResultListFragment.setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("parcelableArtists", parcelableArtists);
        searchView = (SearchView) findViewById(R.id.search_view);
        if (searchView != null && searchView.getQuery() != null)
            outState.putString("searchText", searchText);
        super.onSaveInstanceState(outState);
    }

    private void hideKeyboard() {
        Log.d(TAG, "trying to hide keyboard");
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        hasBeenRestored = false;
    }

    void showNoSearchResultsToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "No Search Results!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showBadNetworkToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Bad Network!", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    void makeNewAdapter(final List<ParcelableArtist> sr) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                adapter.clear();
//                for (ParcelableArtist parcelableArtist : sr) {
//                    if (parcelableArtist == null) continue;
//                    adapter.add(new SearchResult.SearchResultItem("f1", "f2", "f3"));
//                }
//            }
//        }).start();
//    }


    @Override
    protected void onResume() {
        super.onResume();
        if (hasBeenRestored) {
            searchResultListFragment.populateSearchResultsList(parcelableArtists);
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
        }
        hideKeyboard();

//        listView = (ListView) findViewById(R.id.listView);
//        if (listView != null) {
//            listView.setAdapter(adapter);
//        }

        searchView = (SearchView) findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setQuery(searchText, false);
            searchView.setOnQueryTextListener(onQueryTextListener);
            searchView.setIconified(false);
            searchView.setQuery(searchText, false);
        }
    }

//    void playTrack(int trackNumber) {
//        Log.d(TAG, "got play track intent: " + trackNumber);
//
//        if (trackNumber >= searchResults.size()) return;
//
//        if (playTrackFragment != null) {
//            try {
//                playTrackFragment.dismiss();
//            } catch (Exception e) {
//                // ignore
//            }
//        }
//
//        Bundle b = new Bundle();
//        TrackResult trackResult = new TrackResult(searchResults.get(trackNumber).getTrack(), trackNumber);
//        b.putParcelable("track", trackResult);
//        b.putInt("trackNum", trackNumber);
//        b.putInt("numberOfSearchResults", searchResults.size());
//        playTrackFragment = new PlayTrackFragment_();
//        playTrackFragment.setArguments(b);
//        playTrackFragment.show(getFragmentManager(), "track");
//    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        hasBeenRestored = true;
        parcelableArtists = savedInstanceState.getParcelableArrayList("parcelableArtists");
        Log.d(TAG, "restore bundle searchtext: " + savedInstanceState.getString("searchText"));
        searchView = (SearchView) findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setQuery(savedInstanceState.getString("searchText"), false);
            searchView.setIconified(false);
            searchView.clearFocus();
        }
    }

    /**
     * Callback method from {@link SearchResultListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SearchResultDetailFragment.ARG_ITEM_ID, id);
            SearchResultDetailFragment fragment = new SearchResultDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.searchresult_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, SearchResultDetailActivity.class);
            detailIntent.putExtra(SearchResultDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    void searchSpotify(final SpotifyApi api, final String artist) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SpotifyService spotify = api.getService();
                spotify.searchTracks("artist:" + artist, new Callback<TracksPager>() {
                    @Override
                    public void success(TracksPager tracksPager, Response response) {
                        Pager<Track> pager = tracksPager.tracks;
                        if (pager.items.size() == 0) {
                            Log.d(TAG, "TODO: clearing list from searchSpotify");
//                          TODO: clearTrackResultsList();
                            return;
                        }

                        ArrayList<TrackResult> trackResults = new ArrayList<TrackResult>();
                        int c = 0;
                        for (Track t : pager.items) {
                            TrackResult tr = new TrackResult(t, c);
                            c++;
                            trackResults.add(tr);
                        }

                        Log.d(TAG, "TODO: populate search resutls");
//                      TODO: populateTrackResultsList(trackResults);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "failure: " + error.getBody());
                    }
                });
            }
        }).start();

    }
}
