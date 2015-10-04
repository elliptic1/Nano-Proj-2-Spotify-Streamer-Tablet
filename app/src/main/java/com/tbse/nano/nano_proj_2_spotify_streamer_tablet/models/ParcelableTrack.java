package com.tbse.nano.nano_proj_2_spotify_streamer_tablet.models;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

public class ParcelableTrack implements Parcelable {

    private Track myTrack;

    protected ParcelableTrack(Parcel in) {
    }

    public static final Creator<ParcelableTrack> CREATOR = new Creator<ParcelableTrack>() {
        @Override
        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        @Override
        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public Track getMyTrack() {
        return myTrack;
    }

    public void setMyTrack(Track myTrack) {
        this.myTrack = myTrack;
    }
}
