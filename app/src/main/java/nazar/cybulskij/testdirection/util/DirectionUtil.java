package nazar.cybulskij.testdirection.util;

import nazar.cybulskij.testdirection.R;
import nazar.cybulskij.testdirection.model.Direction;

/**
 * Created by Nazarko on 1/15/2016.
 */
public class DirectionUtil {
    public  static Direction getDirectionfromString(String str){
        if (str.indexOf("right")!=-1){
            return  Direction.RIGHT;
        }
        if (str.indexOf("left")!=-1){
            return  Direction.LEFT;
        }

        return Direction.UP;
    }

}
