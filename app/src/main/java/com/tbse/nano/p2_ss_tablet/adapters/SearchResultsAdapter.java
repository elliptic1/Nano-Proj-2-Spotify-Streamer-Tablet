package com.tbse.nano.p2_ss_tablet.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.tbse.nano.p2_ss_tablet.models.SearchResult;
import com.tbse.nano.p2_ss_tablet.views.SearchResultView;
import com.tbse.nano.p2_ss_tablet.views.SearchResultView_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class SearchResultsAdapter extends ArrayAdapter<SearchResult.SearchResultItem> {

    @RootContext
    Context context;

    public SearchResultsAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SearchResultView searchResultView;

        if (convertView == null) {
            searchResultView = SearchResultView_.build(context);
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
