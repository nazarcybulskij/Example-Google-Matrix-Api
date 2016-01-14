package nazar.cybulskij.testdirection.model_entity;

import android.location.Location;

/**
 * Created by Nazarko on 1/14/2016.
 */
public class GeneralPoint {


    Boolean isSpeack=false;
    Boolean isSelect =false;

    Location locaton ;
    String instruction;

    public GeneralPoint(String instruction, Location locaton) {
        this.instruction = instruction;
        this.locaton = locaton;
    }

    public String getInstruction() {
        return instruction;
    }

    public Location getLocaton() {
        return locaton;
    }

    public Boolean getIsSpeack() {
        return isSpeack;
    }

    public void setIsSpeack(Boolean isSpeack) {
        this.isSpeack = isSpeack;
    }

    public Boolean getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(Boolean isSelect) {
        this.isSelect = isSelect;
    }

}
