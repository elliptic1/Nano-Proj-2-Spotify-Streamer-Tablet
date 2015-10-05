package com.tbse.nano.p2_ss_tablet.fragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.activities.SearchResultListActivity;
import com.tbse.nano.p2_ss_tablet.models.TrackResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Image;

@EFragment(R.layout.play_track)
public class PlayTrackFragment extends DialogFragment {

    @ViewById(R.id.play_album_image)
    ImageView albumImage;
    @ViewById(R.id.play_album_title)
    TextView albumTitle;
    @ViewById(R.id.play_track_title)
    TextView trackTitle;
    @ViewById(R.id.play_artist_name)
    TextView artistName;
    @ViewById(R.id.prev_btn)
    ImageView prevBtn;
    @ViewById(R.id.middle_btn)
    ImageView playPauseBtn;
    @ViewById(R.id.next_btn)
    ImageView nextBtn;

    private enum PlayerState {PLAYING, PAUSED}

    private PlayerState mPlayerState = PlayerState.PAUSED;

    private static int showingTrackNum = 0;

    private int numberOfSearchResults = 0;

    private static String TAG = SearchResultListActivity.TAG;

    public PlayTrackFragment() {
        Log.d(TAG, "PTF constr");
    }

    @Click(R.id.middle_btn)
    void clickMiddle() {
        // TODO play / pause

        MediaPlayer mediaPlayer = SearchResultListActivity.getMediaPlayer();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        SearchResultListActivity.setMediaPlayer(mediaPlayer);

        if (mPlayerState == PlayerState.PAUSED) {
            mPlayerState = PlayerState.PLAYING;

            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_pause);

            TrackResult tr = getArguments().getParcelable("track");
            if (tr == null) {
                Log.e(TAG, "track result is null");
                return;
            }

            startAudio(tr.getTrack().preview_url);

        } else if (mPlayerState == PlayerState.PLAYING) {
            mPlayerState = PlayerState.PAUSED;

            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);

            mediaPlayer.release();

            SearchResultListActivity.setMediaPlayer(null);

        }

    }

    @Background
    void startAudio(String track_prev_url) {
        MediaPlayer mediaPlayer = SearchResultListActivity.getMediaPlayer();
        try {
            if (mediaPlayer.isPlaying()) {
                return;
            }
            mediaPlayer.setDataSource(track_prev_url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playPauseBtn.callOnClick();
                }
            });
            mediaPlayer.start();
        } catch (IllegalStateException ignored) {
            mediaPlayer.reset();
            mediaPlayer.release();
        } catch (IOException ignored) {
        }
    }

    @Background
    void playTrackNum(int n) {
        Log.d(TAG, "play track " + n);
        if (n < 0) {
            n = 0;
        } else if (n > 9) {
            n = 9;
        }
        Log.d(TAG, "now play track " + n);
        showingTrackNum = n;
        Intent intent = new Intent("action_play_track");
        intent.putExtra("trackNumber", n);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    @Click(R.id.prev_btn)
    void clickLeft() {
        Log.d(TAG, "click left, showing " + showingTrackNum);
        if (showingTrackNum == 0) return;
        showingTrackNum--;
        playTrackNum(showingTrackNum);
    }

    @Click(R.id.next_btn)
    void clickRight() {
        int numberOfSearchResults = getArguments().getInt("numberOfSearchResults") > 10 ?
                10 : getArguments().getInt("numberOfSearchResults");
        Log.d(TAG, "click right, showing " + showingTrackNum
                + " numResults-1 = " + (numberOfSearchResults - 1));
        if (showingTrackNum == numberOfSearchResults - 1) return;
        showingTrackNum++;
        playTrackNum(showingTrackNum);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());

    }

    @AfterViews
    void fillOutDialogFragment() {

        Log.d(TAG, "AfterViews");

        TrackResult tr = getArguments().getParcelable("track");
        if (tr == null) {
            Log.d(TAG, "tr is null");
            return;
        }

        showingTrackNum = tr.getTrackIndex();

        Log.d(TAG, "artist: " + tr.getTrack().artists.get(0).name);
        artistName.setText(tr.getTrack().artists.get(0).name);
        int duration = Integer.valueOf("" + (tr.getTrack().duration_ms / 1000));
        int minutes = duration / 60;
        int leftover = duration % 60;
        trackTitle.setText(tr.getTrack().name
                + " (" + minutes + "m " + leftover + "s)");
        albumTitle.setText(tr.getAlbum().name);

        if (tr.getNumberOfImages() > 0) {
            albumImage.setVisibility(View.VISIBLE);
            Image image = tr.getImage();
            if (image != null) {
                Picasso.with(getActivity())
                        .load(image.url)
                        .fit()
                        .centerCrop()
                        .into(albumImage);
            }

        } else {
            albumImage.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        MediaPlayer mediaPlayer = SearchResultListActivity.getMediaPlayer();
        if (mediaPlayer == null) {
            mPlayerState = PlayerState.PAUSED;
            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mPlayerState = PlayerState.PLAYING;
            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
        } else {
            mPlayerState = PlayerState.PAUSED;
            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);
        }
    }
}
