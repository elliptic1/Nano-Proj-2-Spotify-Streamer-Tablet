package com.tbse.nano.p2_ss_tablet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.tbse.nano.p2_ss_tablet.Callbacks;
import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.fragments.TrackListFragment;
import com.tbse.nano.p2_ss_tablet.models.ParcelableTrack;

import java.util.ArrayList;

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

    public static final String TAG = "Nano-TLA";
    private TrackListFragment trackListFragment;
    private ArrayList<ParcelableTrack> parcelableTracks;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracklist);

        trackListFragment = (TrackListFragment) getSupportFragmentManager().findFragmentById(R.id.trackresult_list);
        trackListFragment.setActivateOnItemClick(true);

        Log.d(TAG, "intent is " + getIntent().toUri(1));

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString("artist", getIntent().getStringExtra("artist"));
            Log.d(TAG, "artist is " + getIntent().getStringExtra("artist"));
//            arguments.putString(TrackListFragment.ARG_ITEM_ID,
//                    getIntent().getStringExtra(TrackListFragment.ARG_ITEM_ID));
            TrackListFragment fragment = new TrackListFragment();
            fragment.setArguments(arguments);
            Log.d(TAG, "adding trackresult_list");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.trackresult_list, fragment)
                    .commit();
        } else {
            Log.d(TAG, "saved Instance state is not null");
        }
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
        Log.d(TAG, "populating track list from onRestoreInstanceState");
        trackListFragment.populateSearchResultsList(parcelableTracks);
    }
}
