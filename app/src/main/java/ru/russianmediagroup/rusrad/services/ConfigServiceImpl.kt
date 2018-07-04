package ru.russianmediagroup.rusrad.services

import android.app.Application
import android.net.Uri
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import org.jetbrains.anko.defaultSharedPreferences
import ru.russianmediagroup.rusrad.R
import java.io.File

/**
 *  @author Arthur Korchagin on 02.08.17.
 */
class ConfigServiceImpl(val app: Application) : ConfigService {

    companion object {

        const val CACHE_TIME = 60L
        const val KEY_SPLASH_URL = "splash_url"
        const val KEY_SPLASH_TITLE = "splash_title"
    }

    private val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    override fun splashFile(default: String?) = File(app.filesDir,
            default ?: app.defaultSharedPreferences.getString(KEY_SPLASH_URL, ""))

    override fun splashTitle(): String = app.defaultSharedPreferences.getString(KEY_SPLASH_TITLE,
            app.getString(R.string.label_tagline)).run {
        if (isBlank()) app.getString(R.string.label_tagline) else this
    }

    override fun updateSplash() {
        remoteConfig.fetch(CACHE_TIME)

                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AppConfig", "-> App config loaded")
                        remoteConfig.activateFetched()

                        val file = splashFile()
                        if (file.exists()) {
                            Log.d(javaClass.name, "updateSplash-> delete=${file.delete()}")
                        }

                    } else {
                        Log.e("AppConfig", "->  AppConfig  failed to load", task.exception)
                    }

                    val string = remoteConfig.getString(KEY_SPLASH_TITLE)

                    val url = remoteConfig.getString(KEY_SPLASH_URL)
                    val filepath = Uri.parse(url).lastPathSegment

                    app.defaultSharedPreferences
                            .edit()
                            .putString(KEY_SPLASH_TITLE, string)
                            .putString(KEY_SPLASH_URL, filepath)
                            .apply()

                    try {
                        val gsReference = storage.getReferenceFromUrl(url)
                        val file = splashFile(filepath)
                        Log.d(javaClass.name, "updateSplash-> url=$url, string=$string filepath=$filepath")

                        gsReference.getFile(file)
                                .addOnSuccessListener {
                                    Log.d(javaClass.name, "onCreate-> localFile=${file.exists()}")
                                }
                                .addOnFailureListener {
                                    it.printStackTrace()
                                }

                    } catch (ex: IllegalArgumentException) {
                        ex.printStackTrace()
                    }
                }
    }
}