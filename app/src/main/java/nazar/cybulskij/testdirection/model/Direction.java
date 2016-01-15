package nazar.cybulskij.testdirection.model;

import nazar.cybulskij.testdirection.R;

/**
 * Created by Nazarko on 1/15/2016.
 */
public enum Direction {
    LEFT(R.drawable.arrow_up_left),
    RIGHT(R.drawable.arrow_up_right),
    UP(R.drawable.arrow_up);


     Direction(int arrow) {
        value = arrow;
    }

    private final int value;

    public int getValue() { return value; }
}
