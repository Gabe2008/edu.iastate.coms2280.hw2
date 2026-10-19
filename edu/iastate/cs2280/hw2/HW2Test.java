package edu.iastate.cs2280.hw2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.InputMismatchException;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * All the HW2 tests in one file.
 *
 * - the four sorters (selection, insertion, merge, quick): checked against Arrays.sort
 * - Point and the two comparators
 * - the rest of AbstractSorter (getPoints, swap, setComparator, algorithm names)
 * - PointScanner: array + file constructors, scan, stats, toString, writeMCPToFile (output.txt)
 * - CompareSorters: generateRandomPoints and main (normal use and error handling)
 *
 * Expected answers come from Arrays.sort / hand-computed values, not from your own code.
 */
class HW2Test {

    private static final Algorithm[] ALL = Algorithm.values();
    // the assignment's example file (17 points, MCP = (0, 1))
    private static final String SPEC_FILE =
            "0 0 -3 -9 0 -10\n8 4 3 3 -6\n3 -2 1\n10 5 -7 -10\n5 -2\n7 3 10 5\n-7 -10 0 8\n-1 -6\n-10 0\n5 5\n";

    private final java.util.List<Path> tempFiles = new java.util.ArrayList<>();
    private byte[] savedOutputTxt;   // ./output.txt is backed up and restored around every test

    @BeforeEach
    void setUp() throws Exception {
        File out = new File("output.txt");
        savedOutputTxt = out.exists() ? Files.readAllBytes(out.toPath()) : null;
    }

    @AfterEach
    void tearDown() throws Exception {
        for (Path p : tempFiles) Files.deleteIfExists(p);
        File out = new File("output.txt");
        if (savedOutputTxt != null) Files.write(out.toPath(), savedOutputTxt);
        else out.delete();
    }

    // ---- helpers ---------------------------------------------------------------------
    private String tempFile(String content) throws Exception {
        Path p = Files.createTempFile("hw2test", ".txt");
        tempFiles.add(p);
        Files.write(p, content.getBytes("UTF-8"));
        return p.toString();
    }

    private static Point[] randomPoints(int n, long seed) {
        Random r = new Random(seed);
        Point[] p = new Point[n];
        for (int i = 0; i < n; i++) p[i] = new Point(r.nextInt(101) - 50, r.nextInt(101) - 50);
        return p;
    }

    /** Independent median: sort the coordinates themselves, take index (n-1)/2 (n should be odd). */
    private static String expectedMcp(Point[] pts) {
        int n = pts.length;
        int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) { xs[i] = pts[i].getX(); ys[i] = pts[i].getY(); }
        Arrays.sort(xs);
        Arrays.sort(ys);
        return "MCP: (" + xs[n / 2] + ", " + ys[n / 2] + ")";
    }

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

    // ================================================================ shared sorter helpers
    private static final String[] NAMES = {"SelectionSorter", "InsertionSorter", "MergeSorter", "QuickSorter"};
    private static final Function<Point[], AbstractSorter>[] MAKERS = makers();

    @SuppressWarnings("unchecked")
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

    private static Point[] randomPointsInRange(int n, int range, long seed) {
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

    // ================================================================ sorting (all four algorithms) ---------------------------------------------------------------
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
        Point[] pts = randomPointsInRange(300, 50, 1);
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
        checkAllSorters(randomPointsInRange(1000, 2, 3), "1000 points but only 25 different ones");
    }

    @Test
    void sortsManyRandomSizes() {
        int[] sizes = {2, 3, 4, 5, 8, 9, 16, 17, 33, 100, 257, 1000};
        for (int n : sizes) {
            for (long seed = 0; seed < 3; seed++) {
                checkAllSorters(randomPointsInRange(n, 50, seed * 100 + n), "random, n=" + n);
            }
        }
    }

    @Test
    void sortsLargestAllowedInput() {
        checkAllSorters(randomPointsInRange(10201, 50, 7), "10,201 random points");
    }

    @Test
    void sortingByXThenByYWorksOnTheSameSorter() {
        // this is what PointScanner.scan() does
        Point[] pts = randomPointsInRange(500, 50, 9);
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
        Point[] pts = randomPointsInRange(50, 50, 11);
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

    // ================================================================ CompareSorters.main (normal use)
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

    // ================================================================ Point
    @Test
    void pointConstructorsAndGetters() {
        assertEquals(0, new Point().getX());
        assertEquals(0, new Point().getY());
        Point p = new Point(-3, 8);
        assertEquals(-3, p.getX());
        assertEquals(8, p.getY());
        Point copy = new Point(p);
        assertNotSame(p, copy);
        assertEquals(p, copy);
    }

    @Test
    void pointToStringAndEquals() {
        assertEquals("(3, -4)", new Point(3, -4).toString());
        assertEquals(new Point(1, 2), new Point(1, 2));
        assertFalse(new Point(1, 2).equals(new Point(2, 1)));
        assertFalse(new Point(1, 2).equals(null));
        assertFalse(new Point(1, 2).equals("(1, 2)"));
    }

    @Test
    void setXorYChangesTheFlag() {
        Point.setXorY(true);
        assertTrue(Point.xORy);
        Point.setXorY(false);
        assertFalse(Point.xORy);
    }

    @Test
    void compareToByX() {
        Point.setXorY(true);
        assertEquals(-1, new Point(1, 100).compareTo(new Point(2, -100)));
        assertEquals(1, new Point(2, -100).compareTo(new Point(1, 100)));
        assertEquals(-1, new Point(5, 1).compareTo(new Point(5, 2)));   // tie on x -> compare y
        assertEquals(1, new Point(5, 2).compareTo(new Point(5, 1)));
        assertEquals(0, new Point(5, 5).compareTo(new Point(5, 5)));
    }

    @Test
    void compareToByY() {
        Point.setXorY(false);
        assertEquals(-1, new Point(100, 1).compareTo(new Point(-100, 2)));
        assertEquals(1, new Point(-100, 2).compareTo(new Point(100, 1)));
        assertEquals(-1, new Point(1, 5).compareTo(new Point(2, 5)));   // tie on y -> compare x
        assertEquals(1, new Point(2, 5).compareTo(new Point(1, 5)));
        assertEquals(0, new Point(5, 5).compareTo(new Point(5, 5)));
    }

    @Test
    void compareToIsConsistentInBothModes() {
        Random r = new Random(4);
        for (boolean mode : new boolean[]{true, false}) {
            Point.setXorY(mode);
            for (int i = 0; i < 2000; i++) {
                Point a = new Point(r.nextInt(7) - 3, r.nextInt(7) - 3);
                Point b = new Point(r.nextInt(7) - 3, r.nextInt(7) - 3);
                assertEquals(Integer.signum(a.compareTo(b)), -Integer.signum(b.compareTo(a)), a + " vs " + b);
                assertEquals(a.equals(b), a.compareTo(b) == 0, a + " vs " + b);
            }
        }
    }

    // ================================================================ comparators
    @Test
    void xComparatorOrdersByXThenY() {
        PointXComparator c = new PointXComparator();
        assertTrue(c.compare(new Point(1, 9), new Point(2, 0)) < 0);
        assertTrue(c.compare(new Point(2, 0), new Point(1, 9)) > 0);
        assertTrue(c.compare(new Point(2, 0), new Point(2, 1)) < 0);
        assertEquals(0, c.compare(new Point(2, 1), new Point(2, 1)));
    }

    @Test
    void yComparatorOrdersByYThenX() {
        PointYComparator c = new PointYComparator();
        assertTrue(c.compare(new Point(9, 1), new Point(0, 2)) < 0);
        assertTrue(c.compare(new Point(0, 2), new Point(9, 1)) > 0);
        assertTrue(c.compare(new Point(0, 2), new Point(1, 2)) < 0);
        assertEquals(0, c.compare(new Point(1, 2), new Point(1, 2)));
    }

    @Test
    void eachComparatorKeepsItsOwnCoordinateWhenUsedTogether() {
        PointXComparator x = new PointXComparator();
        PointYComparator y = new PointYComparator();
        Point a = new Point(1, 9), b = new Point(2, 0);
        for (int i = 0; i < 3; i++) {   // alternate: the shared flag must not leak between them
            assertTrue(x.compare(a, b) < 0, "x comparator: (1,9) before (2,0)");
            assertTrue(y.compare(a, b) > 0, "y comparator: (1,9) after (2,0)");
        }
    }

    // ================================================================ AbstractSorter (rest)
    @Test
    void sortersSetTheirAlgorithmNames() {
        Point[] one = {new Point(1, 1)};
        assertEquals("selection sort", new SelectionSorter(one).algorithm);
        assertEquals("insertion sort", new InsertionSorter(one).algorithm);
        assertEquals("merge sort", new MergeSorter(one).algorithm);
        assertEquals("quick sort", new QuickSorter(one).algorithm);
    }

    @Test
    void setComparatorOnlyAcceptsZeroAndOne() {
        AbstractSorter s = new SelectionSorter(new Point[]{new Point(1, 1)});
        assertDoesNotThrow(() -> s.setComparator(0));
        assertDoesNotThrow(() -> s.setComparator(1));
        assertThrows(IllegalArgumentException.class, () -> s.setComparator(-1));
        assertThrows(IllegalArgumentException.class, () -> s.setComparator(2));
    }

    @Test
    void getPointsAndSwapWork() {
        AbstractSorter s = new SelectionSorter(new Point[]{new Point(1, 1), new Point(2, 2), new Point(3, 3)});
        Point[] out = new Point[3];
        s.getPoints(out);
        assertArrayEquals(new Point[]{new Point(1, 1), new Point(2, 2), new Point(3, 3)}, out);
        s.swap(0, 2);
        s.getPoints(out);
        assertArrayEquals(new Point[]{new Point(3, 3), new Point(2, 2), new Point(1, 1)}, out);
        s.swap(1, 1);   // same index: nothing changes
        s.getPoints(out);
        assertArrayEquals(new Point[]{new Point(3, 3), new Point(2, 2), new Point(1, 1)}, out);
    }

    @Test
    void getMedianIsTheMiddleElementOfTheSortedArray() {
        Point[] pts = randomPoints(101, 8);
        AbstractSorter s = new MergeSorter(pts);
        s.setComparator(0);
        s.sort();
        Point[] sorted = new Point[pts.length];
        s.getPoints(sorted);
        assertEquals(sorted[50], s.getMedian());
    }

    // ================================================================ PointScanner: array constructor
    @Test
    void scannerArrayConstructorRejectsNullAndEmpty() {
        for (Algorithm a : ALL) {
            assertThrows(IllegalArgumentException.class, () -> new PointScanner((Point[]) null, a));
            assertThrows(IllegalArgumentException.class, () -> new PointScanner(new Point[0], a));
        }
    }

    @Test
    void scannerCopiesInputAndDoesNotReorderIt() {
        Point[] in = {new Point(3, 3), new Point(1, 1), new Point(2, 2)};
        Point[] snapshot = in.clone();
        PointScanner s = new PointScanner(in, Algorithm.MergeSort);
        in[0] = new Point(50, 50);   // changing the caller's array afterwards must not matter
        in[1] = new Point(50, 50);
        s.scan();
        assertEquals("MCP: (2, 2)", s.toString());
        for (Algorithm a : ALL) {
            new PointScanner(snapshot, a).scan();
        }
        assertArrayEquals(new Point[]{new Point(3, 3), new Point(1, 1), new Point(2, 2)}, snapshot);
    }

    @Test
    void scannerSinglePointIsItsOwnMedian() {
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(new Point[]{new Point(-7, 9)}, a);
            s.scan();
            assertEquals("MCP: (-7, 9)", s.toString(), a.toString());
        }
    }

    @Test
    void medianPointDoesNotHaveToBeAnInputPoint() {
        Point[] in = {new Point(1, 10), new Point(2, 30), new Point(3, 20)};   // MCP = (2, 20)
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(in, a);
            s.scan();
            assertEquals("MCP: (2, 20)", s.toString(), a.toString());
        }
    }

    @Test
    void allAlgorithmsFindTheTrueMedianOnRandomOddSizedInput() {
        for (int n : new int[]{1, 3, 5, 17, 101, 999, 1001}) {
            for (long seed = 0; seed < 3; seed++) {
                Point[] in = randomPoints(n, seed + 31L * n);
                for (Algorithm a : ALL) {
                    PointScanner s = new PointScanner(in, a);
                    s.scan();
                    assertEquals(expectedMcp(in), s.toString(), a + " n=" + n + " seed=" + seed);
                }
            }
        }
    }

    @Test
    void allAlgorithmsAgreeOnEvenSizedInput() {
        for (int n : new int[]{2, 4, 10, 100, 1000}) {
            Point[] in = randomPoints(n, n);
            String first = null;
            for (Algorithm a : ALL) {
                PointScanner s = new PointScanner(in, a);
                s.scan();
                if (first == null) first = s.toString();
                assertEquals(first, s.toString(), a + " n=" + n);
            }
        }
    }

    @Test
    void allAlgorithmsHandleTheMaximumSize() {
        Point[] in = randomPoints(10201, 99);
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(in, a);
            s.scan();
            assertEquals(expectedMcp(in), s.toString(), a.toString());
        }
    }

    // ================================================================ PointScanner: file constructor
    @Test
    void fileConstructorReproducesTheAssignmentExample() throws Exception {
        String name = tempFile(SPEC_FILE);
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(name, a);
            s.scan();
            assertEquals("MCP: (0, 1)", s.toString(), a.toString());
            assertEquals("17", s.stats().trim().split("\\s+")[1], "should have read 17 points");
        }
    }

    @Test
    void fileConstructorAcceptsTabsSpacesAndNewlines() throws Exception {
        // 6 integers split across lines and separators: (1,2) (3,4) (5,6)
        PointScanner s = new PointScanner(tempFile("1\t2   3\n\n4 \t 5\n6\n"), Algorithm.InsertionSort);
        s.scan();
        assertEquals("MCP: (3, 4)", s.toString());
    }

    @Test
    void fileConstructorThrowsFileNotFoundForMissingFile() {
        for (Algorithm a : ALL) {
            assertThrows(FileNotFoundException.class, () -> new PointScanner("no_such_file_hw2.txt", a));
        }
    }

    @Test
    void fileConstructorThrowsInputMismatchForOddNumberOfIntegers() throws Exception {
        String five = tempFile("1 2 3\n4 5\n");
        String one = tempFile("42\n");
        for (Algorithm a : ALL) {
            assertThrows(InputMismatchException.class, () -> new PointScanner(five, a));
            assertThrows(InputMismatchException.class, () -> new PointScanner(one, a));
        }
    }

    @Test
    void fileConstructorRejectsEmptyFile() throws Exception {
        String empty = tempFile("");
        assertThrows(IllegalArgumentException.class, () -> new PointScanner(empty, Algorithm.QuickSort));
    }

    // ================================================================ PointScanner: output
    @Test
    void statsHasAlgorithmSizeAndPositiveTime() {
        Point[] in = randomPoints(1000, 2);
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(in, a);
            s.scan();
            String[] parts = s.stats().trim().split("\\s+");
            assertEquals(3, parts.length, s.stats());
            assertEquals(a.toString(), parts[0]);
            assertEquals("1000", parts[1]);
            assertTrue(Long.parseLong(parts[2]) > 0, "scan time should be positive: " + s.stats());
        }
    }

    @Test
    void statsColumnsLineUp() {
        Point[] in = randomPoints(1000, 3);
        int sizeColumn = -1;
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(in, a);
            s.scan();
            int col = s.stats().indexOf("1000");
            if (sizeColumn < 0) sizeColumn = col;
            assertEquals(sizeColumn, col, "size column should line up: " + s.stats());
        }
    }

    @Test
    void toStringUsesMcpFormat() {
        PointScanner s = new PointScanner(new Point[]{new Point(-1, 2)}, Algorithm.QuickSort);
        s.scan();
        assertEquals("MCP: (-1, 2)", s.toString());
    }

    @Test
    void writeMCPToFileWritesTheToStringLine() throws Exception {
        PointScanner s = new PointScanner(randomPoints(21, 5), Algorithm.MergeSort);
        s.scan();
        s.writeMCPToFile();
        String written = new String(Files.readAllBytes(new File("output.txt").toPath()), "UTF-8").trim();
        assertEquals(s.toString(), written);
    }

    // ================================================================ PointScanner: writeMCPToFile (output file)
    private static java.util.List<String> readOutputLines() throws Exception {
        return Files.readAllLines(new File("output.txt").toPath(), java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    void writeMCPToFileCreatesOutputTxtWithExactlyOneLine() throws Exception {
        new File("output.txt").delete();
        PointScanner s = new PointScanner(randomPoints(21, 5), Algorithm.QuickSort);
        s.scan();
        s.writeMCPToFile();
        assertTrue(new File("output.txt").exists(), "output.txt should be created in the working directory");
        java.util.List<String> lines = readOutputLines();
        assertEquals(1, lines.size(), "file should contain exactly one line: " + lines);
        assertEquals(s.toString(), lines.get(0));
        assertTrue(lines.get(0).matches("MCP: \\(-?\\d+, -?\\d+\\)"), "bad format: " + lines.get(0));
    }

    @Test
    void writeMCPToFileWorksForEveryAlgorithmAndMatchesTheExample() throws Exception {
        String name = tempFile(SPEC_FILE);
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(name, a);
            s.scan();
            s.writeMCPToFile();
            assertEquals("MCP: (0, 1)", readOutputLines().get(0), a.toString());
        }
    }

    @Test
    void writeMCPToFileReplacesOldContentInsteadOfAppending() throws Exception {
        Files.write(new File("output.txt").toPath(), "old line 1\nold line 2\nold line 3\n".getBytes("UTF-8"));
        PointScanner s = new PointScanner(new Point[]{new Point(4, 5)}, Algorithm.MergeSort);
        s.scan();
        s.writeMCPToFile();
        assertEquals(java.util.Arrays.asList("MCP: (4, 5)"), readOutputLines());
    }

    @Test
    void writeMCPToFileCalledTwiceStillHasOneLine() throws Exception {
        PointScanner s = new PointScanner(randomPoints(9, 6), Algorithm.SelectionSort);
        s.scan();
        s.writeMCPToFile();
        s.writeMCPToFile();
        assertEquals(1, readOutputLines().size());
    }

    @Test
    void writeMCPToFileReflectsTheLatestScan() throws Exception {
        PointScanner first = new PointScanner(new Point[]{new Point(1, 1)}, Algorithm.QuickSort);
        first.scan();
        first.writeMCPToFile();
        PointScanner second = new PointScanner(new Point[]{new Point(7, -7)}, Algorithm.QuickSort);
        second.scan();
        second.writeMCPToFile();
        assertEquals("MCP: (7, -7)", readOutputLines().get(0));
    }

    @Test
    void writeMCPToFileFromFileInputScannerWorks() throws Exception {
        PointScanner s = new PointScanner(tempFile("3 4 1 2 5 6\n"), Algorithm.InsertionSort);
        s.scan();
        s.writeMCPToFile();
        assertEquals("MCP: (3, 4)", readOutputLines().get(0));
    }

    // ================================================================ PointScanner: scanTime / repeated scans
    @Test
    void statsTimeIsTheScanTimeField() {
        Point[] in = randomPoints(1000, 12);
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(in, a);
            s.scan();
            assertTrue(s.scanTime > 0, "scanTime should be set by scan(): " + a);
            assertEquals(String.valueOf(s.scanTime), s.stats().trim().split("\\s+")[2], a.toString());
        }
    }

    @Test
    void scanningTwiceGivesTheSameMedian() {
        Point[] in = randomPoints(101, 13);
        for (Algorithm a : ALL) {
            PointScanner s = new PointScanner(in, a);
            s.scan();
            String first = s.toString();
            s.scan();   // the scanner's own array is already sorted now; the answer must not change
            assertEquals(first, s.toString(), a.toString());
        }
    }

    // ================================================================ CompareSorters.generateRandomPoints
    @Test
    void generateRandomPointsGivesTheRequestedCount() {
        assertEquals(1, CompareSorters.generateRandomPoints(1, new Random(1)).length);
        assertEquals(10201, CompareSorters.generateRandomPoints(10201, new Random(1)).length);
    }

    @Test
    void generateRandomPointsRejectsNonPositiveCounts() {
        assertThrows(IllegalArgumentException.class, () -> CompareSorters.generateRandomPoints(0, new Random()));
        assertThrows(IllegalArgumentException.class, () -> CompareSorters.generateRandomPoints(-5, new Random()));
    }

    @Test
    void generateRandomPointsCoversExactlyMinus50To50() {
        Point[] pts = CompareSorters.generateRandomPoints(50000, new Random(7));
        int minX = 999, maxX = -999, minY = 999, maxY = -999;
        for (Point p : pts) {
            assertNotNull(p);
            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY());
        }
        assertEquals(-50, minX);
        assertEquals(50, maxX, "+50 must be possible (range is inclusive)");
        assertEquals(-50, minY);
        assertEquals(50, maxY, "+50 must be possible (range is inclusive)");
    }

    @Test
    void generateRandomPointsIsRepeatableWithTheSameSeed() {
        assertArrayEquals(CompareSorters.generateRandomPoints(100, new Random(42)),
                CompareSorters.generateRandomPoints(100, new Random(42)));
    }

    // ================================================================ CompareSorters.main (error handling)
    @Test
    void mainStopsPolitelyOnAnInvalidMenuKey() throws Exception {
        String out = runMain("9\n");   // must not throw
        assertFalse(out.contains("QuickSort"), "no table expected:\n" + out);
    }

    @Test
    void mainStopsPolitelyOnANonNumberKey() throws Exception {
        assertDoesNotThrow(() -> runMain("abc\n"));
    }

    @Test
    void mainStopsPolitelyWhenTheFileDoesNotExist() throws Exception {
        String out = runMain("2\nno_such_file_hw2.txt\n");   // must not throw
        assertFalse(out.contains("QuickSort"), "no table expected:\n" + out);
    }

    @Test
    void mainStopsPolitelyWhenTheNumberOfPointsIsNotANumber() throws Exception {
        assertDoesNotThrow(() -> runMain("1\nabc\n"));
    }

    @Test
    void mainStopsPolitelyWhenTheFileHasAnOddNumberOfIntegers() throws Exception {
        String odd = tempFile("1 2 3\n");
        String out = runMain("2\n" + odd + "\n");   // must not throw
        assertFalse(out.contains("QuickSort"), "no table expected:\n" + out);
        assertTrue(out.toLowerCase().contains("odd"), "should explain the problem:\n" + out);
    }

    @Test
    void oddNumberOfIntegersMessageNamesTheProblem() throws Exception {
        String odd = tempFile("1 2 3\n");
        InputMismatchException e = assertThrows(InputMismatchException.class,
                () -> new PointScanner(odd, Algorithm.SelectionSort));
        assertNotNull(e.getMessage(), "exception should carry a message");
    }
}
