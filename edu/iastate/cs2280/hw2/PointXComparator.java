package edu.iastate.cs2280.hw2;

import java.util.Comparator;

public class PointXComparator implements Comparator<Point> {
    public PointXComparator() {
        Point.setXorY(true);
    }
    @Override
    public int compare(Point p1, Point p2) {
        return(p1.compareTo(p2));
    }
}
