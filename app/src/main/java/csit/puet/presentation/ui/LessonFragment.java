package csit.puet.presentation.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import csit.puet.R;
import csit.puet.data.model.Lesson;

import java.io.Serializable;
import java.util.ArrayList;

public class LessonFragment extends Fragment {

    private static final String ARG_SCHEDULE = "schedule";
    private ArrayList<Lesson> lesson;

    public static LessonFragment newInstance(ArrayList<Lesson> lesson) {
        LessonFragment fragment = new LessonFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SCHEDULE, lesson);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Serializable serializable = getArguments().getSerializable(ARG_SCHEDULE);
            if (serializable instanceof ArrayList<?>) {
                ArrayList<?> arrayList = (ArrayList<?>) serializable;
                lesson = new ArrayList<>();
                for (Object item : arrayList) {
                    if (item instanceof Lesson) {
                        lesson.add((Lesson) item);
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lesson, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (lesson == null || lesson.isEmpty()) {
            return;
        }
        RecyclerView recyclerView = view.findViewById(R.id.schedule_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        LessonAdapter adapter = new LessonAdapter(lesson);
        recyclerView.setAdapter(adapter);
    }
}
