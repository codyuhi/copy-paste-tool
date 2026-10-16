package online.codyuhi.copypastetool.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import online.codyuhi.copypastetool.ui.theme.ThemeMode

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USERNAME = stringPreferencesKey("username")
        val USER_ID = stringPreferencesKey("user_id")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val LOCAL_NETWORK_GRANTED = booleanPreferencesKey("local_network_granted")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            ThemeMode.Light.name -> ThemeMode.Light
            ThemeMode.Dark.name -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTH_TOKEN]
    }

    val username: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USERNAME]
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_ID]
    }

    val lastSyncTime: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_SYNC_TIME]
    }

    val hasLocalNetworkAccess: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOCAL_NETWORK_GRANTED] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setAuth(token: String, username: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTH_TOKEN] = token
            prefs[Keys.USERNAME] = username
            prefs[Keys.USER_ID] = userId
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.AUTH_TOKEN)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.USER_ID)
        }
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = timestamp
        }
    }

    suspend fun setLocalNetworkAccess(granted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCAL_NETWORK_GRANTED] = granted
        }
    }
}
