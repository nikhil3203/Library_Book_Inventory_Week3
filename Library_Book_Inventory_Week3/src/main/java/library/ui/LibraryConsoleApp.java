package library.ui;

import library.exception.BookNotFoundException;
import library.exception.DuplicateBookException;
import library.model.Book;
import library.service.LibraryService;

import java.util.List;

/**
 * Menu-driven console application for the Library Book Inventory.
 *
 * <p><b>Week 4 Refactoring (Issue #6 — SRP + Dependency Injection):</b></p>
 * <ul>
 *   <li>This class was extracted from the monolithic {@code Main.java}, which
 *       previously mixed menu display, input parsing, business-flow orchestration,
 *       and application bootstrapping in a single class with only static methods.</li>
 *   <li>Now receives its dependencies ({@link LibraryService},
 *       {@link ConsoleInputHelper}) via the constructor — <b>Dependency Injection</b>.
 *       This makes the class testable and decoupled from concrete implementations.</li>
 *   <li>Input-reading utilities are delegated to {@link ConsoleInputHelper},
 *       keeping this class focused on menu orchestration.</li>
 *   <li>Error handling now catches the custom {@link BookNotFoundException} and
 *       {@link DuplicateBookException} types for clearer, more targeted error
 *       messages.</li>
 * </ul>
 */
public class LibraryConsoleApp {

    private final LibraryService library;
    private final ConsoleInputHelper input;

    /**
     * Constructs the console application with injected dependencies.
     *
     * <p><b>Dependency Injection:</b> The caller decides which
     * {@code LibraryService} and {@code ConsoleInputHelper} to use, making
     * this class easy to unit-test with mocks or stubs.</p>
     *
     * @param library the library service providing CRUD operations
     * @param input   the console input helper for user prompts
     */
    public LibraryConsoleApp(LibraryService library, ConsoleInputHelper input) {
        this.library = library;
        this.input = input;
    }

    /**
     * Starts the interactive menu loop.
     */
    public void run() {
        System.out.println("==========================================");
        System.out.println("     LIBRARY BOOK INVENTORY SYSTEM");
        System.out.println("==========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = input.readInt("Enter your choice: ");

            try {
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> listBooks();
                    case 3 -> updateBook();
                    case 4 -> deleteBook();
                    case 5 -> searchBooks();
                    case 6 -> {
                        running = false;
                        System.out.println("Thank you. Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Select 1-6.");
                }
            } catch (BookNotFoundException ex) {
                // Week 4: catch specific exception type instead of generic IAE
                System.out.println("Error: " + ex.getMessage());
            } catch (DuplicateBookException ex) {
                // Week 4: catch specific exception type instead of generic IAE
                System.out.println("Error: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                // Fallback for validation errors from the model layer
                System.out.println("Error: " + ex.getMessage());
            }
        }
        input.close();
    }

    private void printMenu() {
        System.out.println("\n1. Add Book");
        System.out.println("2. View All Books");
        System.out.println("3. Update Book");
        System.out.println("4. Delete Book");
        System.out.println("5. Search Book");
        System.out.println("6. Exit");
    }

    private void addBook() {
        int id = input.readPositiveInt("Book ID: ");
        String title = input.readNonEmpty("Title: ");
        String author = input.readNonEmpty("Author: ");
        String isbn = input.readNonEmpty("ISBN: ");
        int year = input.readYear("Publication Year: ");

        library.addBook(new Book(id, title, author, isbn, year));
        System.out.println("Book added successfully.");
    }

    private void listBooks() {
        List<Book> books = library.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }
        System.out.println("\n--- Book Inventory ---");
        books.forEach(System.out::println);
        System.out.println("Total books: " + books.size());
    }

    private void updateBook() {
        int id = input.readPositiveInt("Enter Book ID to update: ");

        // Week 4: use Optional pattern — cleaner than null-check
        if (library.findById(id).isEmpty()) {
            System.out.println("Book not found.");
            return;
        }

        String title = input.readNonEmpty("New Title: ");
        String author = input.readNonEmpty("New Author: ");
        String isbn = input.readNonEmpty("New ISBN: ");
        int year = input.readYear("New Publication Year: ");

        library.updateBook(id, title, author, isbn, year);
        System.out.println("Book updated successfully.");
    }

    private void deleteBook() {
        int id = input.readPositiveInt("Enter Book ID to delete: ");
        library.deleteBook(id);
        System.out.println("Book deleted successfully.");
    }

    private void searchBooks() {
        String keyword = input.readNonEmpty("Enter title or author keyword: ");
        List<Book> result = library.search(keyword);

        if (result.isEmpty()) {
            System.out.println("No matching books found.");
        } else {
            result.forEach(System.out::println);
            System.out.println("Matches: " + result.size());
        }
    }
}
