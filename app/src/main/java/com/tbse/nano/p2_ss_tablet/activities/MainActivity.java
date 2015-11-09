package com.tbse.nano.p2_ss_tablet.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.tbse.nano.p2_ss_tablet.Callbacks;
import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.fragments.ArtistSearchResultListFragment;
import com.tbse.nano.p2_ss_tablet.fragments.TrackListFragment;
import com.tbse.nano.p2_ss_tablet.models.ArtistSearchResult;
import com.tbse.nano.p2_ss_tablet.models.ParcelableArtist;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity implements Callbacks {

    public static String TAG = "Nano";
    private static MediaPlayer mediaPlayer;
    SearchView searchView;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private boolean hasBeenRestored;
    private ArrayList<ParcelableArtist> parcelableArtists;
    private String searchText = "";
    private ArtistSearchResultListFragment artistSearchResultListFragment;
    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

            Log.d(TAG, "Enter was pressed!");

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

                    artistSearchResultListFragment.populateSearchResultsList(parcelableArtists);
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
        MainActivity.mediaPlayer = mediaPlayer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult_app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        artistSearchResultListFragment = (ArtistSearchResultListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.artist_search_result_list);

        if (findViewById(R.id.tracklist_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.

            artistSearchResultListFragment.setActivateOnItemClick(true);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (hasBeenRestored) {
            Log.d(TAG, "has been restored and populating search results list");
            artistSearchResultListFragment.populateSearchResultsList(parcelableArtists);
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
        }
        hideKeyboard();

        searchView = (SearchView) findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setQuery(searchText, false);
            searchView.setOnQueryTextListener(onQueryTextListener);
            searchView.setIconified(false);
            searchView.setQuery(searchText, false);
        }
    }


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

    @Override
    public void onArtistSelected(int id) {
        String artist = ArtistSearchResult.ITEMS.get(id).getArtistName();
        Log.d(TAG, "selected artist: " + artist);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Log.d(TAG, "tablet screen");
            Bundle arguments = new Bundle();
            arguments.putString("artist", artist);
            TrackListFragment fragment = new TrackListFragment();
            fragment.setArguments(arguments);
            fragment.setActivity(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tracklist_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Log.d(TAG, "phone screen with " + artist);
            Intent trackListActivityIntent = new Intent(this, TrackListActivity_.class);
            trackListActivityIntent.putExtra("artist", artist);
            startActivity(trackListActivityIntent);
        }
    }

    @Override
    public void onTrackSelected(int ignore) {
        // not used
    }


}
