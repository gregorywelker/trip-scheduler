package hu.bme.aut.android.tripscheduler.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.tripscheduler.adapter.TripAdapter
import hu.bme.aut.android.tripscheduler.R
import hu.bme.aut.android.tripscheduler.data.Stop
import hu.bme.aut.android.tripscheduler.data.Trip

//Handles both the active and completed lists of the trips
//the completed list does not allow editing just viewing
class TripsFragment(private val activeTrips:Boolean) : Fragment(), TripAdapter.TripItemListener {

    private lateinit var adapter: TripAdapter
    private lateinit var listener: TripFragment.TripListener
    private lateinit var tripFragment: TripFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_trips, container, false)
        val recyclerview = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerview.layoutManager = LinearLayoutManager(view.context)
        adapter = TripAdapter(this,activeTrips)
        recyclerview.adapter = adapter

        //Registering listener for creating new trips if this is the active trips tab, hide the button otherwise
        if(activeTrips){
            view.findViewById<FloatingActionButton>(R.id.new_trip_fab).setOnClickListener{
                EditTripDialogFragment(null).show(childFragmentManager,EditTripDialogFragment.TAG)
            }
        }else{
            view.findViewById<FloatingActionButton>(R.id.new_trip_fab).visibility = View.GONE
        }

        //Load trips initially
        listener = activity as TripFragment.TripListener
        listener.loadTrips(activeTrips)

        view.findViewById<TextView>(R.id.trips_type).text = if(activeTrips) "Active trips" else "Completed trips"

        return view
    }

    //Load the trips on resume because they might have changed on the other tab
    override fun onResume() {
        super.onResume()
        listener.loadTrips(activeTrips)
    }

    //Notify the adapter that the trips have been loaded
    fun onTripsLoaded(trips:MutableList<Trip>){
        adapter.trips = trips
        adapter.notifyDataSetChanged()
    }

    //Notify the adapter and the target trip that a trip has been updated
    fun onTripUpdated(trip:Trip, updateType:TripFragment.TripUpdateType, stop: Stop?, fromPosition: Int?, toPosition: Int?){
        if(updateType != TripFragment.TripUpdateType.COMPLETE_TRIP) {
            if(this::tripFragment.isInitialized) {
                tripFragment.onTripUpdated(updateType, stop, fromPosition, toPosition)
            }
            adapter.notifyItemChanged(adapter.trips.indexOf(trip))
        }else{
            adapter.notifyItemRemoved(fromPosition!!)
        }
    }

    //Notify the adapter that a new trip has been added
    fun onNewActiveTripCreated(trip:Trip) {
        adapter.insert(trip)
    }

    //Notify the adapter that a trip has been deleted
    fun onTripDeleted(position:Int) {
        adapter.notifyItemRemoved(position)
    }

    //Load the particular trip fragment when clicking on a trip
    override fun onTripItemClicked(trip: Trip) {
        tripFragment = TripFragment(trip,activeTrips)
        val trans: FragmentTransaction = parentFragmentManager.beginTransaction()
        trans.replace(R.id.frame_layout, tripFragment, TripFragment.TAG)
        trans.addToBackStack(null);
        trans.commit()
    }

    //Update the trip if a stop changed in the given trip
    override fun onTripStopItemCompleteChanged(trip:Trip, stop: Stop) {
        listener.onUpdateTrip(trip, TripFragment.TripUpdateType.UPDATE_STOP,stop,null,null)
    }

    //Update the trip if it`s completion has changed
    override fun onTripItemCompleteChanged(trip: Trip) {
        val index = adapter.trips.indexOf(trip)
        trip.completed = !trip.completed
        adapter.trips.remove(trip)
        listener.onUpdateTrip(trip, TripFragment.TripUpdateType.COMPLETE_TRIP,null,index,null)
    }
    //Delete the given trip
    override fun onTripItemDeleted(trip: Trip) {
        val index = adapter.trips.indexOf(trip)
        adapter.trips.remove(trip)
        listener.onDeleteTrip(trip, index)
    }

    companion object{
        const val activeTripsId:Long = 0
        const val activeTripsTAG:String = "f0"
        const val completeTripsId:Long = 1
        const val completeTripsTAG:String = "f1"
    }
}