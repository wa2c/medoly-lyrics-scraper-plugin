package com.wa2c.android.medoly.plugin.action.lyricsscraper.search;

/**
 * Search result info item.
 */
public class ResultItem {
    /** Media title. */
    private String musicTitle;
    /** Media artist. */
    private String musicArtist;
    /** Media album. */
    private String musicAlbum;

    /** Lyric title. */
    private String pageTitle;
    /** Lyric url. */
    private String pageUrl;
    /** Lyrics. */
    private String lyrics;

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public String getMusicAlbum() {
        return musicAlbum;
    }

    public void setMusicAlbum(String musicAlbum) {
        this.musicAlbum = musicAlbum;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String lyricTitle) {
        this.pageTitle = pageTitle;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

}
