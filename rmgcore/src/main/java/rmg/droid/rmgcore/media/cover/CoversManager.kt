package rmg.droid.rmgcore.media.cover

import android.content.Context
import android.util.Log
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import rmg.droid.montecarlo.entity.Channel
import rmg.droid.montecarlo.entity.Track
import rmg.droid.rmgcore.utils.parsePlaylistDate
import rmg.droid.rmgcore.utils.parseServerDate
import java.util.*

/**
 *  @author Arthur Korchagin on 12.06.17.
 */

class CoversManager(val context: Context) : Channel.Loader {

    var outOfSync = 0L

    companion object {
        val SIZES: MutableList<String> = Arrays.asList("small", "medium", "large", "extralarge", "mega")!!
    }

    override  fun loadStartTime(track: Track, listener: (Date?) -> Unit) = doAsync {
        val client = OkHttpClient()

        val request = Request.Builder()
                .url("http://playlist.rr.ru/cur_playing/mc/cur_playing.json")
                .get()
                .build()
        var responses: Response? = null

        try {
            responses = client.newCall(request).execute()
        } catch (err: Throwable) {
            err.printStackTrace()
        }

        responses?.header("Date")?.apply {
            outOfSync = System.currentTimeMillis() - (parseServerDate()?.time ?: 0)
        }

        responses?.body()?.string().apply {

            val root = JSONObject(this)

            if (!root.has("Current")) {
                return@doAsync
            }

            val current = root.getJSONObject("Current")

            if (!current.has("StartTime") || !current.has("Artist") || !current.has("Song")) {
                return@doAsync
            }

            val playlistArtist = current.getString("Artist")
            val playlistSong = current.getString("Song")

            if (playlistArtist.equals(track.artist, ignoreCase = true) && playlistSong.equals(track.title, ignoreCase = true)) {
                val string = current.getString("StartTime")
                Log.d(javaClass.name, "loadStartTime-> startTime=$string")

                listener.invoke(string.parsePlaylistDate())
            }
        }
    }.get()!!


   override fun loadSongInfo(audioTrack: Track, listener: (String, Int) -> Unit) = doAsync {

       val client = OkHttpClient()
       val httpUrl = HttpUrl.Builder()
               .scheme("http")
               .host("ws.audioscrobbler.com")
               .addPathSegment("2.0")
               .addQueryParameter("method", "track.getInfo")
               .addQueryParameter("api_key", "d34a33525e9c3b2928362ac93fe02376")
               .addQueryParameter("artist", audioTrack.artist)
               .addQueryParameter("track", audioTrack.title)
               .addQueryParameter("format", "json")
               .build()

       val request = Request.Builder()
               .url(httpUrl)
               .get()
               .build()
       var responses: Response? = null

       try {
           responses = client.newCall(request).execute()
       } catch (err: Throwable) {
           err.printStackTrace()
       }

       responses?.body()?.string().apply {

           val root = JSONObject(this)

           if (!root.has("track")) {
               loadArtistInfo(audioTrack.artist) {
                   listener.invoke(it, 0)
               }
               return@doAsync
           }

           val track = root.getJSONObject("track")

           if (!track.has("album")) {
               loadArtistInfo(audioTrack.artist) {
                   listener.invoke(it, 0)
               }
               return@doAsync
           }

           val album = track.getJSONObject("album")

           if (!album.has("image")) {
               loadArtistInfo(audioTrack.artist) {
                   listener.invoke(it, 0)
               }
               return@doAsync
           }

           val images = album.getJSONArray("image")

           val cover = (0 until images.length())
                   .map { images.getJSONObject(it) }
                   .sortedWith(kotlin.Comparator { obj1, obj2 ->
                       SIZES.indexOf(obj2.getString("size")) - SIZES.indexOf(obj1.getString("size"))
                   })
                   .firstOrNull()?.getString("#text") ?: ""

           val duration = if (track.has("duration")) track.getInt("duration") else 1000 * 60 * 3

           if (cover.isNullOrBlank() && !"рефлекс".equals(audioTrack.artist, true)) {
               loadArtistInfo(audioTrack.artist) {
                   listener.invoke(it, duration)
               }
           } else {
               listener.invoke(cover, duration)
           }
       }
   }.get()!!


    fun loadArtistInfo(artist: String, listener: (String) -> Unit) = doAsync {

        val client = OkHttpClient()
        val httpUrl = HttpUrl.Builder()
                .scheme("http")
                .host("ws.audioscrobbler.com")
                .addPathSegment("2.0")
                .addQueryParameter("method", "artist.getInfo")
                .addQueryParameter("api_key", "d34a33525e9c3b2928362ac93fe02376")
                .addQueryParameter("artist", artist)
                .addQueryParameter("format", "json")
                .build()

        val request = Request.Builder()
                .url(httpUrl)
                .get()
                .build()
        var responses: Response? = null

        try {
            responses = client.newCall(request).execute()
        } catch (err: Throwable) {
            err.printStackTrace();
        }

        responses?.body()?.string().apply {

            val root = JSONObject(this)

            if (!root.has("artist")) {
                return@doAsync
            }

            val artist = root.getJSONObject("artist")


            if (!artist.has("image")) {
                return@doAsync
            }

            val images = artist.getJSONArray("image")

            val cover = (0 until images.length())
                    .map { images.getJSONObject(it) }
                    .sortedWith(kotlin.Comparator { obj1, obj2 ->
                        SIZES.indexOf(obj2.getString("size")) - SIZES.indexOf(obj1.getString("size"))
                    })
                    .firstOrNull()?.getString("#text") ?: ""


            listener.invoke(cover)
        }
    }


}