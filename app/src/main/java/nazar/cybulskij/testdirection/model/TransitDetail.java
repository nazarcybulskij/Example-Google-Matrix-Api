package nazar.cybulskij.testdirection.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nazar on 22.10.15.
 */
public class TransitDetail {



    @SerializedName("line")
    Line line;




    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }




}
