package com.tbse.nano.p2_ss_tablet.activities;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.tbse.nano.p2_ss_tablet.Callbacks;
import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.fragments.TrackListFragment;
import com.tbse.nano.p2_ss_tablet.models.ParcelableTrack;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * An activity representing a single TrackList detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link TrackListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link TrackListFragment}.
 */

public class TrackListActivity extends AppCompatActivity implements Callbacks {

    private TrackListFragment trackListFragment;
    private ArrayList<ParcelableTrack> parcelableTracks;
    public static final String TAG = "Nano";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracklist_app_bar);

        trackListFragment = (TrackListFragment) getSupportFragmentManager().findFragmentById(R.id.trackresult_list);
        trackListFragment.setActivateOnItemClick(true);

        if (savedInstanceState != null) {
            trackListFragment.populateSearchResultsList(parcelableTracks);
            return;
        }

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();
        spotify.searchTracks("artist:" + getIntent().getStringExtra("artist"), new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                Pager<Track> pager = tracksPager.tracks;
                if (pager.items.size() == 0) {
                    Log.d(TAG, "TODO: clearing list from searchSpotify");
//                          TODO: clearTrackResultsList();
                    return;
                }

                int c = 0;
                parcelableTracks = new ArrayList<ParcelableTrack>();
                for (Track t : pager.items) {
                    ParcelableTrack parcelableTrack;
                    parcelableTrack = ParcelableTrack.CREATOR.createFromParcel(null);
                    parcelableTrack.setMyTrack(t);
                    parcelableTracks.add(parcelableTrack);
                    c++;
                }

                trackListFragment.populateSearchResultsList(parcelableTracks);

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "failure: " + error.getBody());
            }
        });

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
//        if (savedInstanceState == null) {
//            // Create the detail fragment and add it to the activity
//            // using a fragment transaction.
//            Bundle arguments = new Bundle();
//            arguments.putString("artist", "mc");
////            arguments.putString(TrackListFragment.ARG_ITEM_ID,
////                    getIntent().getStringExtra(TrackListFragment.ARG_ITEM_ID));
//            TrackListFragment fragment = new TrackListFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.trackresult_list, fragment)
//                    .commit();
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ArtistSearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String id) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("parcelableTracks", parcelableTracks);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        parcelableTracks = savedInstanceState.getParcelableArrayList("parcelableTracks");
        trackListFragment.populateSearchResultsList(parcelableTracks);
    }
}
