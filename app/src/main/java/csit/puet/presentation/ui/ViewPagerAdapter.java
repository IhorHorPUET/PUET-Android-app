package csit.puet.presentation.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import csit.puet.data.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<List<Lesson>> catalogLessons;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<List<Lesson>> catalogLessons) {
        super(fragmentActivity);
        this.catalogLessons = catalogLessons;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ArrayList<Lesson> scheduleArrayList = new ArrayList<>(catalogLessons.get(position));
        return LessonFragment.newInstance(scheduleArrayList);
    }

    @Override
    public int getItemCount() {
        return catalogLessons.size();
    }
}
