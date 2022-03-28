package hu.bme.aut.android.tripscheduler.data

import androidx.room.*

//Database access object used for database actions
@Dao
interface TripDao {
    @Query("SELECT * FROM trip WHERE completed LIKE :completed")
    fun getAllByActive(completed:Boolean): MutableList<Trip>

    @Insert
    fun insert(trip: Trip):Long

    @Update
    fun update(trip: Trip)

    @Delete
    fun delete(trip: Trip)
}