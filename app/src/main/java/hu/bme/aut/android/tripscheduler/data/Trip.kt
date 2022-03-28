package hu.bme.aut.android.tripscheduler.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList

//Entity data class holding trips
@Entity(tableName = "trip")
data class Trip(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id : Long? = null,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "completed") var completed:Boolean = false,
    @ColumnInfo(name = "stops") var stops: MutableList<Stop> = mutableListOf<Stop>()
)  {

    //Converter for the stops variable
    class Converters {

        @TypeConverter
        fun stopsToString(stops : List<Stop>): String {
            var gson:Gson = Gson()
            return  gson.toJson(stops)
        }

        @TypeConverter
        fun stopsFromString(stops: String): List<Stop> {
            val stopsType = object : TypeToken<List<Stop>>() {}.type
            return Gson().fromJson(stops, stopsType)
        }
    }

}