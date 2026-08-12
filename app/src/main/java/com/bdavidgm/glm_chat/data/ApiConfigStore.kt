package com.bdavidgm.glm_chat.data

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

class ApiConfigStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val appContext = context.applicationContext

    fun load(): ApiConfig? {
        val json = prefs.getString(KEY_JSON, null) ?: return null
        return try {
            ApiConfigParser.parse(json)
        } catch (_: Exception) {
            null
        }
    }

    fun hasConfig(): Boolean = load() != null

    /**
     * Lee el JSON desde un [Uri] del almacenamiento (selector del sistema) y lo persiste.
     */
    fun importFromUri(uri: Uri): ApiConfig {
        val text = appContext.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
        } ?: throw IllegalArgumentException("No se pudo leer el archivo seleccionado")

        val config = ApiConfigParser.parse(text)
        prefs.edit()
            .putString(KEY_JSON, ApiConfigParser.toJson(config))
            .apply()
        return config
    }

    fun save(config: ApiConfig) {
        prefs.edit()
            .putString(KEY_JSON, ApiConfigParser.toJson(config))
            .apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_JSON).apply()
    }

    companion object {
        private const val PREFS_NAME = "api_config"
        private const val KEY_JSON = "config_json"
    }
}
