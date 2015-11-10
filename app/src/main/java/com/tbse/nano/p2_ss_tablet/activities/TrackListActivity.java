package com.tbse.nano.p2_ss_tablet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.fragments.TrackListFragment;

import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a single TrackList detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link TrackListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link TrackListFragment}.
 */

public class TrackListActivity extends AppCompatActivity {

    private TrackListFragment trackListFragment;
    public static final String TAG = MainActivity.TAG + "-TLA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracklist_app_bar);

        trackListFragment = (TrackListFragment) getSupportFragmentManager().findFragmentById(R.id.trackresult_list);
        trackListFragment.setActivateOnItemClick(true);

        if (savedInstanceState != null) {
            Log.d(TAG, "The saved instance state is " + savedInstanceState);
            ArrayList<Parcelable> parcelables = savedInstanceState.getParcelableArrayList("trackResults");
            if (parcelables == null) {
                Log.d(TAG, "parcelables is null");
                return;
            }
            trackListFragment.populateSearchResultsListFromParcelables(parcelables);
            return;
        }

        trackListFragment.search(getIntent().getStringExtra("artist"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
            outState.putParcelableArrayList("trackResults", trackListFragment.getTrackResults());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        List<Parcelable> parcelables = savedInstanceState.getParcelableArrayList("trackResults");
        if (parcelables == null) {
            Log.e(TAG, "parcelables is null");
            return;
        }
        trackListFragment.populateSearchResultsListFromParcelables(parcelables);
    }

}
