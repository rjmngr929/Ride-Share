package com.my.raido.Helper

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale


class  LocaleHelper {

     fun setLocale(context: Context, languageCode: String): Context {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, languageCode)
        } else {
            updateResourcesLegacy(context, languageCode)
        }

//        return withContext(Dispatchers.Default) {
//            updateResources(context, languageCode)
//        }
    }

//    private fun updateResources(context: Context, languageCode: String): Context {
//        val locale = Locale(languageCode)
//        Locale.setDefault(locale)
//
//        val config = Configuration(context.resources.configuration)
//        config.setLocale(locale)
//
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            context.createConfigurationContext(config)
//        } else {
//            @Suppress("DEPRECATION")
//            context.resources.updateConfiguration(config, context.resources.displayMetrics)
//            context
//        }
//    }

    // For Android N and above
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    // For older versions
    private fun updateResourcesLegacy(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.locale = locale
        config.setLayoutDirection(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        return context
    }

}