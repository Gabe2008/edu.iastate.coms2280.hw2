package edu.iastate.cs2280.hw2;

/**
 *
 * @author Gabriel Vesperman
 *
 */

import java.util.Comparator;

public class PointYComparator implements Comparator<Point> {
    /*public PointYComparator() {
        Point.setXorY(false);
    }*/
    @Override
    public int compare(Point p1, Point p2) {
        Point.setXorY(false);
        return(p1.compareTo(p2));
    }
}
