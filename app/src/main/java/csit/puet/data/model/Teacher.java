package csit.puet.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class Teacher {

    @SerializedName("id_prep")
    @Expose
    private int idPrep;

    @SerializedName("name")
    @Expose
    private String name;

    public int getIdPrep() {
        return idPrep;
    }

    public String getTeacherName() {
        return name;
    }

    public void setIdPrep(int idPrep) {
        this.idPrep = idPrep;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
