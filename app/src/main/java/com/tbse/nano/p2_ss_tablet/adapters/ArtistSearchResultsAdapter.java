package com.tbse.nano.p2_ss_tablet.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.tbse.nano.p2_ss_tablet.models.ArtistSearchResult;
import com.tbse.nano.p2_ss_tablet.views.SearchResultView;
import com.tbse.nano.p2_ss_tablet.views.SearchResultView_;

import java.util.ArrayList;

public class ArtistSearchResultsAdapter extends ArrayAdapter<ArtistSearchResult.SearchResultItem> {

    public ArtistSearchResultsAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SearchResultView searchResultView;

        if (convertView == null) {
            searchResultView = SearchResultView_.build(getContext());
        } else {
            searchResultView = (SearchResultView) convertView;
        }

        searchResultView.bind(getItem(position));

        return searchResultView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
