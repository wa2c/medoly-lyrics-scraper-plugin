package com.wa2c.android.medoly.plugin.action.lyricsscraper.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.TypedValue
import com.google.gson.Gson


/**
 * Preferences helper.
 */
class Prefs(val context: Context, name : String? = null) {

    /** Preferences. */
    val pref : SharedPreferences = if (name == null) {
        PreferenceManager.getDefaultSharedPreferences(context)
    } else {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }


    /** Check contains key. */
    fun contains(keyRes : Int) : Boolean {
        return contains(context.getString(keyRes))
    }
    /** Check contains key. */
    fun contains(key : String?) : Boolean {
        return pref.contains(key)
    }



    /** Get a value from preference. */
    fun getBoolean(keyRes : Int, default: Boolean = false, defRes : Int = -1) : Boolean {
        return getBoolean(context.getString(keyRes), default, defRes)
    }
    /** Get a value from preference. */
    fun getBoolean(key : String, default: Boolean = false, defRes : Int = -1) : Boolean {
        return if (defRes > 0)
            pref.getBoolean(key, context.resources.getBoolean(defRes))
        else
            pref.getBoolean(key, default)
    }

    /** Get a value from preference. */
    fun getInt(keyRes : Int, default: Int = 0, defRes : Int = -1) : Int {
        return getInt(context.getString(keyRes), default, defRes)
    }
    /** Get a value from preference. */
    fun getInt(key : String, default: Int = 0, defRes : Int = -1) : Int {
        return if (defRes > 0)
            pref.getInt(key, context.resources.getInteger(defRes))
        else
            pref.getInt(key, default)
    }

    /** Get a value from preference. */
    fun getLong(keyRes : Int, default: Long = 0, defRes : Int = -1) : Long {
        return getLong(context.getString(keyRes), default, defRes)
    }
    /** Get a value from preference. */
    fun getLong(key : String, default: Long = 0, defRes : Int = -1) : Long {
        return if (defRes > 0)
            pref.getLong(key, context.resources.getInteger(defRes).toLong())
        else
            pref.getLong(key, default)
    }

    /** Get a value from preference. */
    fun getFloat(keyRes : Int, default: Float = 0f, defRes : Int = -1): Float {
        return getFloat(context.getString(keyRes), default, defRes)
    }
    /** Get a value from preference. */
    fun getFloat(key : String, default: Float = 0f, defRes : Int = -1) : Float {
        return if (defRes > 0) {
            val v = TypedValue()
            context.resources.getValue(defRes, v, true)
            pref.getFloat(key, v.float)
        } else {
            pref.getFloat(key, default)
        }
    }

    /** Get a value from preference. */
    fun getString(keyRes : Int, default: String? = "", defRes : Int = -1) : String? {
        return getString(context.getString(keyRes), default, defRes)
    }
    /** Get a value from preference. */
    fun getString(key : String, default: String? = "", defRes : Int = -1) : String? {
        return if (defRes > 0)
            pref.getString(key, context.getString(defRes))
        else
            pref.getString(key, default)
    }

    /** Get a value from preference. */
    fun getStringSet(keyRes : Int, default: Set<String?>? = HashSet(), defRes : Int = -1) : Set<String?>? {
        return getStringSet(context.getString(keyRes), default, defRes)
    }
    /** Get a value from preference. */
    fun getStringSet(key : String, default: Set<String?>? = HashSet(), defRes : Int = -1) : Set<String?>? {
        return if (defRes > 0)
            pref.getStringSet(key, context.resources.getStringArray(defRes).toSet())
        else
            pref.getStringSet(key, default)
    }

    /** Get a value from preference. */
    inline fun <reified T> getObject(keyRes: Int): T? {
        return getObject(context.getString(keyRes))
    }
    /** Get a value from preference.  */
    inline fun <reified T> getObject(key: String): T? {
        val json = pref.getString(key, "")
        return Gson().fromJson(json, T::class.java)
    }



    /** Set a value to preference. */
    fun putValue(keyRes : Int, value : Boolean) : Prefs {
        putValue(context.getString(keyRes), value)
        return this
    }
    /** Set a value to preference. */
    fun putValue(key : String, value : Boolean) : Prefs {
        pref.edit().putBoolean(key, value).apply()
        return this
    }

    /** Set a value to preference. */
    fun putValue(keyRes : Int, value : Int) : Prefs {
        putValue(context.getString(keyRes), value)
        return this
    }
    /** Set a value to preference. */
    fun putValue(key : String, value : Int) : Prefs {
        pref.edit().putInt(key, value).apply()
        return this
    }

    /** Set a value to preference. */
    fun putValue(keyRes : Int, value : Long) : Prefs {
        putValue(context.getString(keyRes), value)
        return this
    }
    /** Set a value to preference. */
    fun putValue(key : String, value : Long) : Prefs {
        pref.edit().putLong(key, value).apply()
        return this
    }

    /** Set a value to preference. */
    fun putValue(keyRes : Int, value : Float) : Prefs {
        putValue(context.getString(keyRes), value)
        return this
    }
    /** Set a value to preference. */
    fun putValue(key : String, value : Float) : Prefs {
        pref.edit().putFloat(key, value).apply()
        return this
    }

    /** Set a value to preference. */
    fun putValue(keyRes : Int, value : CharSequence?) : Prefs {
        putValue(context.getString(keyRes), value)
        return this
    }
    /** Set a value to preference. */
    fun putValue(key : String, value : CharSequence?) : Prefs {
        pref.edit().putString(key, value?.toString()).apply()
        return this
    }

    /** Set a value to preference. */
    fun putValue(keyRes : Int, value : Set<String?>?) : Prefs {
        putValue(context.getString(keyRes), value)
        return this
    }
    /** Set a value to preference. */
    fun putValue(key : String, value : Set<String?>?) : Prefs {
        pref.edit().putStringSet(key, value).apply()
        return this
    }

    /** Set a value to preference. */
    fun putObject(keyRes : Int, value : Any?) : Prefs {
        putObject(context.getString(keyRes), value)
        return this
    }
    /** Set a value to preference. */
    fun putObject(key : String, value : Any?) : Prefs {
        val json = Gson().toJson(value)
        pref.edit().putString(key, json).apply()
        return this
    }



    /** Remove preference value. */
    fun remove(keyRes : Int) : Prefs {
        remove(context.getString(keyRes))
        return this
    }
    /** Remove preference value. */
    fun remove(key : String) : Prefs {
        pref.edit().remove(key).apply()
        return this
    }

}