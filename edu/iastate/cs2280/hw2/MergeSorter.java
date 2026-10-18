package edu.iastate.cs2280.hw2;

import java.io.FileNotFoundException;
import java.lang.NumberFormatException; 
import java.lang.IllegalArgumentException; 
import java.util.InputMismatchException;

/**
 *  
 * @author Gabriel Vesperman
 *
 */

/**
 * 
 * This class implements the mergesort algorithm.   
 *
 */

public class MergeSorter extends AbstractSorter
{
	// Other private instance variables if needed
	
	/** 
	 * Constructor takes an array of points.  It invokes the superclass constructor, and also 
	 * set the instance variables algorithm in the superclass.
	 *  
	 * @param pts   input array of integers
	 */
	public MergeSorter(Point[] pts) 
	{
		// TODO
		super(pts);
		algorithm = "merge sort";
	}


	/**
	 * Perform mergesort on the array points[] of the parent class AbstractSorter. 
	 * 
	 */
	@Override 
	public void sort()
	{
		// TODO
		mergeSortRec(points);
	}

	
	/**
	 * This is a recursive method that carries out mergesort on an array pts[] of points. One 
	 * way is to make copies of the two halves of pts[], recursively call mergeSort on them, 
	 * and merge the two sorted subarrays into pts[].   
	 * 
	 * @param pts	point array 
	 */
	private void mergeSortRec(Point[] pts)
	{
		Point[] left = new Point[pts.length/2];
		Point[] right = new Point[pts.length- left.length];
		for(int i = 0; i < left.length; i++)
		{
			left[i] = pts[i];
		}
		for(int i = 0; i < right.length; i++)
		{
			right[i] = pts[i+left.length];
		}

		if(left.length > 1){
			mergeSortRec(left);
		}
		if(right.length > 1){
			mergeSortRec(right);
		}

		int i =0;
		int j =0;
		while( i < left.length && j < right.length)
		{
			if(pointComparator.compare(right[j], left[i]) < 0){
				pts[i+j]=right[j];
				j++;
			} else {
				pts[i+j]=left[i];
				i++;
			}
		}

		while(i < left.length){
			pts[i+j]=left[i];
			i++;
		}

		while(j < right.length){
			pts[i+j]=right[j];
			j++;
		}
	}

	
	// Other private methods if needed ...

}
