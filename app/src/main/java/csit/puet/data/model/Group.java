package csit.puet.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class Group {

    private int id;
    @SerializedName("course")
    @Expose
    private int course;
    @SerializedName("spec_id")
    @Expose
    private int specId;
    @SerializedName("forma")
    @Expose
    private int forma;
    @SerializedName("owner")
    @Expose
    private int owner;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("num")
    @Expose
    private int num;

    public int getId() {
        return id;
    }

    public int getCourse() {
        return course;
    }

    public int getSpecId() {
        return specId;
    }

    public int getForma() {
        return forma;
    }

    public int getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }


    public void setId(int id) {
        this.id = id;
    }
    public void setCourse(int course) {
        this.course = course;
    }

    public void setSpecId(int specId) {
        this.specId = specId;
    }

    public void setForma(int forma) {
        this.forma = forma;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getGroupBand() {
        return String.valueOf("course=" + course + "&" +
                "spec_id=" + specId + "&" + "num=" + num +
                "&" + "forma=" + forma + "&" + "owner=" + owner + "&");
    }

    public String allString() {
        return "Group{" +
                "course=" + course +
                ", specId=" + specId +
                ", forma=" + forma +
                ", owner=" + owner +
                ", name='" + name + '\'' +
                ", num=" + num +
                '}';
    }
}
