package edu.iastate.cs2280.hw2;

/**
 *
 * @author
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;


/**
 *
 * This class sorts all the points in an array of 2D points to determine a reference point whose x and y
 * coordinates are respectively the medians of the x and y coordinates of the original points.
 *
 * It records the employed sorting algorithm as well as the sorting time for comparison.
 *
 */
public class PointScanner
{
	private Point[] points;

	private Point medianCoordinatePoint;  // point whose x and y coordinates are respectively the medians of
	                                      // the x coordinates and y coordinates of those points in the array points[].
	private Algorithm sortingAlgorithm;

	protected long scanTime; 	       // execution time in nanoseconds.

	/**
	 * This constructor accepts an array of points and one of the four sorting algorithms as input. Copy
	 * the points into the array points[].
	 *
	 * @param  pts  input array of points
	 * @throws IllegalArgumentException if pts == null or pts.length == 0.
	 */
	public PointScanner(Point[] pts, Algorithm algo) throws IllegalArgumentException
	{
		if(pts == null || pts.length == 0)
		{
			throw new IllegalArgumentException();
		}
		points = new Point[pts.length];
		for(int i = 0; i < pts.length; i++)
		{
			points[i] = pts[i];
		}

		sortingAlgorithm = algo;
	}


	/**
	 * This constructor reads points from a file.
	 *
	 * @param  inputFileName
	 * @throws FileNotFoundException
	 * @throws InputMismatchException   if the input file contains an odd number of integers
	 */
	protected PointScanner(String inputFileName, Algorithm algo) throws FileNotFoundException, InputMismatchException
	{
		// TODO
		sortingAlgorithm = algo;
		Scanner scanner = new Scanner(new File(inputFileName));
		ArrayList<Point> pointList = new ArrayList<>();
		while(scanner.hasNextInt())
		{
			int x = scanner.nextInt();

			if (!scanner.hasNextInt())
			{
				scanner.close();
				throw new InputMismatchException();
			}

			int y = scanner.nextInt();
			pointList.add(new Point(x, y));
		}

		scanner.close();

		if (pointList.isEmpty())
		{
			throw new IllegalArgumentException();
		}

		points = new Point[pointList.size()];
		for(int i = 0; i < points.length; i++){
			points[i] = pointList.get(i);
		}
	}


	/**
	 * Carry out two rounds of sorting using the algorithm designated by sortingAlgorithm as follows:
	 *
	 *     a) Sort points[] by the x-coordinate to get the median x-coordinate.
	 *     b) Sort points[] again by the y-coordinate to get the median y-coordinate.
	 *     c) Construct medianCoordinatePoint using the obtained median x- and y-coordinates.
	 *
	 * Based on the value of sortingAlgorithm, create an object of SelectionSorter, InsertionSorter, MergeSorter,
	 * or QuickSorter to carry out sorting.
	 * @param algo
	 * @return
	 */
	public void scan()
	{
		// TODO
		AbstractSorter aSorter;

		// create an object to be referenced by aSorter according to sortingAlgorithm. for each of the two
		// rounds of sorting, have aSorter do the following:
		//
		//     a) call setComparator() with an argument 0 or 1.
		//
		//     b) call sort().
		//
		//     c) use a new Point object to store the coordinates of the medianCoordinatePoint
		//
		//     d) set the medianCoordinatePoint reference to the object with the correct coordinates.
		//
		//     e) sum up the times spent on the two sorting rounds and set the instance variable scanTime.

		if(sortingAlgorithm == Algorithm.SelectionSort){
			aSorter = new SelectionSorter(points);
		} else if (sortingAlgorithm == Algorithm.InsertionSort) {
			aSorter = new InsertionSorter(points);
		} else if (sortingAlgorithm == Algorithm.MergeSort) {
			aSorter = new MergeSorter(points);
		} else {
			aSorter = new QuickSorter(points);
		}

		long startTime = 0;
		long time1=0;
		long time2=0;

		aSorter.setComparator(0);
		startTime = System.nanoTime();
		aSorter.sort();
		time1 = System.nanoTime() - startTime;
		Point p1 = new Point(aSorter.getMedian().getX(), aSorter.getMedian().getY());

		aSorter.setComparator(1);
		startTime = System.nanoTime();
		aSorter.sort();
		time2 = System.nanoTime() - startTime;
		Point p2 = new Point(aSorter.getMedian().getX(), aSorter.getMedian().getY());

		medianCoordinatePoint = new Point(p1.getX(), p2.getY());
		scanTime = time1+time2;
	}


	/**
	 * Outputs performance statistics in the format:
	 *
	 * <sorting algorithm> <size>  <time>
	 *
	 * For instance,
	 *
	 * selection sort   1000	  9200867
	 *
	 * Use the spacing in the sample run in Section 2 of the project description.
	 */
	public String stats()
	{
		//return null;
		// TODO
		//return sortingAlgorithm+" "+points.length+" "+scanTime;
		return String.format("%-16s %-5s %-11d", sortingAlgorithm, points.length, scanTime);
	}


	/**
	 * Write MCP after a call to scan(),  in the format "MCP: (x, y)"   The x and y coordinates of the point are displayed on the same line with exactly one blank space
	 * in between.
	 */
	@Override
	public String toString()
	{
		//return null;
		// TODO
		return "MCP: (" + medianCoordinatePoint.getX() + ", " + medianCoordinatePoint.getY() + ")";
	}


	/**
	 *
	 * This method, called after scanning, writes point data into a file by outputFileName. The format
	 * of data in the file is the same as printed out from toString().  The file can help you verify
	 * the full correctness of a sorting result and debug the underlying algorithm.
	 *
	 * @throws FileNotFoundException
	 */
	public void writeMCPToFile() throws FileNotFoundException
	{
		// TODO
		// 1. Define your output file name (and path)
		/*String outputFileName = "output.txt";
		Path outputPath = Paths.get(outputFileName);

		// 2. Content to write
		String content = toString();

		try {
			// 3. Write the content to the file
			Files.writeString(outputPath, content);
			//System.out.println("Successfully wrote to " + outputFileName);
		} catch (IOException e) {
			//System.err.println("An error occurred while writing the file.");
			e.printStackTrace();
		}*/

		String outputFileName = "output.txt";
		PrintWriter writer = new PrintWriter(outputFileName);
		writer.println(this);
		writer.close();
	}




}
