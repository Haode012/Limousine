import android.widget.Filter
import com.example.limousine.Adapter.ContactsAdapterAdmin
import com.example.limousine.Model.ContactsModel

class ContactsFilterAdmin: Filter {

    //arrayList in which we want to search
    private var filterList: ArrayList<ContactsModel>

    //adapter in which filter need to be implemented
    private var contactsAdapterAdmin: ContactsAdapterAdmin

    //constructor
    constructor(
        filterList: ArrayList<ContactsModel>,
        contactsAdapterAdmin: ContactsAdapterAdmin
    ) : super() {
        this.filterList = filterList
        this.contactsAdapterAdmin = contactsAdapterAdmin
    }

    override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
        var constraint = constraint
        val results = Filter.FilterResults()

        //value should not be null and not empty
        if(constraint != null && constraint.isNotEmpty()) {

            // Convert constraint to lowercase to perform case-insensitive search
            val lowercaseConstraint = constraint.toString().toLowerCase()

            val filteredModels: ArrayList<ContactsModel> = ArrayList()

            for (i in 0 until filterList.size) {
                val fullName = filterList[i].fullName.toLowerCase()

                // Validate
                if (fullName.contains(lowercaseConstraint)) {
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
        contactsAdapterAdmin.contactsModelArrayList = results.values as ArrayList<ContactsModel>
        //notify changes
        contactsAdapterAdmin.notifyDataSetChanged()
    }
}