package nazar.cybulskij.testdirection.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nazar on 22.10.15.
 */
public class Step {

    @SerializedName("travel_mode")
    String travel_mode;
    @SerializedName("transit_details")
    TransitDetail detail;


    public String getTravel_mode() {
        return travel_mode;
    }

    public void setTravel_mode(String travel_mode) {
        this.travel_mode = travel_mode;
    }

    public TransitDetail getDetail() {
        return detail;
    }

    public void setDetail(TransitDetail detail) {
        this.detail = detail;
    }




}
