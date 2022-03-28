package hu.bme.aut.android.tripscheduler.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import hu.bme.aut.android.tripscheduler.R

private const val NUM_PAGES = 2

//View pager for the active trips and completed trips
//On the first page it return the active on the second the completed trips
class TripsViewPagerFragment : Fragment() {
    private lateinit var viewPager: ViewPager2

    //Inflating view and adding the adapted
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        viewPager = view.findViewById(R.id.pager)
        val pagerAdapter = activity?.let { ViewPagerAdapter(it) }
        viewPager.adapter = pagerAdapter

        return view
    }

    //Adapter for the view pager that returns the actual fragments
    private inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        //Returns the correct custom fragment id
        override fun getItemId(position: Int): Long {
            return when(position){
                1-> TripsFragment.completeTripsId
                else -> TripsFragment.activeTripsId
            }
        }

        //Creates the fragment based on the position
        override fun createFragment(position: Int): Fragment{
            return when(position){
                1-> TripsFragment(false)
                else -> TripsFragment(true)
            }
        }
    }

    companion object{
        const val TAG = "TripsViewPagerFragment"
    }
}