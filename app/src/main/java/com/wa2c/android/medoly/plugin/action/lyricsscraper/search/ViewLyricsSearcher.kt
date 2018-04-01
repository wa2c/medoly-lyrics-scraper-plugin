package com.wa2c.android.medoly.plugin.action.lyricsscraper.search

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
import android.text.TextUtils

import com.wa2c.android.medoly.plugin.action.lyricsscraper.util.Logger

import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Locale

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

//
//object ViewLyricsSearcher {
//    /*
//     * Needed data
//     */
//    private val url = "http://search.crintsoft.com/searchlyrics.htm"
//    //ACTUAL: http://search.crintsoft.com/searchlyrics.htm
//    //CLASSIC: http://www.viewlyrics.com:1212/searchlyrics.htm
//
//    private val clientUserAgent = "MiniLyrics"
//    //NORMAL: MiniLyrics <version> for <player>
//    //EXAMPLE: MiniLyrics 7.6.44 for Windows Media Player
//    //MOBILE: MiniLyrics4Android
//
//    private val clientTag = "client=\"ViewLyricsOpenSearcher\""
//    //NORMAL: MiniLyrics
//    //MOBILE: MiniLyricsForAndroid
//
//    private val searchQueryBase = "<?xml version='1.0' encoding='utf-8' ?><searchV1 artist=\"%s\" title=\"%s\" OnlyMatched=\"1\" %s/>"
//
//    private val searchQueryPage = " RequestPage='%d'"
//
//    private val magickey = "Mlv1clt4.0".toByteArray()
//
//    val LYRICS_ENCODING = "UTF-8"
//
//
//    /**
//     * Download lyrics bytes.
//     * @param urlText Lyrics URL.
//     * @return Lyrics bytes.
//     */
//    @Throws(IOException::class)
//    fun downloadLyricsBytes(urlText: String): ByteArray? {
//        val url = URL(urlText)
//        val con = url.openConnection() as HttpURLConnection
//        con.requestMethod = "GET"
//        con.connect()
//        val status = con.responseCode
//
//        if (status != HttpURLConnection.HTTP_OK) {
//            // failed
//            return null
//        }
//
//        val lyricsBytes: ByteArray
//
//        var inputStream: InputStream? = null
//        var bout: ByteArrayOutputStream? = null
//        try {
//            inputStream = con.inputStream
//            bout = ByteArrayOutputStream()
//            val buffer = ByteArray(4096)
//            while (true) {
//                val len = inputStream!!.read(buffer)
//                if (len < 0) {
//                    break
//                }
//                bout.write(buffer, 0, len)
//            }
//            lyricsBytes = bout.toByteArray()
//        } finally {
//            if (bout != null)
//                bout.close()
//            if (inputStream != null)
//                inputStream.close()
//        }
//        return lyricsBytes
//    }
//
//    /**
//     * Download lyrics.
//     * @param urlText Lyrics URL.
//     * @return Lyrics text.
//     */
//    @Throws(IOException::class)
//    fun downloadLyricsText(urlText: String): String? {
//        val lyricsBytes = downloadLyricsBytes(urlText)
//        return if (lyricsBytes == null || lyricsBytes.isEmpty()) null else String(lyricsBytes, Charset.forName(LYRICS_ENCODING))
//    }
//
//
//    /*
//     * Search function
//     */
//    @Throws(IOException::class, NoSuchAlgorithmException::class, SAXException::class, ParserConfigurationException::class)
//    fun search(title: String, artist: String, page: Int): Result? {
//        return searchQuery(
//                String.format(searchQueryBase, artist, title, clientTag + String.format(Locale.getDefault(), searchQueryPage, page)) // Create XMLQuery String
//        )
//    }
//
//    @Throws(IOException::class, NoSuchAlgorithmException::class, SAXException::class, ParserConfigurationException::class)
//    private fun searchQuery(searchQuery: String): Result? {
//
//        val searchUrl = URL(url)
//        val con = searchUrl.openConnection() as HttpURLConnection
//        con.requestMethod = "POST"
//        con.instanceFollowRedirects = false
//        con.setRequestProperty("User-Agent", clientUserAgent)
//        con.connect()
//
//        // Request
//        var s: OutputStream? = null
//        try {
//            s = con.outputStream
//            s!!.write(assembleQuery(searchQuery.toByteArray(charset("UTF-8"))))
//            s.flush()
//            s.close()
//            if (con.responseCode != HttpURLConnection.HTTP_OK)
//                return null
//        } finally {
//            if (s != null)
//                s.close()
//        }
//
//        // Get full result
//        var rd: BufferedReader? = null
//        try {
//            rd = BufferedReader(InputStreamReader(con.inputStream, "ISO_8859_1"))
//            val builder = StringBuilder()
//            val buffer = CharArray(8192)
//            var read: Int
//            while ((read = rd.read(buffer, 0, buffer.size)) > 0) {
//                builder.append(buffer, 0, read)
//            }
//            val full = builder.toString()
//            // Decrypt, parse, store, and return the result list
//            val r = parseResultXML(decryptResultXML(full))
//            val inf = r.infoList
//            Logger.d(inf)
//            return r
//        } finally {
//            if (rd != null)
//                rd.close()
//        }
//    }
//
//    /*
//     * Add MD5 and Encrypts Search Query
//     */
//
//    @Throws(NoSuchAlgorithmException::class, IOException::class)
//    private fun assembleQuery(value: ByteArray): ByteArray {
//        // Create the variable POG to be used in a dirt code
//        val pog = ByteArray(value.size + magickey.size) //TODO Give a better name then POG
//
//        // POG = XMLQuery + Magic Key
//        System.arraycopy(value, 0, pog, 0, value.size)
//        System.arraycopy(magickey, 0, pog, value.size, magickey.size)
//
//        // POG is hashed using MD5
//        val pog_md5 = MessageDigest.getInstance("MD5").digest(pog)
//
//        //TODO Thing about using encryption or k as 0...
//        // Prepare encryption key
//        var j = 0
//        for (v in value) {
//            j += v.toInt()
//        }
//        val k = (j / value.size).toByte().toInt()
//
//        // Value is encrypted
//        for (m in value.indices)
//            value[m] = (k xor value[m]).toByte()
//
//        // Prepare result code
//        val result = ByteArrayOutputStream()
//
//        // Write Header
//        result.write(0x02)
//        result.write(k)
//        result.write(0x04)
//        result.write(0x00)
//        result.write(0x00)
//        result.write(0x00)
//
//        // Write Generated MD5 of POG problaby to be used in a search cache
//        result.write(pog_md5)
//
//        // Write encrypted value
//        result.write(value)
//
//        // Return magic encoded query
//        return result.toByteArray()
//    }
//
//    /*
//     * Decrypts only the XML from the entire result
//     */
//
//    private fun decryptResultXML(value: String): String {
//        // Get Magic key value
//        val magickey = value[1]
//
//        // Prepare output
//        val neomagic = ByteArrayOutputStream()
//
//        // Decrypts only the XML
//        for (i in 22 until value.length)
//            neomagic.write((value[i] xor magickey).toByte().toInt())
//
//        // Return value
//        return neomagic.toString()
//    }
//
//    /*
//     * Create the ArrayList<LyricInfo>
//     */
//
//    private fun readIntFromAttr(elem: Element, attr: String, def: Int): Int {
//        val data = elem.getAttribute(attr)
//        try {
//            if (!TextUtils.isEmpty(data))
//                return Integer.valueOf(data)!!
//        } catch (e: NumberFormatException) {
//            Logger.d(e)
//        }
//
//        return def
//    }
//
//    private fun readFloatFromAttr(elem: Element, attr: String, def: Float): Double {
//        val data = elem.getAttribute(attr)
//        try {
//            if (!TextUtils.isEmpty(data))
//                return java.lang.Double.valueOf(data)!!
//        } catch (e: NumberFormatException) {
//            Logger.d(e)
//        }
//
//        return def.toDouble()
//    }
//
//    private fun readStrFromAttr(elem: Element, attr: String, def: String): String {
//        val data = elem.getAttribute(attr)
//        try {
//            if (!TextUtils.isEmpty(data))
//                return data
//        } catch (e: NumberFormatException) {
//            Logger.d(e)
//        }
//
//        return def
//    }
//
//    @Throws(SAXException::class, IOException::class, ParserConfigurationException::class)
//    private fun parseResultXML(resultXML: String): Result {
//        val result = Result()
//
//        // Create array for storing the results
//        val availableLyrics = ArrayList<ResultItem>()
//
//        // Parse XML
//        val resultBA = ByteArrayInputStream(resultXML.toByteArray(charset("UTF-8")))
//        val resultRootElem = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultBA).documentElement
//
//        result.currentPage = readIntFromAttr(resultRootElem, "CurPage", 0)
//        result.pageCount = readIntFromAttr(resultRootElem, "PageCount", 1)
//        val server_url = readStrFromAttr(resultRootElem, "server_url", "http://www.viewlyrics.com/")
//        //result.setMessage(readStrFromAttr(resultRootElem, "message", ""));
//
//        val resultItemList = resultRootElem.getElementsByTagName("fileinfo")
//        for (i in 0 until resultItemList.length) {
//            val itemElem = resultItemList.item(i) as Element
//            val itemInfo = ResultItem()
//            itemInfo.pageUrl = server_url + readStrFromAttr(itemElem, "link", "")
//            itemInfo.musicArtist = readStrFromAttr(itemElem, "artist", "")
//            itemInfo.musicTitle = readStrFromAttr(itemElem, "title", "")
//            itemInfo.musicAlbum = readStrFromAttr(itemElem, "album", "")
//            //            itemInfo.setLyricsFileName(readStrFromAttr(itemElem, "filename", ""));
//            //            itemInfo.setLyricUploader(readStrFromAttr(itemElem, "uploader", ""));
//            //            itemInfo.setLyricRate(readFloatFromAttr(itemElem, "rate", 0.0F));
//            //            itemInfo.setLyricRatesCount(readIntFromAttr(itemElem, "ratecount", 0));
//            //            itemInfo.setLyricDownloadsCount(readIntFromAttr(itemElem, "downloads", 0));
//            //itemInfo.setFType(readIntFromAttr(itemElem, "file_type", 0));
//            //itemInfo.setMatchVal(readFloatFromAttr(itemElem, "match_value", 0.0F));
//            //itemInfo.setTimeLenght(readIntFromAttr(itemElem, "timelength", 0));
//
//
//            availableLyrics.add(itemInfo)
//        }
//
//        // Add all founded lyrics founded to result
//        result.infoList = availableLyrics
//
//        return result
//    }
//
//}
