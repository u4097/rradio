package ru.russianmediagroup.rusrad.network.interceptors

import android.content.res.AssetManager
import okhttp3.*

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
class MockInterceptor
constructor(private val assets: AssetManager) : Interceptor {

    private fun readAssets(method: String, segments: String): ByteArray {
        val json = assets.open("API/$method/$segments.json")
        val fileBytes = ByteArray(json.available())
        json.read(fileBytes)
        json.close()
        return fileBytes
    }

    override fun intercept(chain: Interceptor.Chain?) =
            chain?.request()?.run {
                val segments = url().pathSegments()

                if (segments.component2() in listOf(
                                ""
                                /* , "settings" */
                                /*, "channel_list" */
                                /*, "poll"*/
                                /*, "app_menu"*/
                                /*, "active_translation"*/
                                /*,"actions_list"*/
                                /*, "news_mobile_list"*/
                        )) {

                    Response.Builder()
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .request(this)
                            .message("Success")
                            .body(ResponseBody.create(MediaType.parse("application/json"), readAssets(method(), segments.joinToString("/"))))
                            .build()

                } else {
                    chain.proceed(this)
                }

            }


}