package de.uni_potsdam.hpi.openmensa.api.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData

import de.uni_potsdam.hpi.openmensa.R

/**
 * Provides simple methods to access shared settings.
 *
 * @author dominik
 */
object SettingsUtils {
    const val KEY_SOURCE_URL = "pref_source_url"

    // Make sure to update xml/preferences.xml as well
    const val KEY_FAVOURITES = "pref_favourites"

    const val KEY_STYLE = "pref_style"
    private const val THEME_DARK = "dark"
    private const val THEME_LIGHT = "light"

    private fun getSharedPrefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getSourceUrl(context: Context): String = getSharedPrefs(context).getString(KEY_SOURCE_URL, context.resources.getString(R.string.source_url_default))!!

    private fun getSelectedThemeName(context: Context) = getSharedPrefs(context).getString(KEY_STYLE, THEME_LIGHT)

    private fun getThemeByString(theme: String): Int = when(theme) {
        THEME_DARK -> R.style.DarkAppTheme
        THEME_LIGHT -> R.style.LightAppTheme
        else -> R.style.LightAppTheme
    }

    fun getSelectedTheme(context: Context) = getThemeByString(getSelectedThemeName(context))

    fun getFavouriteCanteensFromPreferences(context: Context): Set<Int> {
        return getSharedPrefs(context).getStringSet(KEY_FAVOURITES, emptySet())!!.map { it.toInt() }.toSet()
    }

    fun setFavouriteCanteensAtPreferences(context: Context, canteenIds: Set<Int>) {
        getSharedPrefs(context).edit()
                .putStringSet(KEY_FAVOURITES, canteenIds.map { it.toString() }.toSet())
                .apply()
    }

    fun getFavoriteCanteenIdsLive(context: Context) = object: LiveData<Set<Int>>() {
        val prefs = getSharedPrefs(context)
        val listener = object: SharedPreferences.OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
                if (key == KEY_FAVOURITES) {
                    refresh()
                }
            }
        }

        fun refresh() {
            val newValue = getFavouriteCanteensFromPreferences(context)

            if (newValue != value) {
                value = newValue
            }
        }

        override fun onActive() {
            super.onActive()

            prefs.registerOnSharedPreferenceChangeListener(listener)
            refresh()
        }

        override fun onInactive() {
            super.onInactive()

            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
