package csit.puet.presentation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import csit.puet.R;
import csit.puet.data.model.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterGroupName extends ArrayAdapter<Group> {

    private final List<Group> originalData;
    private List<Group> filteredData;
    private final List<Group> selectedGroups = new ArrayList<>();
    private final List<String> groupBands = new ArrayList<>();

    public AdapterGroupName(Context context, List<Group> groups) {
        super(context, android.R.layout.simple_dropdown_item_1line, groups);
        this.originalData = new ArrayList<>(groups);
        this.filteredData = new ArrayList<>(groups);
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Group getItem(int position) {
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
                    List<Group> filterResultsData = new ArrayList<>();
                    for (Group group : originalData) {
                        if (group.getName().toLowerCase(new Locale("uk")).contains(filterString)) {
                            filterResultsData.add(group);
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
                filteredData = (List<Group>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_group_with_checkbox, parent, false);
            holder = new ViewHolder();
            holder.checkBox = convertView.findViewById(R.id.checkbox_group);
            holder.textView = convertView.findViewById(R.id.textview_group_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Group group = getItem(position);
        if (group != null) {
            holder.textView.setText(group.getName());
            holder.checkBox.setChecked(selectedGroups.contains(group));

            holder.checkBox.setOnClickListener(v -> {
                boolean isChecked = holder.checkBox.isChecked();
                if (isChecked) {
                    if (!selectedGroups.contains(group)) {
                        selectedGroups.add(group);
                        groupBands.add(group.getGroupBand()); // Добавляем данные группы в список groupBands
                    }
                } else {
                    selectedGroups.remove(group);
                    groupBands.remove(group.getGroupBand()); // Удаляем данные группы из списка groupBands
                }

            });
        }

        return convertView;
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView textView;
    }

    public void clearSelectedGroups() {
        selectedGroups.clear(); // Очищаем список
        groupBands.clear(); // Очищаем также список groupBands
        notifyDataSetChanged();  // Уведомляем об изменении данных, чтобы обновить представление
    }

    public List<Group> getSelectedGroups() {
        return new ArrayList<>(selectedGroups); // Возвращаем копию списка для безопасности
    }
}
