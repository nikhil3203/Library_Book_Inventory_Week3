package library;

import library.model.Book;
import library.persistence.JsonDataStore;
import library.service.LibraryService;
import library.ui.ConsoleInputHelper;
import library.ui.LibraryConsoleApp;

import java.util.Scanner;

/**
 * Application entry point — wires up dependencies and launches the console UI.
 *
 * <p><b>Week 4 Refactoring (Issue #6 — SRP):</b> In Week 3, this class was
 * 153 lines long and contained menu display, input parsing, business-flow
 * orchestration, sample-data seeding, <em>and</em> the {@code main()} method —
 * all in static methods. It now has a single responsibility: <b>bootstrapping</b>.
 * The menu logic lives in {@link LibraryConsoleApp}, and input utilities live
 * in {@link ConsoleInputHelper}.</p>
 *
 * <p><b>Week 5 Integration:</b> The service is now backed by a
 * {@link JsonDataStore} so the inventory persists across restarts.
 * Sample data is seeded only on the very first launch (when no persisted
 * data exists).</p>
 *
 * <p><b>Dependency Injection:</b> {@code main()} constructs concrete
 * dependencies and passes them into {@link LibraryConsoleApp}'s constructor,
 * making the UI controller easily testable with mocks.</p>
 */
public class Main {

    public static void main(String[] args) {
        // Week 5: assemble dependencies with file-based persistence
        JsonDataStore dataStore = new JsonDataStore();
        LibraryService library = new LibraryService(dataStore);
        ConsoleInputHelper input = new ConsoleInputHelper(new Scanner(System.in));

        // Seed sample data only on first launch (no persisted data yet)
        if (!library.hasBooks()) {
            seedSampleData(library);
        }

        // Launch the interactive console
        LibraryConsoleApp app = new LibraryConsoleApp(library, input);
        app.run();
    }

    /**
     * Adds sample records so the application has data on first launch.
     *
     * @param library the library service to seed
     */
    private static void seedSampleData(LibraryService library) {
        library.addBook(new Book(101, "Effective Java", "Joshua Bloch", "9780134685991", 2018));
        library.addBook(new Book(102, "Clean Code", "Robert C. Martin", "9780132350884", 2008));
    }
}