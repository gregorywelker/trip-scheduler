package hu.bme.aut.android.tripscheduler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentTransaction
import hu.bme.aut.android.tripscheduler.data.Stop
import hu.bme.aut.android.tripscheduler.data.Trip
import hu.bme.aut.android.tripscheduler.data.TripDatabase
import hu.bme.aut.android.tripscheduler.databinding.ActivityMainBinding
import hu.bme.aut.android.tripscheduler.fragment.TripsFragment
import hu.bme.aut.android.tripscheduler.fragment.TripFragment
import hu.bme.aut.android.tripscheduler.fragment.TripsViewPagerFragment
import kotlin.concurrent.thread


//Main activity that is listening to trip action callbacks and handles the database moreover the communication between various fragments
class MainActivity : AppCompatActivity(), TripFragment.TripListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database : TripDatabase

    //Creating database connection and view pager fragment that holds the active and completed trips
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = TripDatabase.getDatabase(applicationContext)

        val viewPagerFragment = TripsViewPagerFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, viewPagerFragment, TripsViewPagerFragment.TAG)
        transaction.commit()
    }

    //Interface implementation for creating new trips
    override fun onCreateNewActiveTrip(tripName: String) {
        thread {
            val trip = Trip(name = tripName)
            val tripId = database.tripDao().insert(trip)
            trip.id = tripId
            val activeTripsFrag :TripsFragment = supportFragmentManager.findFragmentByTag(TripsFragment.activeTripsTAG) as TripsFragment
            runOnUiThread {
                activeTripsFrag.onNewActiveTripCreated(trip)
            }
        }
    }

    //Interface implementation for updating trips
    override fun onUpdateTrip(trip:Trip, updateType:TripFragment.TripUpdateType, stop: Stop?, fromPosition:Int?, toPosition:Int?) {
        thread {
            database.tripDao().update(trip)
            val targetSwap = if(updateType == TripFragment.TripUpdateType.COMPLETE_TRIP) trip.completed else !trip.completed
            val target = supportFragmentManager.findFragmentByTag(if(targetSwap) TripsFragment.activeTripsTAG else TripsFragment.completeTripsTAG ) as TripsFragment
            runOnUiThread {
                target.onTripUpdated(trip,updateType,stop,fromPosition,toPosition)
            }
        }
    }

    //Interface implementation for loading trips from the database
    override fun loadTrips(activeTrips:Boolean) {
        thread {
            val trips :MutableList<Trip> = database.tripDao().getAllByActive(!activeTrips)
            val target = supportFragmentManager.findFragmentByTag(if(activeTrips) TripsFragment.activeTripsTAG else TripsFragment.completeTripsTAG) as TripsFragment
            runOnUiThread {
                target.onTripsLoaded(trips)
            }
        }
    }

    //Interface implementation for deleting trips
    override fun onDeleteTrip(trip: Trip, position:Int) {
        thread {
            database.tripDao().delete(trip)
            val target = supportFragmentManager.findFragmentByTag(if(!trip.completed) TripsFragment.activeTripsTAG else TripsFragment.completeTripsTAG ) as TripsFragment
            runOnUiThread {
                target.onTripDeleted(position)
            }
        }
    }
}


