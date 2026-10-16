package online.codyuhi.copypastetool.di

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import online.codyuhi.copypastetool.BuildConfig
import online.codyuhi.copypastetool.data.api.CopyPasteApi
import online.codyuhi.copypastetool.data.api.SablierInterceptor
import online.codyuhi.copypastetool.data.db.AppDatabase
import online.codyuhi.copypastetool.data.repository.CopyPasteRepository
import online.codyuhi.copypastetool.data.repository.SettingsRepository
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(val context: Context) {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val sablierInterceptor: SablierInterceptor by lazy {
        SablierInterceptor()
    }

    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(sablierInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: CopyPasteApi by lazy {
        val baseUrl = BuildConfig.API_BASE_URL.let {
            if (it.endsWith("/")) it else "$it/"
        }
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(CopyPasteApi::class.java)
    }

    val copyPasteRepository: CopyPasteRepository by lazy {
        CopyPasteRepository(
            copyPasteDao = database.copyPasteDao(),
            outboxDao = database.outboxDao(),
            api = api,
            settingsRepository = settingsRepository,
            sablierInterceptor = sablierInterceptor
        )
    }
}
