package edu.iastate.cs2280.hw2;

import java.util.Comparator;

public class PointYComparator implements Comparator<Point> {
    public PointYComparator() {
        Point.setXorY(false);
    }
    @Override
    public int compare(Point p1, Point p2) {
        return(p1.compareTo(p2));
    }
}
