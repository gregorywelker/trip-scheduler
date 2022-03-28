package hu.bme.aut.android.tripscheduler.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

//Class for returning the database
@Database(entities = [Trip::class], version = 1)
@TypeConverters(Trip.Converters::class)
abstract class TripDatabase:RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object{
        fun getDatabase(applicationContext: Context):TripDatabase{
            return Room.databaseBuilder(
                applicationContext,
                TripDatabase::class.java,
                "trip-database"
            ).build()
        }
    }
}