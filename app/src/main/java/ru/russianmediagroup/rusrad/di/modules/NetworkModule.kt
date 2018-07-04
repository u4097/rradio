package ru.russianmediagroup.rusrad.di.modules

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import rmg.droid.rmgcore.analytics.AnalyticsService
import ru.russianmediagroup.rusrad.app.RusRadioApp
import ru.russianmediagroup.rusrad.network.MyTLSSocketFactory
import ru.russianmediagroup.rusrad.network.NetworkService
import ru.russianmediagroup.rusrad.network.NetworkServiceImpl
import ru.russianmediagroup.rusrad.network.RusRadioTrust
import ru.russianmediagroup.rusrad.network.interceptors.MockInterceptor
import ru.russianmediagroup.rusrad.services.ConfigService
import ru.russianmediagroup.rusrad.services.ConfigServiceImpl
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
@Module
class NetworkModule(private val googleAnalyticsKey: String) {

    @Provides
    @Singleton
    fun provideAnalyticsService(app: RusRadioApp): AnalyticsService =
            AnalyticsService(app, googleAnalyticsKey)

    @Provides
    @Singleton
    fun provideConfigService(app: RusRadioApp): ConfigService =
            ConfigServiceImpl(app)

    @Provides
    @Singleton
    fun provideConnectionSpec() =
            ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                    .cipherSuites(
                            CipherSuite.TLS_PSK_WITH_RC4_128_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,

                            CipherSuite.TLS_RSA_WITH_NULL_SHA,
                            CipherSuite.TLS_RSA_EXPORT_WITH_RC4_40_MD5,
                            CipherSuite.TLS_RSA_WITH_RC4_128_MD5,
                            CipherSuite.TLS_RSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_RSA_EXPORT_WITH_DES40_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_DES_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_NULL_SHA256,
                            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
                            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256,
                            CipherSuite.TLS_RSA_WITH_CAMELLIA_128_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_CAMELLIA_256_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_SEED_CBC_SHA,
                            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,

                            CipherSuite.TLS_RSA_EXPORT_WITH_DES40_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
                    .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(connectionSpecs: ConnectionSpec): OkHttpClient = OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(connectionSpecs))
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(MockInterceptor(RusRadioApp.inst.assets))
            .readTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(MyTLSSocketFactory(), RusRadioTrust().trustManager)
            .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm")
            .create()

    @Provides
    @Singleton
    fun provideNetworkService(okHttpClient: OkHttpClient, gson: Gson, app: RusRadioApp): NetworkService = NetworkServiceImpl(okHttpClient, gson, app)

}