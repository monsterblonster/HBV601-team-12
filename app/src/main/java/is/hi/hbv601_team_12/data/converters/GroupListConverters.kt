package `is`.hi.hbv601_team_12.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GroupListConverters {
    private val gson = Gson()

    @TypeConverter
    @JvmStatic
    fun fromStringList(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @TypeConverter
    @JvmStatic
    fun fromLongList(list: List<Long>?): String {
        return gson.toJson(list ?: emptyList<Long>())
    }

    @TypeConverter
    @JvmStatic
    fun toLongList(json: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
