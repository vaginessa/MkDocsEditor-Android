package de.markusressel.mkdocseditor.view.fragment.preferences

import android.content.Context
import com.eightbitlab.rxbus.Bus
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import de.markusressel.kutepreferences.library.persistence.KutePreferenceDataProvider
import de.markusressel.kutepreferences.library.preference.category.KuteCategory
import de.markusressel.kutepreferences.library.preference.category.KuteDivider
import de.markusressel.kutepreferences.library.preference.select.KuteSingleSelectPreference
import de.markusressel.kutepreferences.library.preference.text.KutePasswordPreference
import de.markusressel.kutepreferences.library.preference.text.KuteTextPreference
import de.markusressel.mkdocseditor.R
import de.markusressel.mkdocseditor.event.BasicAuthPasswordChangedEvent
import de.markusressel.mkdocseditor.event.BasicAuthUserChangedEvent
import de.markusressel.mkdocseditor.event.HostChangedEvent
import de.markusressel.mkdocseditor.event.ThemeChangedEvent
import de.markusressel.mkdocseditor.view.IconHandler
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Holder for KutePreference items for easy access to preference values across the application
 */
@Singleton
class KutePreferencesHolder @Inject constructor(private val context: Context, private val iconHelper: IconHandler, private val dataProvider: KutePreferenceDataProvider) {

    val connectionCategory by lazy {
        KuteCategory(key = R.string.category_connection_key, icon = iconHelper.getPreferenceIcon(MaterialDesignIconic.Icon.gmi_wifi), title = context.getString(R.string.category_connection_title), description = context.getString(R.string.category_connection_description), children = listOf(connectionUriPreference, KuteDivider(key = R.string.divider_basic_auth_key, title = context.getString(R.string.divider_basic_auth_title)), basicAuthUserPreference, basicAuthPasswordPreference))
    }

    val connectionUriPreference by lazy {
        KuteTextPreference(key = R.string.connection_host_key, icon = iconHelper.getPreferenceIcon(MaterialDesignIconic.Icon.gmi_battery), title = context.getString(R.string.connection_host_title), defaultValue = "127.0.0.1", dataProvider = dataProvider, onPreferenceChangedListener = { old, new ->
            Bus
                    .send(HostChangedEvent(new))
        })
    }

    val basicAuthUserPreference by lazy {
        KuteTextPreference(key = R.string.connection_basic_auth_user_key, title = context.getString(R.string.connection_basic_auth_user_title), defaultValue = "", dataProvider = dataProvider, onPreferenceChangedListener = { old, new ->
            Bus
                    .send(BasicAuthUserChangedEvent(new))
        })
    }

    val basicAuthPasswordPreference by lazy {
        KutePasswordPreference(key = R.string.connection_basic_auth_password_key, title = context.getString(R.string.connection_basic_auth_password_title), defaultValue = "", dataProvider = dataProvider, onPreferenceChangedListener = { old, new ->
            Bus
                    .send(BasicAuthPasswordChangedEvent(new))
        })
    }

    val themePreference by lazy {
        KuteSingleSelectPreference(context = context, key = R.string.theme_key, icon = iconHelper.getPreferenceIcon(MaterialDesignIconic.Icon.gmi_colorize), title = context.getString(R.string.theme_title), possibleValues = mapOf(R.string.theme_dark_value to R.string.theme_dark_value_name, R.string.theme_light_value to R.string.theme_light_value_name), defaultValue = R.string.theme_dark_value, dataProvider = dataProvider, onPreferenceChangedListener = { old, new ->
            Bus
                    .send(ThemeChangedEvent(new))
        })
    }

}

