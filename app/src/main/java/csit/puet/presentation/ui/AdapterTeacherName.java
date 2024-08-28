package csit.puet.presentation.ui;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import csit.puet.data.model.Teacher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterTeacherName extends ArrayAdapter<Teacher> {

    private final List<Teacher> originalData;
    private List<Teacher> filteredData;

    public AdapterTeacherName(Context context, List<Teacher> teachers) {
        super(context, android.R.layout.simple_dropdown_item_1line, teachers);
        this.originalData = new ArrayList<>(teachers);
        this.filteredData = new ArrayList<>(teachers);
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Teacher getItem(int position) {
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
                    List<Teacher> filterResultsData = new ArrayList<>();

                    for (Teacher teacher : originalData) {
                        if (teacher.getTeacherName().toLowerCase(new Locale("uk")).contains(filterString)) {
                            filterResultsData.add(teacher);
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
                filteredData = (List<Teacher>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
