package com.tbse.nano.p2_ss_tablet.fragments;

import android.app.DialogFragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tbse.nano.p2_ss_tablet.R;
import com.tbse.nano.p2_ss_tablet.activities.MainActivity;
import com.tbse.nano.p2_ss_tablet.models.TrackResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.ViewById;
import org.aspectj.lang.annotation.After;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

@EFragment(R.layout.play_track)
public class PlayTrackFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    @FragmentArg
    Parcelable trackResult;
    @FragmentArg
    String artist;
    @FragmentArg
    int trackNumber;
    @FragmentArg
    int numberOfSearchResults;

    @ViewById(R.id.seekBar)
    SeekBar seekBar;
    @ViewById(R.id.play_album_image)
    ImageView albumImage;
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

    private static Handler handler;
    private static Handler seekBarHandler;

    private static String TAG = MainActivity.TAG + "-PTF";

    private static Track selectedTrack;
    private static int currentProgress;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "start tracking");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "stop tracking");

        if (MainActivity.getMediaPlayer() == null) return;
        MainActivity.getMediaPlayer().seekTo( currentProgress );
    }

    private enum PlayerState {PLAYING, PAUSED}

    private PlayerState mPlayerState = PlayerState.PAUSED;

    public PlayTrackFragment() {
        Log.d(TAG, "constr");
    }

    @Click(R.id.middle_btn)
    void clickMiddle() {
        // TODO play / pause

        MediaPlayer mediaPlayer = MainActivity.getMediaPlayer();

//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//        }
//
//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//        MainActivity.setMediaPlayer(mediaPlayer);

        if (mPlayerState == PlayerState.PAUSED) {
            mPlayerState = PlayerState.PLAYING;

            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_pause);

//            TrackResult tr = (TrackResult) getArguments().getSerializable("track");
//            if (tr == null) {
//                Log.e(TAG, "tr was null");
//                return;
//            }
//            startAudio(tr.getTrack().preview_url);
            mediaPlayer.start();

        } else if (mPlayerState == PlayerState.PLAYING) {
            mPlayerState = PlayerState.PAUSED;

            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);

            mediaPlayer.pause();

//            MainActivity.setMediaPlayer(null);

        }

    }

    void startAudio(String track_prev_url) {
        MediaPlayer mediaPlayer = MainActivity.getMediaPlayer();

        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        MainActivity.setMediaPlayer(mediaPlayer);


        try {
            mediaPlayer.setDataSource(track_prev_url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playPauseBtn.callOnClick();
                }
            });
            seekBar.setMax(mediaPlayer.getDuration());
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
            selectedTrack = ((TrackResult) trackResult).getTrack();
            Log.d(TAG, "set track result to " + selectedTrack.name);
        } catch (NullPointerException e) {
            Log.d(TAG, "onCreate npe");
        } catch (Exception e) {
            Log.d(TAG, "onCreate is using a new track");
        } finally {
            if (selectedTrack == null)
                selectedTrack = new Track();
        }

        if (getHandler() == null) {
            Log.e(TAG, "PTF onCreate getHandler is null");
        } else {
            Log.d(TAG, "PTF onCreate getHandler is NOT null");
        }

        seekBarHandler = new Handler();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.getMediaPlayer() != null && seekBar != null) {
                    seekBar.setProgress(MainActivity.getMediaPlayer().getCurrentPosition());
                }
                seekBarHandler.postDelayed(this, 1000);
            }
        });



    }

    @AfterViews
    void fillOutDialogFragment() {

        Log.d(TAG, "AfterViews");

        trackResult = getArguments().getParcelable("track");
        selectedTrack = ((TrackResult) trackResult).getTrack();
        Log.d(TAG, "currently the selected track is " + selectedTrack.name);

        Log.d(TAG, "artist: " + selectedTrack.artists.get(0).name);
        artistName.setText(selectedTrack.artists.get(0).name);
        int duration = Integer.valueOf("" + (selectedTrack.duration_ms / 1000));
        int minutes = duration / 60;
        int leftover = duration % 60;
        trackTitle.setText(selectedTrack.name + " / " + selectedTrack.album.name
                + " (" + minutes + "m " + leftover + "s)");

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

    @NonNull
    public static Handler getHandler() {
        Log.d(TAG, "getting handler " + handler);
        return handler;
    }

    public void setHandler(@NonNull Handler handler) {
        this.handler = handler;
    }

    public int getNumberOfImages(@NonNull Track track) {
        if (track.album == null || track.album.images == null) return 0;
        return track.album.images.size();
    }

    @Override
    public void onResume() {
        super.onResume();

        seekBar.setOnSeekBarChangeListener(this);

        mPlayerState = PlayerState.PLAYING;
        playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_pause);

        TrackResult tr = (TrackResult) getArguments().getSerializable("track");
        if (tr == null) {
            Log.e(TAG, "tr was null");
            return;
        }
        startAudio(tr.getTrack().preview_url);

//            return;
//        }

//        if (mediaPlayer.isPlaying()) {
//            mPlayerState = PlayerState.PLAYING;
//            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
//        } else {
//            mPlayerState = PlayerState.PAUSED;
//            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);
//        }

    }
}
