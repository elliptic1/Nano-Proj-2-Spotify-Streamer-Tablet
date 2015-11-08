package com.tbse.nano.p2_ss_tablet.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tbse.nano.p2_ss_tablet.activities.MainActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackResult implements Serializable, Parcelable {

    private Track track;
    public static List<TrackResult.TrackResultItem> ITEMS = new ArrayList<TrackResult.TrackResultItem>();

    protected TrackResult(Parcel in) {
        trackIndex = in.readInt();
        track = ((TrackResult) in.readSerializable()).getTrack();
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

    private int trackIndex;

    public TrackResult(int index, Track track) {
        this.track = track;
        this.trackIndex = index;
    }

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
        dest.writeInt(trackIndex);
//        dest.writeSerializable(this);
    }

    public static class TrackResultItem implements Serializable {
        private int id;
        private Track track;

        public int getId() {
            return id;
        }

        public TrackResultItem(int id, Track track) {
            this.id = id;
            this.track = track;
        }

        @Override
        public String toString() {
            return getArtistName();
        }

        public String getArtistName() {
            if (track != null) {
                if (track.artists != null) {
                    if (track.artists.size() > 0) {
                        return track.artists.get(0).name;
                    }
                }
            }
            return "no names";
        }

        public int getNumberOfTrackImages() {
            if (track == null || track.album == null || track.album.images == null) return 0;
            return track.album.images.size();
        }

        @Nullable
        public Image getFirstTrackImage() {
            if (getNumberOfTrackImages() > 0) {
                return track.album.images.get(0);
            }
            return null;
        }

        public Track getTrack() {
            return track;
        }

    }

    @Override
    public String toString() {
        if (track == null) {
            return "track is null";
        }
        return getTrack().name;
    }
}
