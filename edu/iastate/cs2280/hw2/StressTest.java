package edu.iastate.cs2280.hw2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Stress tests. Builds 16 awkward input files itself (no data files needed) and checks:
 *   - the median point from the file constructor, for all four algorithms
 *   - the median point from the array constructor
 *   - every sorter's full sorted output against Arrays.sort
 *   - what CompareSorters.main prints for each file
 *   - files that are not allowed (odd number of integers, no points at all)
 *
 * Expected answers are worked out here from the raw numbers (not from your code), using the
 * starter getMedian() convention: index n/2 of the sorted coordinates.
 *
 * Takes a few seconds: it sorts the 10,201-point files with selection and insertion sort many times.
 */
class StressTest {

    private static final Algorithm[] ALL = Algorithm.values();

    /** One input file: its name, its exact text, and the numbers in it (parsed independently). */
    private static final class Case {
        final String name;
        final String text;
        final int[] nums;      // x0 y0 x1 y1 ...
        Case(String name, String text) {
            this.name = name;
            this.text = text;
            String t = text.trim();
            if (t.isEmpty()) { nums = new int[0]; return; }
            String[] parts = t.split("\\s+");
            nums = new int[parts.length];
            for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);
        }
        int count() { return nums.length / 2; }
        Point[] points() {
            Point[] p = new Point[count()];
            for (int i = 0; i < p.length; i++) p[i] = new Point(nums[2 * i], nums[2 * i + 1]);
            return p;
        }
        String expectedMcp() {
            int n = count();
            int[] xs = new int[n], ys = new int[n];
            for (int i = 0; i < n; i++) { xs[i] = nums[2 * i]; ys[i] = nums[2 * i + 1]; }
            Arrays.sort(xs);
            Arrays.sort(ys);
            return "MCP: (" + xs[n / 2] + ", " + ys[n / 2] + ")";
        }
        @Override public String toString() { return name; }
    }

    private final List<Path> tempFiles = new ArrayList<>();

    @AfterEach
    void cleanUp() throws Exception {
        for (Path p : tempFiles) Files.deleteIfExists(p);
    }

    // ---------------------------------------------------------------- building the cases
    private static String lines(List<int[]> pts, int perLine) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pts.size(); i++) {
            sb.append(pts.get(i)[0]).append(' ').append(pts.get(i)[1]);
            sb.append((i + 1) % perLine == 0 || i == pts.size() - 1 ? "\n" : " ");
        }
        return sb.toString();
    }

    private static List<int[]> grid() {
        List<int[]> g = new ArrayList<>();
        for (int x = -50; x <= 50; x++) for (int y = -50; y <= 50; y++) g.add(new int[]{x, y});
        return g;
    }

    private static List<Case> validCases() {
        Random r = new Random(2280);
        List<Case> c = new ArrayList<>();

        List<int[]> randomMax = new ArrayList<>();
        for (int i = 0; i < 10201; i++) randomMax.add(new int[]{r.nextInt(101) - 50, r.nextInt(101) - 50});
        c.add(new Case("01 max random 10201", lines(randomMax, 1)));

        List<int[]> shuffled = grid();
        Collections.shuffle(shuffled, new Random(1));
        c.add(new Case("02 full grid shuffled", lines(shuffled, 1)));
        c.add(new Case("03 full grid sorted (quicksort worst case)", lines(grid(), 1)));
        List<int[]> reversed = grid();
        Collections.reverse(reversed);
        c.add(new Case("04 full grid reverse (insertion worst case)", lines(reversed, 1)));

        List<int[]> same = new ArrayList<>();
        for (int i = 0; i < 10201; i++) same.add(new int[]{7, -7});
        c.add(new Case("05 all identical 10201 (quicksort recursion depth)", lines(same, 1)));

        List<int[]> dups = new ArrayList<>();
        for (int i = 0; i < 10201; i++) dups.add(new int[]{r.nextInt(3) - 1, r.nextInt(3) - 1});
        c.add(new Case("06 heavy duplicates (9 distinct points)", lines(dups, 1)));

        c.add(new Case("07 one point", "-3 4\n"));
        c.add(new Case("08 two points", "10 -10\n-10 10\n"));
        c.add(new Case("09 three points", "5 5\n-5 -5\n0 0\n"));
        c.add(new Case("10 corners and extremes", "-50 -50\n50 50\n-50 50\n50 -50\n0 0\n-50 -50\n50 50\n"));

        List<int[]> vertical = new ArrayList<>();
        for (int y = -50; y <= 50; y++) vertical.add(new int[]{0, y});
        c.add(new Case("11 all x equal", lines(vertical, 1)));
        List<int[]> horizontal = new ArrayList<>();
        for (int x = 50; x >= -50; x--) horizontal.add(new int[]{x, 0});
        c.add(new Case("12 all y equal", lines(horizontal, 1)));

        List<int[]> negative = new ArrayList<>();
        for (int i = 0; i < 501; i++) negative.add(new int[]{-1 - r.nextInt(50), -1 - r.nextInt(50)});
        c.add(new Case("13 all negative", lines(negative, 1)));

        c.add(new Case("14 assignment example",
                "0 0 -3 -9 0 -10\n8 4 3 3 -6\n3 -2 1\n10 5 -7 -10\n5 -2\n7 3 10 5\n-7 -10 0 8\n-1 -6\n-10 0\n5 5\n"));
        c.add(new Case("15 messy whitespace", "  1\t2     3\n\n\n 4\t\t5 \n6   7\t8\n\n  9 10  \n"));

        List<int[]> oneLine = new ArrayList<>();
        for (int i = 0; i < 2000; i++) oneLine.add(new int[]{r.nextInt(101) - 50, r.nextInt(101) - 50});
        c.add(new Case("16 2000 points on one line", lines(oneLine, 2000)));
        return c;
    }

    private String write(Case c) throws Exception {
        Path p = Files.createTempFile("hw2stress", ".txt");
        tempFiles.add(p);
        Files.write(p, c.text.getBytes("UTF-8"));
        return p.toString();
    }

    private String writeText(String text) throws Exception {
        Path p = Files.createTempFile("hw2stress", ".txt");
        tempFiles.add(p);
        Files.write(p, text.getBytes("UTF-8"));
        return p.toString();
    }

    private static AbstractSorter newSorter(Algorithm a, Point[] pts) {
        switch (a) {
            case SelectionSort: return new SelectionSorter(pts);
            case InsertionSort: return new InsertionSorter(pts);
            case MergeSort:     return new MergeSorter(pts);
            default:            return new QuickSorter(pts);
        }
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

    /** Number of table rows ("<AlgorithmName> <size> <time>") for the given size. */
    private static int rowsWithSize(String out, int size) {
        int rows = 0;
        for (String line : out.split("\\R")) {
            String[] p = line.trim().split("\\s+");
            if (p.length == 3 && p[1].equals(String.valueOf(size)) && p[2].matches("\\d+")) {
                for (Algorithm a : ALL) if (p[0].equals(a.toString())) rows++;
            }
        }
        return rows;
    }

    // ---------------------------------------------------------------- tests on valid files
    @Test
    void fileInputGivesTheRightMedianForEveryCaseAndAlgorithm() throws Exception {
        for (Case c : validCases()) {
            String file = write(c);
            for (Algorithm a : ALL) {
                PointScanner s = new PointScanner(file, a);
                s.scan();
                assertEquals(c.expectedMcp(), s.toString(), c + " / " + a);
                assertEquals(String.valueOf(c.count()), s.stats().trim().split("\\s+")[1], c + " / " + a + " size");
            }
        }
    }

    @Test
    void arrayInputGivesTheRightMedianForEveryCaseAndAlgorithm() throws Exception {
        for (Case c : validCases()) {
            for (Algorithm a : ALL) {
                PointScanner s = new PointScanner(c.points(), a);
                s.scan();
                assertEquals(c.expectedMcp(), s.toString(), c + " / " + a);
            }
        }
    }

    @Test
    void everySorterFullySortsEveryCaseByXAndByY() throws Exception {
        Comparator<Point> byX = Comparator.comparingInt(Point::getX).thenComparingInt(Point::getY);
        Comparator<Point> byY = Comparator.comparingInt(Point::getY).thenComparingInt(Point::getX);
        for (Case c : validCases()) {
            for (Algorithm a : ALL) {
                for (int order = 0; order <= 1; order++) {
                    Point[] in = c.points();
                    AbstractSorter sorter = newSorter(a, in);
                    sorter.setComparator(order);
                    sorter.sort();
                    Point[] actual = new Point[in.length];
                    sorter.getPoints(actual);
                    Point[] expected = in.clone();
                    Arrays.sort(expected, order == 0 ? byX : byY);
                    assertArrayEquals(expected, actual, c + " / " + a + " / " + (order == 0 ? "by x" : "by y"));
                }
            }
        }
    }

    @Test
    void mainPrintsAFullTableForEveryCase() throws Exception {
        for (Case c : validCases()) {
            String out = runMain("2\n" + write(c) + "\n3\n");
            assertEquals(4, rowsWithSize(out, c.count()), c + " should give 4 table rows of size " + c.count() + ":\n" + out);
            assertFalse(out.contains("Exception"), c + ":\n" + out);
        }
    }

    @Test
    void mainRunsManyTrialsInOneSession() throws Exception {
        String three = write(new Case("three", "1 2 3 4 5 6\n"));
        String one = write(new Case("one", "9 9\n"));
        // random(40), file(3 points), file(1 point), random(41), file(3 points), then exit
        String out = runMain("1\n40\n2\n" + three + "\n2\n" + one + "\n1\n41\n2\n" + three + "\n3\n");
        assertEquals(4, rowsWithSize(out, 40), out);
        assertEquals(4, rowsWithSize(out, 41), out);
        assertEquals(8, rowsWithSize(out, 3), out);
        assertEquals(4, rowsWithSize(out, 1), out);
        assertTrue(out.contains("Trial 6: "), "should have reached trial 6:\n" + out);
    }

    // ---------------------------------------------------------------- tests on bad files
    @Test
    void oddNumberOfIntegersIsRejectedWithInputMismatch() throws Exception {
        String five = writeText("1 2 3\n4 5\n");
        String one = writeText("42\n");
        String three = writeText("1 2\n3\n");
        for (Algorithm a : ALL) {
            assertThrows(InputMismatchException.class, () -> new PointScanner(five, a));
            assertThrows(InputMismatchException.class, () -> new PointScanner(one, a));
            assertThrows(InputMismatchException.class, () -> new PointScanner(three, a));
        }
    }

    @Test
    void aFileWithNoPointsCanNeverBeScanned() throws Exception {
        // The file constructor is not required to throw here. Either it or scan() must throw
        // IllegalArgumentException (the AbstractSorter constructor rejects an empty array).
        String empty = writeText("");
        String blank = writeText("   \n\t\n  \n");
        for (Algorithm a : ALL) {
            assertThrows(IllegalArgumentException.class, () -> new PointScanner(empty, a).scan());
            assertThrows(IllegalArgumentException.class, () -> new PointScanner(blank, a).scan());
        }
    }

    @Test
    void mainSurvivesEveryBadFileWithoutATable() throws Exception {
        String[] bad = {"1 2 3\n4 5\n", "42\n", "", "   \n\t\n"};
        for (String text : bad) {
            String out = runMain("2\n" + writeText(text) + "\n");   // must not throw
            for (Algorithm a : ALL) {
                assertFalse(out.contains(a.toString()), "no table expected for file [" + text.trim() + "]:\n" + out);
            }
            assertFalse(out.contains("Exception"), "no stack trace expected:\n" + out);
        }
    }

    @Test
    void mainSurvivesAMissingFile() throws Exception {
        String out = runMain("2\nthis_file_does_not_exist_hw2.txt\n");
        assertFalse(out.contains("QuickSort"), out);
        assertFalse(out.contains("Exception"), "no stack trace expected:\n" + out);
    }
}
