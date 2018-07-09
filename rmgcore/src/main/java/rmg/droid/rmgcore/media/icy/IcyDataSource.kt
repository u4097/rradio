package rmg.droid.rmgcore.media.icy

import android.net.Uri
import android.text.Html
import android.text.TextUtils
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.HttpDataSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSource
import rmg.droid.rmgcore.utils.parseServerDate
import java.io.IOException
import java.util.*


class IcyDataSource(private val listener: Listener?) : HttpDataSource {

    companion object {

        private val DELIMETER = " - "

        /* Response Headers */
        private val HEADER_ICY_METAINT = "icy-metaint"
        private val HEADER_DATE = "Date"

        /* Request Headers */
        private val HEADER_ICY_METADATA = "Icy-Metadata"
        private val ICY_METADATA_VALUE = "1"

        /* Keys */
        private val KEY_STREAM_TITLE = "StreamTitle"

    }

    private var mCurrentServerDate: Date? = null
        set(value) {
            listener?.onServerDate(value)
        }

    interface Listener {
        fun onMetaData(artist: String, title: String)
        fun onServerDate(serverDate: Date?)
    }

    private val builder: Request.Builder
    private val client: OkHttpClient = OkHttpClient()
    private var interval: Int = 0
    private var remaining: Int = 0
    private var contents: BufferedSource? = null
    private var response: Response? = null
    private var dataSpec: DataSpec? = null

    init {
        builder = Request.Builder().addHeader(HEADER_ICY_METADATA, ICY_METADATA_VALUE)
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec

        val request = builder
                .url(dataSpec.uri.toString())
                .build()

        try {
            response = client.newCall(request).execute()
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException(e, dataSpec, HttpDataSource.HttpDataSourceException.TYPE_CLOSE)
        }

        val headers = response!!.headers()

        if (!response!!.isSuccessful)
            throw HttpDataSource.InvalidResponseCodeException(response!!.code(), headers.toMultimap(), dataSpec)

        val metaintVal = headers.get(HEADER_ICY_METAINT)
        mCurrentServerDate = headers.get(HEADER_DATE)?.parseServerDate()

        interval = if (metaintVal != null) Integer.parseInt(metaintVal) else 0

        remaining = interval
        contents = response?.body()?.source()
        return C.LENGTH_UNSET.toLong()
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun close() {
        try {
            contents?.close()
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException(e, dataSpec, HttpDataSource.HttpDataSourceException.TYPE_CLOSE)
        }

    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        try {
            val rv = contents?.read(buffer, offset, if (interval > 0 && remaining < readLength) remaining else readLength)

            if (interval > 0 && remaining == rv) {
                remaining = interval
                readMetaData()
            } else {
                remaining -= rv ?: 0
            }

            return rv ?: 0
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException(e, dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)
        }

    }

    @Throws(IOException::class)
    private fun readMetaData() {
        val length = (contents?.readByte() ?: 0) * 16
        if (length > 0) {
            contents?.readByteArray(length.toLong())?.apply {
                parseMetadata(Html.fromHtml(String(this, Charsets.UTF_8)).toString())
            }
        }
    }

    private fun parseMetadata(metadata: String) {
        Log.d(javaClass.name, "parseMetadata-> metadata=$metadata")

        var artist = ""
        var title = ""

        val parts = metadata.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (part in parts) {
            val index = part.indexOf('=')
            if (index < 0)
                continue
            val key = part.substring(0, index)
            val value = part.substring(index + 2, part.length - 1)

            if (KEY_STREAM_TITLE.contentEquals(key)) {
                val components = TextUtils.split(value, DELIMETER)
                if (components.isNotEmpty()) artist = components[0].capWords()
                if (components.size > 1) title = TextUtils.join(DELIMETER, components.drop(1)).capWords()
            }
        }

        listener?.onMetaData(artist, title)
    }

    fun String.capWords() = indices
            .map { if (it == 0 || !this[it - 1].isLetter()) this[it].toUpperCase() else this[it].toLowerCase() }
            .joinToString("")
            .trim()

    override fun setRequestProperty(name: String, value: String) {
        builder.addHeader(name, value)
    }

    override fun clearRequestProperty(name: String) {
        builder.removeHeader(name)
    }

    override fun clearAllRequestProperties() {
        builder.headers(Headers.Builder().build())
    }

    override fun getResponseHeaders(): Map<String, List<String>> {
        return response!!.headers().toMultimap()
    }

    override fun getUri(): Uri? {
        return dataSpec?.uri
    }

}