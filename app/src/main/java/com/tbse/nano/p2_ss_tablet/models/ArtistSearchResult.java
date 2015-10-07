package com.tbse.nano.p2_ss_tablet.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ArtistSearchResult {


    /**
     * An array of sample (dummy) items.
     */
    public static List<SearchResultItem> ITEMS = new ArrayList<SearchResultItem>();


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

    private static void addItem(SearchResultItem item) {
        ITEMS.add(item);
//        ITEM_MAP.put(item.getId(), item);
    }

//    public ArtistSearchResult.SearchResultItem getItem(int n) {
//        return n > 0 && ITEMS.size() > 0 && n < ITEMS.size() ? ITEMS.get(n) : null;
//    }

    private static SearchResultItem createDummyItem(int position) {
        return new SearchResultItem(""+position, new Artist());
    }

//    private static String makeDetails(int position) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Details about Item: ").append(position);
//        for (int i = 0; i < position; i++) {
//            builder.append("\nMore details information here.");
//        }
//        return builder.toString();
//    }

    public static class SearchResultItem {
        private String id;
        private Artist artist;

        public String getId() {
            return id;
        }

        public SearchResultItem(String id, Artist artist) {
            this.id = id;
            this.artist = artist;
        }

        @Override
        public String toString() {
            return getArtistName();
        }

        public String getGenre() {
            String genre = "";
            if (artist == null || artist.genres == null || artist.genres.size() == 0) return genre;
            genre = artist.genres.get(0);
            genre = genre.substring(0, 1).toUpperCase() + genre.substring(1, genre.length());
            return genre;
        }

        public String getArtistName() {
            if (artist == null) {
                return "null artist";
            }
            return artist.name;
        }

        public int getNumberOfArtistImages() {
            if (artist == null || artist.images == null) return 0;
            return artist.images.size();
        }

        public Image getFirstArtistImage() {
            if (getNumberOfArtistImages() > 0) {
                return artist.images.get(0);
            }
            return null;
        }

    }
}
