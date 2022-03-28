package hu.bme.aut.android.tripscheduler.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.tripscheduler.R
import hu.bme.aut.android.tripscheduler.adapter.TripStopsAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.bme.aut.android.tripscheduler.data.*
import java.util.*
import android.view.MotionEvent

//Fragment for displaying the trips and their data and letting users edit the trip`s data if it is active
class TripFragment(private val trip: Trip, private val activeTrips:Boolean): Fragment() , TripStopsAdapter.StopItemListener {

    private lateinit var adapter:TripStopsAdapter
    private lateinit var listener: TripListener
    private lateinit var tripName:TextView
    private var isMovingItem:Boolean = false
    private var moveFrom:Int = -1
    private var moveTo:Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip, container, false)
        listener = context as TripListener

        val recyclerview = view.findViewById<RecyclerView>(R.id.trip_stops_recycler_view)
        recyclerview.layoutManager = LinearLayoutManager(view.context)
        adapter = TripStopsAdapter(trip,this,activeTrips)
        recyclerview.adapter = adapter

        tripName = view.findViewById<TextView>(R.id.trip_name)
        tripName.text = trip.name

        if(activeTrips) {
            //Adding the listener to the FAB
            val newDest = view.findViewById<FloatingActionButton>(R.id.new_destination_fab)
            newDest.setOnClickListener {
                EditStopDialogFragment(trip, null).show(
                    childFragmentManager,
                    EditStopDialogFragment.TAG
                )
            }

            //Adding listener for gestures, needed for rearrangement and delete
            val stopItemTouchHelper = ItemTouchHelper(stopItemGestureCallback)
            stopItemTouchHelper.attachToRecyclerView(recyclerview)

            //Adding listener for trip name edit
            tripName.setOnClickListener {
                EditTripDialogFragment(trip).show(
                    childFragmentManager,
                    EditTripDialogFragment.TAG
                )
            }

            //Touch listener for touch release, triggers trip update if an item has been moved
            recyclerview.setOnTouchListener { v, event ->

                if (event.action == MotionEvent.ACTION_UP) {
                    if (isMovingItem) {
                        isMovingItem = false
                        listener.onUpdateTrip(
                            trip,
                            TripUpdateType.MOVE_STOP,
                            null,
                            moveFrom,
                            moveTo
                        )
                        moveFrom = -1
                        moveTo = -1
                    }
                }
                false
            }
        }else{
            //Disabling FAB if trip is complete
            view.findViewById<FloatingActionButton>(R.id.new_destination_fab).visibility = View.GONE
        }
        return view
    }

    //Gesture handling for rearrangement and deletion
    private var stopItemGestureCallback: SimpleCallback = object: SimpleCallback(UP or DOWN, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.absoluteAdapterPosition
            val toPosition = target.absoluteAdapterPosition

            if(trip.stops[fromPosition].completed) {
                return false
            }
            return !trip.stops[toPosition].completed
        }

        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            Collections.swap(trip.stops, fromPos, toPos)
            val temp = trip.stops[fromPos].date
            trip.stops[fromPos].date = trip.stops[toPos].date
            trip.stops[toPos].date = temp
            adapter.notifyItemMoved(fromPos,toPos)
            if(moveFrom == -1)
            {
                moveFrom = fromPos
            }
            moveTo = toPos
            isMovingItem = true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            trip.stops.removeAt(position)
            listener.onUpdateTrip(trip,TripUpdateType.REMOVE_STOP,null, position, null)
        }
    }

    //Updating the trip based on the update type
    fun onTripUpdated(updateType:TripUpdateType, stop:Stop?, fromPosition: Int?,toPosition: Int?) {
        when(updateType){
            TripUpdateType.RENAME_TRIP ->{  tripName.text = trip.name }
            TripUpdateType.CREATE_STOP ->{ adapter.notifyItemInserted(trip.stops.size - 1)}
            TripUpdateType.UPDATE_STOP ->{ adapter.notifyItemRangeChanged(trip.stops.indexOf(stop) - 1, 3)}
            TripUpdateType.MOVE_STOP ->{
                adapter.notifyItemRangeChanged(if(fromPosition!! < toPosition!!) fromPosition else toPosition,
                    kotlin.math.abs(fromPosition - toPosition) + 1
                )
            }
            TripUpdateType.REMOVE_STOP ->{
                adapter.notifyItemRemoved(fromPosition!!)
                adapter.notifyItemRangeChanged(trip.stops.indexOf(stop) - 1, 3)
            }
        }
    }

    //Interface for trip handling
    interface TripListener{
        fun onUpdateTrip(trip:Trip, updateType:TripUpdateType, stop:Stop?, fromPosition: Int?, toPosition: Int?)
        fun onCreateNewActiveTrip(tripName:String)
        fun loadTrips(completed:Boolean)
        fun onDeleteTrip(trip:Trip, position:Int)
    }

    //Enum for knowing what type of update has been made on the trip
    enum class TripUpdateType {
        RENAME_TRIP, COMPLETE_TRIP,CREATE_STOP,UPDATE_STOP,MOVE_STOP,REMOVE_STOP
    }

    companion object{
        const val TAG:String = "TripFragment"
    }

    //Calling the stop edit dialog if a stop has been clicked
    override fun onStopItemClicked(stop: Stop) {
        EditStopDialogFragment(trip, stop).show(childFragmentManager, EditStopDialogFragment.TAG)
    }

    //Updating the trip if a stop has been updated
    override fun onStopItemCompleteChanged(stop:Stop) {
        listener.onUpdateTrip(trip, TripUpdateType.UPDATE_STOP,stop,null,null)
    }
}