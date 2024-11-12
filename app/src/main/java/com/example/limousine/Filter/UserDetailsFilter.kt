import android.widget.Filter
import com.example.limousine.Adapter.UserDetailsAdapter
import com.example.limousine.Model.UserDetailsModel

class UserDetailsFilter: Filter {

    //arrayList in which we want to search
    private var filterList: ArrayList<UserDetailsModel>

    //adapter in which filter need to be implemented
    private var userDetailsAdapter: UserDetailsAdapter

    //constructor
    constructor(
        filterList: ArrayList<UserDetailsModel>,
        userDetailsAdapter: UserDetailsAdapter
    ) : super() {
        this.filterList = filterList
        this.userDetailsAdapter = userDetailsAdapter
    }

    override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
        var constraint = constraint
        val results = Filter.FilterResults()

        //value should not be null and not empty
        if(constraint != null && constraint.isNotEmpty()) {

            // Convert constraint to lowercase to perform case-insensitive search
            val lowercaseConstraint = constraint.toString().toLowerCase()

            val filteredModels: ArrayList<UserDetailsModel> = ArrayList()

            for (i in 0 until filterList.size) {
                val fullName = filterList[i].fullName.toLowerCase()
                val id = filterList[i].id.toLowerCase()

                // Validate
                if (fullName.contains(lowercaseConstraint) || id.contains(lowercaseConstraint)) {
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
        userDetailsAdapter.userDetailsModelArrayList = results.values as ArrayList<UserDetailsModel>
        //notify changes
        userDetailsAdapter.notifyDataSetChanged()
    }
}