package com.wa2c.android.medoly.plugin.action.lyricsscraper.search;

/**
 * @title ViewLyricsSearcher
 * @author PedroHLC
 * @email pedro.laracampos@gmail.com
 * @date (DD-MM-YYYY) FirstRls: 02-08-2012 02-06-2012 LastUpd: 03-08-2012
 * @version 0.9.03-beta
 * @works Search and get results
 */
/**
 * Modified:  wa2c.
 * Date:      2017-02-21
 */
import android.text.TextUtils;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class ViewLyricsSearcher {
    /*
     * Needed data
     */
    private static final String url = "http://search.crintsoft.com/searchlyrics.htm";
    //ACTUAL: http://search.crintsoft.com/searchlyrics.htm
    //CLASSIC: http://www.viewlyrics.com:1212/searchlyrics.htm

    private static final String clientUserAgent = "MiniLyrics";
    //NORMAL: MiniLyrics <version> for <player>
    //EXAMPLE: MiniLyrics 7.6.44 for Windows Media Player
    //MOBILE: MiniLyrics4Android

    private static final String clientTag = "client=\"ViewLyricsOpenSearcher\"";
    //NORMAL: MiniLyrics
    //MOBILE: MiniLyricsForAndroid

    private static final String searchQueryBase = "<?xml version='1.0' encoding='utf-8' ?><searchV1 artist=\"%s\" title=\"%s\" OnlyMatched=\"1\" %s/>";

    private static final String searchQueryPage = " RequestPage='%d'";

    private static final byte[] magickey = "Mlv1clt4.0".getBytes();

    public static final String LYRICS_ENCODING = "UTF-8";



    /**
     * Download lyrics bytes.
     * @param urlText Lyrics URL.
     * @return Lyrics bytes.
     */
    public static byte[] downloadLyricsBytes(String urlText) throws IOException {
        final URL url = new URL(urlText);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        final int status = con.getResponseCode();

        if (status != HttpURLConnection.HTTP_OK) {
            // failed
            return null;
        }

        byte[] lyricsBytes;

        InputStream inputStream = null;
        ByteArrayOutputStream bout = null;
        try {
            inputStream = con.getInputStream();
            bout = new ByteArrayOutputStream();
            byte [] buffer = new byte[4096];
            while(true) {
                int len = inputStream.read(buffer);
                if(len < 0) {
                    break;
                }
                bout.write(buffer, 0, len);
            }
            lyricsBytes = bout.toByteArray();
        } finally {
            if (bout != null)
                bout.close();
            if (inputStream != null)
                inputStream.close();
        }
        return lyricsBytes;
    }

    /**
     * Download lyrics.
     * @param urlText Lyrics URL.
     * @return Lyrics text.
     */
    public static String downloadLyricsText(String urlText) throws IOException {
        byte[] lyricsBytes = downloadLyricsBytes(urlText);
        if (lyricsBytes == null || lyricsBytes.length == 0)
            return null;
        return new String(lyricsBytes, LYRICS_ENCODING);
    }



    /*
     * Search function
     */
    public static Result search(String title, String artist, int page) throws IOException, NoSuchAlgorithmException, SAXException, ParserConfigurationException {
        return searchQuery(
                String.format(searchQueryBase, artist, title, clientTag +
                        String.format(Locale.getDefault(), searchQueryPage, page)) // Create XMLQuery String
        );
    }

    private static Result searchQuery(String searchQuery) throws IOException, NoSuchAlgorithmException, SAXException, ParserConfigurationException {

        URL searchUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) searchUrl.openConnection();
        con.setRequestMethod("POST");
        con.setInstanceFollowRedirects(false);
        con.setRequestProperty("User-Agent", clientUserAgent);
        con.connect();

        // Request
        OutputStream s = null;
        try {
            s = con.getOutputStream();
            s.write(assembleQuery(searchQuery.getBytes("UTF-8")));
            s.flush();
            s.close();
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;
        } finally {
            if (s != null)
                s.close();
        }

        // Get full result
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO_8859_1"));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = rd.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            String full = builder.toString();
            // Decrypt, parse, store, and return the result list
            Result r = parseResultXML(decryptResultXML(full));
            List<ResultItem> inf = r.getInfoList();
            Logger.d(inf);
            return r;
        } finally {
            if (rd != null)
                rd.close();
        }
    }

    /*
     * Add MD5 and Encrypts Search Query
     */

    private static byte[] assembleQuery(byte[] value) throws NoSuchAlgorithmException, IOException {
        // Create the variable POG to be used in a dirt code
        byte[] pog = new byte[value.length + magickey.length]; //TODO Give a better name then POG

        // POG = XMLQuery + Magic Key
        System.arraycopy(value, 0, pog, 0, value.length);
        System.arraycopy(magickey, 0, pog, value.length, magickey.length);

        // POG is hashed using MD5
        byte[] pog_md5 = MessageDigest.getInstance("MD5").digest(pog);

        //TODO Thing about using encryption or k as 0...
        // Prepare encryption key
        int j = 0;
        for (byte v : value) {
            j += v;
        }
        int k = (byte) (j / value.length);

        // Value is encrypted
        for (int m = 0; m < value.length; m++)
            value[m] = (byte) (k ^ value[m]);

        // Prepare result code
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        // Write Header
        result.write(0x02);
        result.write(k);
        result.write(0x04);
        result.write(0x00);
        result.write(0x00);
        result.write(0x00);

        // Write Generated MD5 of POG problaby to be used in a search cache
        result.write(pog_md5);

        // Write encrypted value
        result.write(value);

        // Return magic encoded query
        return result.toByteArray();
    }

    /*
     * Decrypts only the XML from the entire result
     */

    private static String decryptResultXML(String value) {
        // Get Magic key value
        char magickey = value.charAt(1);

        // Prepare output
        ByteArrayOutputStream neomagic = new ByteArrayOutputStream();

        // Decrypts only the XML
        for (int i = 22; i < value.length(); i++)
            neomagic.write((byte) (value.charAt(i) ^ magickey));

        // Return value
        return neomagic.toString();
    }

    /*
     * Create the ArrayList<LyricInfo>
     */

    private static int readIntFromAttr(Element elem, String attr, int def) {
        String data = elem.getAttribute(attr);
        try {
            if (!TextUtils.isEmpty(data))
                return Integer.valueOf(data);
        } catch (NumberFormatException e) {
            Logger.d(e);
        }
        return def;
    }

    private static double readFloatFromAttr(Element elem, String attr, float def) {
        String data = elem.getAttribute(attr);
        try {
            if (!TextUtils.isEmpty(data))
                return Double.valueOf(data);
        } catch (NumberFormatException e) {
            Logger.d(e);
        }
        return def;
    }

    private static String readStrFromAttr(Element elem, String attr, String def) {
        String data = elem.getAttribute(attr);
        try {
            if (!TextUtils.isEmpty(data))
                return data;
        } catch (NumberFormatException e) {
            Logger.d(e);
        }
        return def;
    }

    private static Result parseResultXML(String resultXML) throws SAXException, IOException, ParserConfigurationException {
        Result result = new Result();

        // Create array for storing the results
        ArrayList<ResultItem> availableLyrics = new ArrayList<>();

        // Parse XML
        ByteArrayInputStream resultBA = new ByteArrayInputStream(resultXML.getBytes("UTF-8"));
        Element resultRootElem = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultBA).getDocumentElement();

        result.setCurrentPage(readIntFromAttr(resultRootElem, "CurPage", 0));
        result.setPageCount(readIntFromAttr(resultRootElem, "PageCount", 1));
        String server_url = readStrFromAttr(resultRootElem, "server_url", "http://www.viewlyrics.com/");
        //result.setMessage(readStrFromAttr(resultRootElem, "message", ""));

        NodeList resultItemList = resultRootElem.getElementsByTagName("fileinfo");
        for (int i = 0; i < resultItemList.getLength(); i++) {
            Element itemElem = (Element) resultItemList.item(i);
            ResultItem itemInfo = new ResultItem();
            itemInfo.setPageUrl(server_url + readStrFromAttr(itemElem, "link", ""));
            itemInfo.setMusicArtist(readStrFromAttr(itemElem, "artist", ""));
            itemInfo.setMusicTitle(readStrFromAttr(itemElem, "title", ""));
            itemInfo.setMusicAlbum(readStrFromAttr(itemElem, "album", ""));
//            itemInfo.setLyricsFileName(readStrFromAttr(itemElem, "filename", ""));
//            itemInfo.setLyricUploader(readStrFromAttr(itemElem, "uploader", ""));
//            itemInfo.setLyricRate(readFloatFromAttr(itemElem, "rate", 0.0F));
//            itemInfo.setLyricRatesCount(readIntFromAttr(itemElem, "ratecount", 0));
//            itemInfo.setLyricDownloadsCount(readIntFromAttr(itemElem, "downloads", 0));
            //itemInfo.setFType(readIntFromAttr(itemElem, "file_type", 0));
            //itemInfo.setMatchVal(readFloatFromAttr(itemElem, "match_value", 0.0F));
            //itemInfo.setTimeLenght(readIntFromAttr(itemElem, "timelength", 0));


            availableLyrics.add(itemInfo);
        }

        // Add all founded lyrics founded to result
        result.setInfoList(availableLyrics);

        return result;
    }

}
