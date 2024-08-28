package csit.puet.data;

import csit.puet.data.model.Lesson;

import java.util.List;

public interface DataCallbackAllLessons {
    void onDataLoaded(List<List<Lesson>> allLessons);
    void onError(Throwable throwable);
}
