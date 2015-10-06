package com.tbse.nano.p2_ss_tablet.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.models.AlbumSearchResult;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import hugo.weaving.DebugLog;
import kaaes.spotify.webapi.android.models.Image;

@EViewGroup(R.layout.track_result_item)
@DebugLog
public class TrackResultView extends LinearLayout {

    @ViewById(R.id.item_track_text_view)
    TextView trackTextView;

    @ViewById(R.id.item_track_album)
    TextView trackAlbumTextView;

    @ViewById(R.id.item_album_image)
    ImageView albumImageView;

    public TrackResultView(Context context) {
        super(context);
    }

    public void bind(AlbumSearchResult albumSearchResult) {
        trackTextView.setText(albumSearchResult.getTrack().name);
        if (albumSearchResult.getAlbum() != null) {
            trackAlbumTextView.setText(albumSearchResult.getAlbum().name);
        }

        if (albumSearchResult.getNumberOfImages() > 0) {
            albumImageView.setVisibility(View.VISIBLE);
            Image image = albumSearchResult.getImage();
            if (image != null) {
                Picasso.with(getContext())
                        .load(image.url)
                        .fit()
                        .centerCrop()
                        .into(albumImageView);
            }

        } else {
            albumImageView.setVisibility(View.INVISIBLE);
        }
    }
}
