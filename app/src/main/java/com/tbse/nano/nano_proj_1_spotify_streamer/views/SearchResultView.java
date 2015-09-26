package com.tbse.nano.nano_proj_1_spotify_streamer.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tbse.nano.nano_proj_1_spotify_streamer.R;
import com.tbse.nano.nano_proj_1_spotify_streamer.models.SearchResult;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import kaaes.spotify.webapi.android.models.Image;

@EViewGroup(R.layout.search_result_item)
public class SearchResultView extends LinearLayout {

    @ViewById(R.id.search_result_main_text_view)
    TextView searchResultTextView;

    @ViewById(R.id.search_result_main_genre)
    TextView searchResultMainGenre;

    @ViewById(R.id.search_result_main_image)
    ImageView searchResultMainImage;

    public SearchResultView(Context context) {
        super(context);
    }

    public void bind(SearchResult searchResult) {
        searchResultTextView.setText(searchResult.getArtistName());
        searchResultMainGenre.setText(searchResult.getGenre());

        if (searchResult.getNumberOfImages() > 0) {
            searchResultMainImage.setVisibility(View.VISIBLE);
            Image image = searchResult.getFirstImage();
            if (image != null) {
                Picasso.with(getContext())
                        .load(image.url)
                        .fit()
                        .centerCrop()
                        .into(searchResultMainImage);
            }

        } else {
            searchResultMainImage.setVisibility(View.INVISIBLE);
        }
    }
}
