package com.tbse.nano.p2_ss_tablet.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tbse.nano.p2_ss_tablet.activities.ArtistSearchActivity;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackResult implements Parcelable {

    private Track track;
    public static List<TrackResult.TrackResultItem> ITEMS = new ArrayList<TrackResult.TrackResultItem>();

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    private int trackIndex; // Should be ten

    public TrackResult(int index, Track track) {
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

    public Track getTrack() {
        if (track == null) {
            Log.d(ArtistSearchActivity.TAG, "getting new empty track");
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

    /**
     * A map of sample (dummy) items, by ID.
     */
//    public static Map<String, SearchResultItem> ITEM_MAP = new HashMap<String, SearchResultItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(TrackResultItem item) {
        ITEMS.add(item);
//        ITEM_MAP.put(item.getId(), item);
    }

//    public ArtistSearchResult.SearchResultItem getItem(int n) {
//        return n > 0 && ITEMS.size() > 0 && n < ITEMS.size() ? ITEMS.get(n) : null;
//    }

    private static TrackResultItem createDummyItem(int position) {
        return new TrackResultItem(""+position, new Track());
    }

    public static class TrackResultItem {
        private String id;
        private Track track;

        public String getId() {
            return id;
        }

        public TrackResultItem(String id, Track track) {
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

        public Image getFirstTrackImage() {
            if (getNumberOfTrackImages() > 0) {
                return track.album.images.get(0);
            }
            return null;
        }

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
