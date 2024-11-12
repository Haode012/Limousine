import android.widget.Filter
import com.example.limousine.Adapter.BookingDoneAdapter
import com.example.limousine.Model.BookingModel

class BookingDoneFilter: Filter {

    //arrayList in which we want to search
    private var filterList: ArrayList<BookingModel>

    //adapter in which filter need to be implemented
    private var bookingDoneAdapter : BookingDoneAdapter

    //constructor
    constructor(
        filterList: ArrayList<BookingModel>,
        bookingDoneAdapter: BookingDoneAdapter
    ) : super() {
        this.filterList = filterList
        this.bookingDoneAdapter = bookingDoneAdapter
    }

    override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
        var constraint = constraint
        val results = Filter.FilterResults()

        //value should not be null and not empty
        if(constraint != null && constraint.isNotEmpty()) {

            // Convert constraint to lowercase to perform case-insensitive search
            val lowercaseConstraint = constraint.toString().toLowerCase()

            val filteredModels: ArrayList<BookingModel> = ArrayList()

            for (i in 0 until filterList.size) {
                val id = filterList[i].id.toLowerCase()
                val pickUpLocation = filterList[i].pickUpLocation.toLowerCase()
                val dropOffLocation = filterList[i].dropOffLocation.toLowerCase()

                // Validate
                if (id.contains(lowercaseConstraint) || pickUpLocation.contains(lowercaseConstraint) || dropOffLocation.contains(lowercaseConstraint)) {
                    // Add to filtered list
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //search value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results //don't miss it
    }

    override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
        //apply filter changes
        bookingDoneAdapter.bookingDoneModelArrayList = results.values as ArrayList<BookingModel>
        //notify changes
        bookingDoneAdapter.notifyDataSetChanged()
    }
}