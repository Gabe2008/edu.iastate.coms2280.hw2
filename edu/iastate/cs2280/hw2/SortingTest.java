//AI Generated Test

package edu.iastate.cs2280.hw2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * Checks that all four sorters (selection, insertion, merge, quick) sort correctly,
 * and that CompareSorters.main runs. Expected answers come from Arrays.sort, not from your code.
 */
class SortingTest {

    // ---- the four sorters under test -------------------------------------------------
    private static final String[] NAMES = {"SelectionSorter", "InsertionSorter", "MergeSorter", "QuickSorter"};
    private static final Function<Point[], AbstractSorter>[] MAKERS = makers();

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Function<Point[], AbstractSorter>[] makers() {
        return new Function[]{
                (Function<Point[], AbstractSorter>) SelectionSorter::new,
                (Function<Point[], AbstractSorter>) InsertionSorter::new,
                (Function<Point[], AbstractSorter>) MergeSorter::new,
                (Function<Point[], AbstractSorter>) QuickSorter::new};
    }

    // ---- helpers ---------------------------------------------------------------------
    /** order 0 = by x (ties by y), order 1 = by y (ties by x). */
    private static Comparator<Point> expectedOrder(int order) {
        return order == 0
                ? Comparator.comparingInt(Point::getX).thenComparingInt(Point::getY)
                : Comparator.comparingInt(Point::getY).thenComparingInt(Point::getX);
    }

    private static Point[] randomPoints(int n, int range, long seed) {
        Random r = new Random(seed);
        Point[] p = new Point[n];
        for (int i = 0; i < n; i++) {
            p[i] = new Point(r.nextInt(2 * range + 1) - range, r.nextInt(2 * range + 1) - range);
        }
        return p;
    }

    /** Sort `input` by x and by y with every algorithm and compare to Arrays.sort. */
    private static void checkAllSorters(Point[] input, String description) {
        for (int a = 0; a < MAKERS.length; a++) {
            for (int order = 0; order <= 1; order++) {
                AbstractSorter sorter = MAKERS[a].apply(input);
                sorter.setComparator(order);
                sorter.sort();
                Point[] actual = new Point[input.length];
                sorter.getPoints(actual);

                Point[] expected = input.clone();
                Arrays.sort(expected, expectedOrder(order));

                assertArrayEquals(expected, actual,
                        NAMES[a] + ", " + (order == 0 ? "by x" : "by y") + ", " + description);
            }
        }
    }

    // ---- sorting tests ---------------------------------------------------------------
    @Test
    void sortsOnePoint() {
        checkAllSorters(new Point[]{new Point(3, -3)}, "1 point");
    }

    @Test
    void sortsTwoPoints() {
        checkAllSorters(new Point[]{new Point(2, 2), new Point(1, 1)}, "2 points, out of order");
        checkAllSorters(new Point[]{new Point(1, 1), new Point(2, 2)}, "2 points, in order");
    }

    @Test
    void breaksTiesUsingTheOtherCoordinate() {
        // lots of equal x values (and, for the y sort, lots of equal y values)
        Point[] pts = {new Point(5, 9), new Point(5, -3), new Point(1, 7), new Point(5, 0),
                new Point(-2, 7), new Point(1, -8), new Point(5, 4), new Point(-2, 7)};
        checkAllSorters(pts, "ties");
    }

    @Test
    void sortsAlreadySortedAndReverseSortedInput() {
        Point[] pts = randomPoints(300, 50, 1);
        Arrays.sort(pts, expectedOrder(0));
        checkAllSorters(pts, "already sorted by x");
        Arrays.sort(pts, expectedOrder(0).reversed());
        checkAllSorters(pts, "reverse sorted by x");
    }

    @Test
    void sortsAllIdenticalPoints() {
        Point[] pts = new Point[200];
        for (int i = 0; i < pts.length; i++) pts[i] = new Point(4, 4);
        checkAllSorters(pts, "all identical");
    }

    @Test
    void sortsLotsOfDuplicates() {
        checkAllSorters(randomPoints(1000, 2, 3), "1000 points but only 25 different ones");
    }

    @Test
    void sortsManyRandomSizes() {
        int[] sizes = {2, 3, 4, 5, 8, 9, 16, 17, 33, 100, 257, 1000};
        for (int n : sizes) {
            for (long seed = 0; seed < 3; seed++) {
                checkAllSorters(randomPoints(n, 50, seed * 100 + n), "random, n=" + n);
            }
        }
    }

    @Test
    void sortsLargestAllowedInput() {
        checkAllSorters(randomPoints(10201, 50, 7), "10,201 random points");
    }

    @Test
    void sortingByXThenByYWorksOnTheSameSorter() {
        // this is what PointScanner.scan() does
        Point[] pts = randomPoints(500, 50, 9);
        for (int a = 0; a < MAKERS.length; a++) {
            AbstractSorter sorter = MAKERS[a].apply(pts);
            sorter.setComparator(0);
            sorter.sort();
            sorter.setComparator(1);
            sorter.sort();
            Point[] actual = new Point[pts.length];
            sorter.getPoints(actual);
            Point[] expected = pts.clone();
            Arrays.sort(expected, expectedOrder(1));
            assertArrayEquals(expected, actual, NAMES[a] + " sorted by x then y");
        }
    }

    @Test
    void sortersDoNotModifyTheArrayTheyWereGiven() {
        Point[] pts = randomPoints(50, 50, 11);
        Point[] copy = pts.clone();
        for (Function<Point[], AbstractSorter> make : MAKERS) {
            AbstractSorter s = make.apply(pts);
            s.setComparator(0);
            s.sort();
        }
        assertArrayEquals(copy, pts);
    }

    @Test
    void constructorsRejectNullAndEmpty() {
        for (Function<Point[], AbstractSorter> make : MAKERS) {
            assertThrows(IllegalArgumentException.class, () -> make.apply(null));
            assertThrows(IllegalArgumentException.class, () -> make.apply(new Point[0]));
        }
    }

    @Test
    void medianOfAssignmentExampleIsZeroAndOne() {
        // the 17 points from the assignment PDF: median x = 0, median y = 1
        Point[] pts = {new Point(0, 0), new Point(-3, -9), new Point(0, -10), new Point(8, 4),
                new Point(3, 3), new Point(-6, 3), new Point(-2, 1), new Point(10, 5), new Point(-7, -10),
                new Point(5, -2), new Point(7, 3), new Point(10, 5), new Point(-7, -10), new Point(0, 8),
                new Point(-1, -6), new Point(-10, 0), new Point(5, 5)};
        for (int a = 0; a < MAKERS.length; a++) {
            AbstractSorter sorter = MAKERS[a].apply(pts);
            sorter.setComparator(0);
            sorter.sort();
            assertEquals(0, sorter.getMedian().getX(), NAMES[a] + " median x");
            sorter.setComparator(1);
            sorter.sort();
            assertEquals(1, sorter.getMedian().getY(), NAMES[a] + " median y");
        }
    }

    // ---- CompareSorters.main ---------------------------------------------------------
    /** Runs main() with the given keyboard input and returns what it printed. */
    private static String runMain(String keyboardInput) throws Exception {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(keyboardInput.getBytes("UTF-8")));
            System.setOut(new PrintStream(buffer, true, "UTF-8"));
            CompareSorters.main(new String[0]);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return buffer.toString("UTF-8");
    }

    private static void assertTableHasFourRowsWithSize(String output, String size) {
        for (String name : new String[]{"SelectionSort", "InsertionSort", "MergeSort", "QuickSort"}) {
            boolean found = false;
            for (String line : output.split("\\R")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 3 && parts[0].equals(name) && parts[1].equals(size)
                        && Long.parseLong(parts[2]) >= 0) {
                    found = true;
                }
            }
            assertTrue(found, "no row for " + name + " with size " + size + " in:\n" + output);
        }
    }

    @Test
    void mainExitsCleanlyOnKey3() throws Exception {
        String out = runMain("3\n");
        assertTrue(out.startsWith("Performances of Four Sorting Algorithm"), out);
        assertTrue(out.contains("keys: 1 (random integers) 2 (file input) 3 (exit)"), out);
        assertTrue(out.contains("Trial 1: "), out);
    }

    @Test
    void mainRunsRandomTrial() throws Exception {
        String out = runMain("1\n500\n3\n");
        assertTrue(out.contains("Enter number of random points: "), out);
        assertTableHasFourRowsWithSize(out, "500");
    }

    @Test
    void mainRunsFileTrial() throws Exception {
        Path file = Files.createTempFile("hw2points", ".txt");
        try {
            Files.write(file, ("0 0 -3 -9 0 -10\n8 4 3 3 -6\n3 -2 1\n10 5 -7 -10\n5 -2\n"
                    + "7 3 10 5\n-7 -10 0 8\n-1 -6\n-10 0\n5 5\n").getBytes("UTF-8"));
            String out = runMain("2\n" + file + "\n3\n");
            assertTrue(out.contains("Points from a file"), out);
            assertTableHasFourRowsWithSize(out, "17");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void mainRunsSeveralTrialsInARow() throws Exception {
        String out = runMain("1\n50\n1\n60\n1\n70\n3\n");
        assertTrue(out.contains("Trial 4: "), out);
        assertTableHasFourRowsWithSize(out, "50");
        assertTableHasFourRowsWithSize(out, "60");
        assertTableHasFourRowsWithSize(out, "70");
    }
}