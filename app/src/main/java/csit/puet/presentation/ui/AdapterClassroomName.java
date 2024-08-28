package csit.puet.presentation.ui;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import csit.puet.data.model.Classroom;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterClassroomName extends ArrayAdapter<Classroom> {

    private final List<Classroom> originalData;
    private List<Classroom> filteredData;

    public AdapterClassroomName(Context context, List<Classroom> classrooms) {
        super(context, android.R.layout.simple_dropdown_item_1line, classrooms);
        this.originalData = new ArrayList<>(classrooms);
        this.filteredData = new ArrayList<>(classrooms);
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Classroom getItem(int position) {
        return filteredData.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = originalData;
                    results.count = originalData.size();
                } else {
                    String filterString = constraint.toString().toLowerCase(new Locale("uk"));
                    List<Classroom> filterResultsData = new ArrayList<>();

                    for (Classroom classroom : originalData) {
                        if (classroom.getClassroomName().toLowerCase(new Locale("uk")).contains(filterString)) {
                            filterResultsData.add(classroom);
                        }
                    }
                    results.values = filterResultsData;
                    results.count = filterResultsData.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (List<Classroom>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
