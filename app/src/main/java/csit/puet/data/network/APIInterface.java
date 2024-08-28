package csit.puet.data.network;

import csit.puet.BuildConfig;
import csit.puet.data.model.Group;
import csit.puet.data.model.Classroom;
import csit.puet.data.model.Teacher;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

interface APIInterface {
    String token = BuildConfig.API_TOKEN;
    String url = "/api/v1/schedule/";

    @Headers(token)
    @GET(url + "teachers")
    Call<List<Teacher>> doGetListTeachers();

    @Headers(token)
    @GET(url + "rooms")
    Call<List<Classroom>> doGetListRooms();

    @Headers(token)
    @GET(url + "groups")
    Call<List<Group>> doGetListGroups();

    @Headers(token)
    @GET
    Call<ResponseBody> getRawData(@Url String url);
}
