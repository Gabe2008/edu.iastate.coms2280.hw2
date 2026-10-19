package edu.iastate.cs2280.hw2;

/**
 *  
 * @author Gabriel Vesperman
 *
 */

/**
 * 
 * This class executes four sorting algorithms: selection sort, insertion sort, mergesort, and
 * quicksort, over randomly generated integers as well integers from a file input. It compares the 
 * execution times of these algorithms on the same input. 
 *
 */

import java.io.FileNotFoundException;
import java.util.Scanner; 
import java.util.Random; 


public class CompareSorters 
{
	/**
	 * Repeatedly take integer sequences either randomly generated or read from files. 
	 * Use them as coordinates to construct points.  Scan these points with respect to their 
	 * median coordinate point four times, each time using a different sorting algorithm.  
	 * 
	 * @param args
	 **/

	public static void main(String[] args) throws FileNotFoundException
	{		
		// TODO 
		// 
		// Conducts multiple rounds of comparison of four sorting algorithms.  Within each round, 
		// set up scanning as follows: 
		// 
		//    a) If asked to scan random points, calls generateRandomPoints() to initialize an array 
		//       of random points. 
		// 
		//    b) Reassigns to the array scanners[] (declared below) the references to four new 
		//       PointScanner objects, which are created using four different values  
		//       of the Algorithm type:  SelectionSort, InsertionSort, MergeSort and QuickSort. 
		// 
		// 	
		PointScanner[] scanners = new PointScanner[4];
		
		// For each input of points, do the following. 
		// 
		//     a) Initialize the array scanners[].  
		//
		//     b) Iterate through the array scanners[], and have every scanner call the scan() 
		//        method in the PointScanner class.  
		//
		//     c) After all four scans are done for the input, print out the statistics table from
		//		  section 2.
		//
		// A sample scenario is given in Section 2 of the project description.

		System.out.println("Performances of Four Sorting Algorithms in Point Scanning");
		System.out.println();
		System.out.println("keys: 1 (random integers) 2 (file input) 3 (exit)");

		Scanner s = new Scanner(System.in);

		int trial = 0;
		while (true)
		{
			trial+=1;
			int userChoice = 0;
			System.out.print("Trial "+trial+": ");
			try {
                userChoice = s.nextInt();
            } catch (java.util.InputMismatchException e) {
				System.out.println(e.getMessage());
				s.close();
				return;
			}

			if(userChoice == 1){
				System.out.print("Enter number of random points: ");
				int points = 0;
				try {
					points = s.nextInt();
				} catch (java.util.InputMismatchException e) {
					System.out.println(e.getMessage());
					s.close();
					return;
				}

				Point[] pts = null;
				pts = generateRandomPoints(points, new Random());

				scanners[0] = new PointScanner(pts, Algorithm.SelectionSort);
				scanners[1] = new PointScanner(pts, Algorithm.InsertionSort);
				scanners[2] = new PointScanner(pts, Algorithm.MergeSort);
				scanners[3] = new PointScanner(pts, Algorithm.QuickSort);
			} else if(userChoice == 2){
				System.out.println("Points from a file");
				System.out.print("File name: ");
				String filename = s.nextLine();
				try {
					filename = s.nextLine();
				} catch (java.util.InputMismatchException e) {
					System.out.println(e.getMessage());
					s.close();
					return;
				}
				try {
					PointScanner test = new PointScanner(filename, Algorithm.SelectionSort);
				} catch (java.io.FileNotFoundException e) {
					System.out.println(e.getMessage());
					s.close();
					return;
				} catch (java.util.InputMismatchException e) {
					System.out.println(e.getMessage());
					s.close();
					return;
				}
				scanners[0] = new PointScanner(filename, Algorithm.SelectionSort);
				scanners[1] = new PointScanner(filename, Algorithm.InsertionSort);
				scanners[2] = new PointScanner(filename, Algorithm.MergeSort);
				scanners[3] = new PointScanner(filename, Algorithm.QuickSort);

			} else if(userChoice == 3){
				break;
			} else {
				System.out.println("Invalid choice");
				s.close();
				return;
			}

			System.out.println();

			scanners[0].scan();
			scanners[1].scan();
			scanners[2].scan();
			scanners[3].scan();

			System.out.printf("%-16s %-5s %-11s%n", "algorithm", "size", "time (ns)");
			System.out.println("----------------------------------");
			System.out.println(scanners[0].stats());
			System.out.println(scanners[1].stats());
			System.out.println(scanners[2].stats());
			System.out.println(scanners[3].stats());
			System.out.println("----------------------------------");
			System.out.println();
		}
	}
	
	
	/**
	 * This method generates a given number of random points.
	 * The coordinates of these points are pseudo-random numbers within the range 
	 * [-50,50] x [-50,50]. Please refer to Section 3 on how such points can be generated.
	 * 
	 * Ought to be private. Made public for testing. 
	 * 
	 * @param numPts  	number of points
	 * @param rand      Random object to allow seeding of the random number generator
	 * @throws IllegalArgumentException if numPts < 1
	 */
	public static Point[] generateRandomPoints(int numPts, Random rand) throws IllegalArgumentException
	{ 
		//return null;
		// TODO
		if (numPts < 1) {
			throw new IllegalArgumentException();
		}
		Point[] points = new Point[numPts];

		int min=-50;
		int max=50;

		for (int i = 0; i < numPts; i++) {
			points[i] = new Point(rand.nextInt(max-min+1)+min, rand.nextInt(max-min+1)+min);
		}

		return points;
	}
	
}
