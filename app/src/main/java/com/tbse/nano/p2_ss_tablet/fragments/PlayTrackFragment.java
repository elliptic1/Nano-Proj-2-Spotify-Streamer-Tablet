package com.tbse.nano.p2_ss_tablet.fragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.activities.MainActivity;
import com.tbse.nano.p2_ss_tablet.activities.TrackListActivity;
import com.tbse.nano.p2_ss_tablet.models.TrackResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

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

    Handler handler;

    private static String TAG = MainActivity.TAG + "-PTF";

    private static Track selectedTrack;

    private enum PlayerState {PLAYING, PAUSED}

    private PlayerState mPlayerState = PlayerState.PAUSED;

    public PlayTrackFragment() {
        Log.d(TAG, "constr");
    }

    @Click(R.id.middle_btn)
    void clickMiddle() {
        // TODO play / pause

        MediaPlayer mediaPlayer = MainActivity.getMediaPlayer();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        MainActivity.setMediaPlayer(mediaPlayer);

        if (mPlayerState == PlayerState.PAUSED) {
            mPlayerState = PlayerState.PLAYING;

            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_pause);

            TrackResult tr = (TrackResult) getArguments().getSerializable("track");
            if (tr == null) {
                Log.e(TAG, "tr was null");
                return;
            }
            startAudio(tr.getTrack().preview_url);

        } else if (mPlayerState == PlayerState.PLAYING) {
            mPlayerState = PlayerState.PAUSED;

            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);

            mediaPlayer.release();

            MainActivity.setMediaPlayer(null);

        }

    }

    @Background
    void startAudio(String track_prev_url) {
        MediaPlayer mediaPlayer = MainActivity.getMediaPlayer();
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

    @Click(R.id.prev_btn)
    void clickLeft() {
        Log.d(TAG, "click left");
        getHandler().obtainMessage(-1).sendToTarget();
    }

    @Click(R.id.next_btn)
    void clickRight() {
        Log.d(TAG, "click right");
        getHandler().obtainMessage(1).sendToTarget();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());

        try {
            selectedTrack = ((TrackResult) getArguments().getSerializable("track")).getTrack();
            Log.d(TAG, "set track result to " + selectedTrack.name);
        } catch (Exception e) {
            Log.d(TAG, "onCreate is using a new track");
            selectedTrack = new Track();
        }
    }

    @AfterViews
    void fillOutDialogFragment() {

        Log.d(TAG, "AfterViews");

        Log.d(TAG, "currently the selected track is " + selectedTrack.name);

        Log.d(TAG, "artist: " + selectedTrack.artists.get(0).name);
        artistName.setText(selectedTrack.artists.get(0).name);
        int duration = Integer.valueOf("" + (selectedTrack.duration_ms / 1000));
        int minutes = duration / 60;
        int leftover = duration % 60;
        trackTitle.setText(selectedTrack.name
                + " (" + minutes + "m " + leftover + "s)");
        albumTitle.setText(selectedTrack.name);

        if (getNumberOfImages(selectedTrack) > 0) {
            albumImage.setVisibility(View.VISIBLE);
            Image image = selectedTrack.album.images.get(0);
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

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public int getNumberOfImages(Track track) {
        if (track == null || track.album == null || track.album.images == null) return 0;
        return track.album.images.size();
    }

    @Override
    public void onResume() {
        super.onResume();
        MediaPlayer mediaPlayer = MainActivity.getMediaPlayer();
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
