package com.tbse.nano.nano_proj_1_spotify_streamer.models;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class SearchResult {

    private Artist artist;

    public String getArtistName() {
        if (artist == null) {
            return "null artist";
        }
        return artist.name;
    }

    public int getNumberOfImages() {
        if (artist == null || artist.images == null) return 0;
        return artist.images.size();
    }

    public Image getFirstImage() {
        if (getNumberOfImages() > 0) {
            return artist.images.get(0);
        }
        return null;
    }

    public String getGenre() {
        if (artist == null || artist.genres == null || artist.genres.size() == 0) return "";
        String genre = artist.genres.get(0);
        genre = genre.substring(0,1).toUpperCase() + genre.substring(1, genre.length());
        return genre;
    }

    public SearchResult(Artist artist) {
        this.artist = artist;
    }

}
