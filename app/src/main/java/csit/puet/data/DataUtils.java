package csit.puet.data;

import androidx.annotation.NonNull;

import csit.puet.AppConstants;
import csit.puet.data.model.Lesson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class DataUtils {

     public static List<List<Lesson>> sortLessonsList(List<List<Lesson>> allLessonsList) {
        List<List<Lesson>> sortedLessonsList = new ArrayList<>(allLessonsList);
        sortedLessonsList.forEach(subList -> subList.sort((lesson1, lesson2) -> {
            int yearComparison = Integer.compare(Integer.parseInt(lesson1.getDate().substring(6)),
                    Integer.parseInt(lesson2.getDate().substring(6)));
            if (yearComparison != 0) {
                return yearComparison;
            }
            int monthComparison = Integer.compare(Integer.parseInt(lesson1.getDate().substring(3, 5)),
                    Integer.parseInt(lesson2.getDate().substring(3, 5)));
            if (monthComparison != 0) {
                return monthComparison;
            }
            int dayComparison = Integer.compare(Integer.parseInt(lesson1.getDate().substring(0, 2)),
                    Integer.parseInt(lesson2.getDate().substring(0, 2)));
            if (dayComparison != 0) {
                return dayComparison;
            }
            int numComparison = Integer.compare(lesson1.getNum(), lesson2.getNum());
            if (numComparison != 0) {
                return numComparison;
            }
            return lesson1.getGroup().compareTo(lesson2.getGroup());
        }));
        return sortedLessonsList;
    }

    public static <T> void fetchData(Call<T> call, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.accept(response.body());
                } else {
                    onError.accept(new RuntimeException("Сервер не відповідає"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                onError.accept(t);
            }
        });
    }

    public static <T, R> void saveListToDatabase(Executor executor, List<T> list, Function<T, R> converter, Consumer<List<R>> daoMethod) {
        executor.execute(() -> {
            List<R> entities = list.stream().map(converter).collect(Collectors.toList());
            daoMethod.accept(entities);
        });
    }

    public static String extractGroupNames(JsonElement groupsElement) {
        StringBuilder groupNames = new StringBuilder();

        if (groupsElement.isJsonArray()) {
            JsonArray groupsArray = groupsElement.getAsJsonArray();
            for (JsonElement element : groupsArray) {
                addGroupNames(groupNames, element);
            }
        } else if (groupsElement.isJsonObject()) {
            JsonObject groupsObject = groupsElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : groupsObject.entrySet()) {
                addGroupNames(groupNames, entry.getValue());
            }
        }
        return groupNames.toString();
    }

    private static void addGroupNames(StringBuilder groupNames, JsonElement element) {
        if (element.isJsonObject() && element.getAsJsonObject().has("name")) {
            String name = element.getAsJsonObject().get("name").getAsString();
            if (groupNames.length() > 0) {
                groupNames.append("\n");
            }
            groupNames.append(name);
        }
    }

    public static void saveSearchBands(Context context, List<String> searchBands) {
        SharedPreferences prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefSet.edit();
        Gson gson = new Gson();
        String json = gson.toJson(searchBands);
        editor.putString("searchBands", json);
        editor.apply();
    }

    public static List<String> loadSearchBands(Context context) {
        SharedPreferences prefSet = context.getSharedPreferences(AppConstants.PREF_SET, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefSet.getString("searchBands", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
