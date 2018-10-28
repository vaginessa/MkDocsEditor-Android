package de.markusressel.mkdocseditor.navigation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Created by Markus on 08.01.2018.
 */
class NavigationPage(val activityClass: Class<*>? = null, val fragment: (() -> androidx.fragment.app.Fragment)? = null, val tag: String? = null) {

    fun createIntent(context: Context): Intent {
        return Intent(context, activityClass)
    }

}