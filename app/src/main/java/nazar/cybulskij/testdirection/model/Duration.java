package nazar.cybulskij.testdirection.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nazar on 22.10.15.
 */
public class Duration {

    @SerializedName("value")
    String value;
    @SerializedName("text")
    String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
