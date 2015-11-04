package com.tbse.nano.p2_ss_tablet.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.tbse.nano.p2_ss_tablet.models.TrackResult;
import com.tbse.nano.p2_ss_tablet.views.TrackResultView;
import com.tbse.nano.p2_ss_tablet.views.TrackResultView_;

public class TrackResultsAdapter extends ArrayAdapter<TrackResult> {

    public TrackResultsAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TrackResultView trackResultView;

        if (convertView == null) {
            trackResultView = TrackResultView_.build(getContext());
        } else {
            trackResultView = (TrackResultView) convertView;
        }

        trackResultView.bind(getItem(position));

        return trackResultView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
