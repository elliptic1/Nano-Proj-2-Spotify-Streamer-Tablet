package com.tbse.nano.nano_proj_2_spotify_streamer_tablet.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tbse.nano.nano_proj_2_spotify_streamer_tablet.activities.MainActivity;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackResult implements Parcelable {

    private Track track;

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    private int trackIndex; // Should be ten

    public TrackResult(Track track, int index) {
        this.track = track;
        this.trackIndex = index;
    }

    protected TrackResult(Parcel in) {
    }

    public static final Creator<TrackResult> CREATOR = new Creator<TrackResult>() {
        @Override
        public TrackResult createFromParcel(Parcel in) {
            return new TrackResult(in);
        }

        @Override
        public TrackResult[] newArray(int size) {
            return new TrackResult[size];
        }
    };

//    public MyTrack getMyTrack() {
//        if (track == null) {
//            track = new Track();
//        }
//        return new MyTrack().setTrack(track);
//    }

    public Track getTrack() {
        if (track == null) {
            Log.d(MainActivity.TAG, "getting new empty track");
            track = new Track();
        }
        return track;
    }

    public AlbumSimple getAlbum() {
        if (getTrack() != null) {
            return getTrack().album;
        }
        return null;
    }

    public int getNumberOfImages() {
        if (getTrack() == null || getTrack().album == null || getTrack().album.images == null
                || getTrack().album.images.size() == 0) {
            return 0;
        }
        return getTrack().album.images.size();
    }

    public Image getImage() {
        if (getNumberOfImages() > 0)
            return getTrack().album.images.get(0);
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public String toString() {
        if (track == null) {
            return "track is null";
        }
        return getTrack().name;
    }
}
