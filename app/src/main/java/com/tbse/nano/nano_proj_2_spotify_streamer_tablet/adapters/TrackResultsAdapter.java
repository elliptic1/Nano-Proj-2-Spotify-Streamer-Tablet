package com.tbse.nano.nano_proj_2_spotify_streamer_tablet.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.activities.SearchResultListActivity;
import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.models.TrackResult;
import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.views.TrackResultView;
import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.views.TrackResultView_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import hugo.weaving.DebugLog;

@EBean
@DebugLog
public class TrackResultsAdapter extends ArrayAdapter<TrackResult> {

    @RootContext Context context;

    public TrackResultsAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TrackResultView trackResultView;

        if (convertView == null) {
            trackResultView = TrackResultView_.build(context);
        } else {
            trackResultView = (TrackResultView) convertView;
        }

        TrackResult tr = getItem(position);
        Log.d(SearchResultListActivity.TAG, "binding " + tr);

        trackResultView.bind(getItem(position));

        return trackResultView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
