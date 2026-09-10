package library.ui;

import java.time.Year;
import java.util.Scanner;

/**
 * Utility class encapsulating all console input-reading operations.
 *
 * <p><b>Week 4 Refactoring (Issue #6 — SRP):</b> These methods were previously
 * {@code private static} helpers buried inside {@code Main.java}. Extracting
 * them into a dedicated class:</p>
 * <ul>
 *   <li>Makes them independently testable.</li>
 *   <li>Allows reuse if additional CLI tools are built (e.g., a patron
 *       management console).</li>
 *   <li>Keeps the UI controller ({@link LibraryConsoleApp}) focused on
 *       orchestration rather than low-level I/O parsing.</li>
 * </ul>
 *
 * <p>The class is <b>not static</b> — it holds a {@link Scanner} reference so
 * it can be swapped with a mock/test scanner via constructor injection.</p>
 */
public class ConsoleInputHelper {

    private final Scanner scanner;

    /**
     * Creates a helper that reads from the given scanner.
     *
     * <p><b>Dependency Injection:</b> Accepting a {@code Scanner} via the
     * constructor (rather than hard-coding {@code System.in}) makes this class
     * testable with a {@code Scanner(String)} in unit tests.</p>
     *
     * @param scanner the input source
     */
    public ConsoleInputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads an integer from the console, re-prompting on invalid input.
     *
     * @param prompt the prompt to display
     * @return a valid integer
     */
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a whole number.");
            }
        }
    }

    /**
     * Reads a positive integer (greater than 0) from the console.
     *
     * @param prompt the prompt to display
     * @return a positive integer
     */
    public int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) return value;
            System.out.println("Value must be greater than 0.");
        }
    }

    /**
     * Reads a valid publication year (between 1000 and the current year).
     *
     * @param prompt the prompt to display
     * @return a valid year
     */
    public int readYear(String prompt) {
        int currentYear = Year.now().getValue();
        while (true) {
            int year = readInt(prompt);
            if (year >= 1000 && year <= currentYear) return year;
            System.out.println("Enter a valid year between 1000 and " + currentYear + ".");
        }
    }

    /**
     * Reads a non-empty, trimmed string from the console.
     *
     * @param prompt the prompt to display
     * @return a non-blank string
     */
    public String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }

    /**
     * Closes the underlying scanner.
     */
    public void close() {
        scanner.close();
    }
}
