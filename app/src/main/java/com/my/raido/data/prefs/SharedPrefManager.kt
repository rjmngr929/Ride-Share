package com.my.raido.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SharedPrefManager @Inject constructor(@ApplicationContext context: Context) {

    private val mPref: SharedPreferences = context.getSharedPreferences(
        context.applicationContext.packageName,
        Context.MODE_PRIVATE
    )

    private var mEditor: SharedPreferences.Editor? = null

    fun put(key: String, value: Any) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Double -> putDouble(key, value)
            is Boolean -> putBoolean(key, value)
        }
    }

    fun get(key: String): Any {
        return mPref.all[key] ?: ""
    }

    fun <T> putObject(key: String, value: T) {
        putString(key, Gson().toJson(value))
    }


    fun <T> getObject(key: String, theClass: Class<T>): T? {
        return Gson().fromJson(getString(key, ""), theClass)
    }

    // Save LatLng to SharedPreferences
    fun putLatLng(key: String, latLng: LatLng) {
        putString(key, Gson().toJson(latLng))
    }

    // Retrieve LatLng from SharedPreferences
    fun getLatLng(key: String): LatLng? {
        val json = getString(key, "")
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, LatLng::class.java)
        } else {
            null
        }
    }

    fun putString(key: String, value: String) {
        doEdit()
        mEditor?.putString(key, value)
        doCommit()
    }

    fun getString(key: String): String? {
        return mPref.getString(key, "")
    }

    fun getString(key: String, defaultValue: String): String? {
        return mPref.getString(key, defaultValue)
    }

    fun saveCurrentLatLng(key: String, latLng: com.google.android.gms.maps.model.LatLng) {
        doEdit()
        // Convert LatLng to a string (e.g., "latitude,longitude")
        val latLngString = "${latLng.latitude},${latLng.longitude}"

        // Save the string in SharedPreferences
        mEditor?.putString(key, latLngString)
        doCommit() // Or use commit() to write synchronously
    }

    fun getCurrentLatLng(key: String,): LatLng? {
        // Retrieve the saved string
        val latLngString = mPref.getString(key, null)

        // If the string is not null, convert it back to LatLng
        return latLngString?.let {
            val parts = it.split(",")
            if (parts.size == 2) {
                val latitude = parts[0].toDoubleOrNull()
                val longitude = parts[1].toDoubleOrNull()

                if (latitude != null && longitude != null) {
                    LatLng(latitude, longitude) // Return LatLng object
                } else {
                    null // Return null if conversion fails
                }
            } else {
                null
            }
        }
    }

    // Save enum to SharedPreferences
    fun <T : Enum<T>> saveEnum( key: String, value: T) {
        doEdit()
        mEditor?.putString(key, value.name)
        doCommit()
    }

    // Retrieve enum from SharedPreferences
    fun <T : Enum<T>> getEnum( key: String, enumClass: Class<T>, defaultValue: T): T {
        val storedValue = mPref.getString(key, null)
        return if (storedValue != null) {
            java.lang.Enum.valueOf(enumClass, storedValue)
        } else {
            defaultValue
        }
    }

    fun putInt(key: String, value: Int) {
        doEdit()
        mEditor?.putInt(key, value)
        doCommit()
    }

    fun getInt(key: String): Int {
        return mPref.getInt(key, 0)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return mPref.getInt(key, defaultValue)
    }

    fun putDouble(key: String, value: Double) {
        doEdit()
        mEditor?.putString(key, value.toString())
        doCommit()
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        val result = mPref.getString(key, "")
        return java.lang.Double.parseDouble(result)
    }

    fun putBoolean(key: String, value: Boolean) {
        doEdit()
        mEditor?.putBoolean(key, value)
        doCommit()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return mPref.getBoolean(key, defaultValue)
    }

    fun putStringArrayList(key: String, arrayList: ArrayList<String>) {
        val jsonStr = Gson().toJson(arrayList) // ArrayList to Json String
        putString(key, jsonStr)
    }

    fun getStringArrayList(key: String): ArrayList<String> {
        val jsonStr = getString(key, "")
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson<ArrayList<String>>(jsonStr, type)
    }

    fun putIntegerArrayList(key: String, arrayList: ArrayList<Int>) {
        val jsonStr = Gson().toJson(arrayList) // ArrayList to json string
        putString(key, jsonStr)
    }

    fun getIntegerArrayList(key: String): ArrayList<Int> {
        val jsonStr = mPref.getString(key, "")
        val type = object : TypeToken<ArrayList<Int>>() {}.type
        return Gson().fromJson<ArrayList<Int>>(jsonStr, type)
    }

    fun putStringHashMap(key: String, map: HashMap<String, String>) {
        val jsonStr = Gson().toJson(map)
        putString(key, jsonStr)
    }

    fun getStringHashMap(key: String): HashMap<String, String>? {
        val jsonStr = mPref.getString(key, null)
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        return Gson().fromJson<HashMap<String, String>>(jsonStr, type)
    }

    fun firstTimeAskingPermission(permission: String, isFirstTime: Boolean) {
        putBoolean(permission, isFirstTime)
    }

    fun isFirstTimeAskingPermission(permission: String): Boolean {
        return getBoolean(permission, true)
    }

    private fun doEdit() {
        if (mEditor == null) {
            mEditor = mPref.edit()
        }
    }

    private fun doCommit() {
        if (mEditor != null) {
            mEditor!!.apply()
            mEditor = null
        }
    }

    fun remove(key: String) {
        doEdit()
        mEditor?.remove(key)
        doCommit()
    }

    fun removeAll() {
        doEdit()
        mEditor?.clear()
        doCommit()
    }

}


/**
 * .....................................................................
 *  Difference between apply() and commit() in SharedPreference
 * .....................................................................
 * 1. apply() commits without returning a boolean indicating success or failure. commit() returns true if the save works, false otherwise.
 * 2. apply() is faster. commit() is slower.
 * 3. apply(): Asynchronous commit(): Synchronous
 * 4. apply() was added as the Android dev team noticed that almost no one took notice of the return value, so apply is faster as it is asynchronous.
 * 5. apply() writes to a temporary Map that is later written asynchronously to disk. If you immediately use methods like getBoolean() in your case,
 *    it will first lookup if there is a value for this key in the temporary Map and returns it.
 * 6. Even if you call a commit() after apply(), its safe because all the commit calls will be blocked until the apply()’s commit and the commit()’s commit
 *    (you called after apply()) is done.
 * */