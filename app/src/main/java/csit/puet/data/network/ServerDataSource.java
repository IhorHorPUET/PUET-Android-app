package csit.puet.data.network;

import csit.puet.data.DataCallback;
import csit.puet.data.DataUtils;
import csit.puet.data.model.Group;
import csit.puet.data.model.Lesson;
import csit.puet.data.model.Classroom;
import csit.puet.data.model.Teacher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServerDataSource {

    private final APIInterface apiInterface;

    // Constructor initializes the API client interface
    public ServerDataSource() {
        this.apiInterface = APIClient.getClient().create(APIInterface.class);
    }

    // Fetches the list of teachers, classrooms and groups from the server and returns it via the callback
    // If the fetch fails, an empty list is returned to ensure the app's stability
    public void getTeachersServerList(final DataCallback<List<Teacher>> callback) {
        Call<List<Teacher>> call = apiInterface.doGetListTeachers();
        DataUtils.fetchData(call, callback::onDataLoaded, error -> callback.onDataLoaded(new ArrayList<>()));
    }

    public void getClassroomsServerList(final DataCallback<List<Classroom>> callback) {
        Call<List<Classroom>> call = apiInterface.doGetListRooms();
        DataUtils.fetchData(call, callback::onDataLoaded, error -> callback.onDataLoaded(new ArrayList<>()));
    }

    public void getGroupsServerList(final DataCallback<List<Group>> callback) {
        Call<List<Group>> call = apiInterface.doGetListGroups();
        DataUtils.fetchData(call, callback::onDataLoaded, error -> callback.onDataLoaded(new ArrayList<>()));
    }


    // Fetches the list of lessons based on a dynamic URL (which includes query parameters)
    // Processes the JSON data into Lesson objects and sorts them before returning
    // Returns an empty list in case of any failure during the fetch or data processing
    public void getLessonsServerList(String dynamicUrl, final DataCallback<List<Lesson>> callback) {
        String fullUrl = "/api/staging/schedule/classes/table?" + dynamicUrl + "language=uk";
        Call<ResponseBody> call = apiInterface.getRawData(fullUrl);
        DataUtils.fetchData(call, responseBody -> {
            try {
                String jsonData = responseBody.string();
                List<Lesson> lessonList = processJsonData(jsonData);
                callback.onDataLoaded(lessonList);
            } catch (IOException e) {
                callback.onDataLoaded(new ArrayList<>());
            }
        }, error -> callback.onDataLoaded(new ArrayList<>()));
    }

    // Converts JSON data into a list of Lesson objects
    // Parses each lesson's details and constructs Lesson objects accordingly
    // Sorts the final list of lessons before returning
    private List<Lesson> processJsonData(String jsonData) {
        List<Lesson> lessonList = new ArrayList<>();
        JsonArray jsonArray = JsonParser.parseString(jsonData).getAsJsonArray();

        for (JsonElement element : jsonArray) {
            JsonObject dateObject = element.getAsJsonObject();
            String date = dateObject.has("date") ? dateObject.get("date").getAsString() : "";
            JsonArray classesArray = dateObject.has("classes") ? dateObject.getAsJsonArray("classes") : new JsonArray();

            for (JsonElement classElement : classesArray) {
                JsonObject classObject = classElement.getAsJsonObject();
                JsonArray lessonsArray = classObject.getAsJsonObject("class").getAsJsonArray("lessons");
                if (!lessonsArray.isJsonNull() && !lessonsArray.isEmpty()) {
                    JsonObject lessonDetailsObject = lessonsArray.get(0).getAsJsonObject();
                    int num = classObject.has("num") ? classObject.get("num").getAsInt() : 0;

                    String lesson = lessonDetailsObject.has("lesson") && !lessonDetailsObject.get("lesson").isJsonNull() ? lessonDetailsObject.get("lesson").getAsString() : "";
                    String lessonType = lessonDetailsObject.has("lessonType") && !lessonDetailsObject.get("lessonType").isJsonNull() ? lessonDetailsObject.get("lessonType").getAsString() : "";
                    String teacher = lessonDetailsObject.has("teacher") && !lessonDetailsObject.get("teacher").isJsonNull() ? lessonDetailsObject.get("teacher").getAsString() : "";
                    String room = lessonDetailsObject.has("room") && !lessonDetailsObject.get("room").isJsonNull() ? lessonDetailsObject.get("room").getAsString() : "";

                    JsonElement groupsElement = classObject.getAsJsonObject("class").has("groups") ? classObject.getAsJsonObject("class").get("groups") : null;
                    String groupNames = groupsElement != null ? DataUtils.extractGroupNames(groupsElement) : "";

                    Lesson lessonDetails = new Lesson(date, num, lesson, lessonType, groupNames, room, teacher);
                    lessonList.add(lessonDetails);
                }
            }
        }
        // Sorts the list based on the natural ordering of the lessons
        lessonList.sort(Comparator.comparing(Lesson::getDate).thenComparing(Lesson::getNum));
        return lessonList;
    }
}
