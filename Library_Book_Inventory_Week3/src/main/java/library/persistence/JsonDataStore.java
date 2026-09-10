package library.persistence;

import library.model.Book;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persists the book inventory to a JSON file on the local file system.
 *
 * <p><b>Week 5 Integration:</b> This class uses <em>only</em> standard
 * Java I/O and string processing — no external JSON library (Gson,
 * Jackson, etc.) is required. The trade-off is a simpler but less
 * flexible serializer; for a production system, a mature library
 * would be preferred.</p>
 *
 * <p>The default data file is {@code library_data.json} in the current
 * working directory. A custom path can be supplied via the constructor.</p>
 *
 * @see DataStore
 */
public class JsonDataStore implements DataStore {

    private final Path filePath;

    /**
     * Creates a data store that reads/writes to {@code library_data.json}
     * in the current working directory.
     */
    public JsonDataStore() {
        this(Path.of("library_data.json"));
    }

    /**
     * Creates a data store that reads/writes to the given file path.
     *
     * @param filePath the path to the JSON data file
     */
    public JsonDataStore(Path filePath) {
        this.filePath = filePath;
    }

    // ---------- DataStore contract ----------

    /**
     * Loads books from the JSON file. Returns an empty list if the file
     * does not exist or is empty.
     */
    @Override
    public List<Book> load() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try {
            String content = Files.readString(filePath).trim();
            if (content.isEmpty() || content.equals("[]")) {
                return new ArrayList<>();
            }
            return parseBooks(content);
        } catch (IOException e) {
            System.err.println("Warning: Could not read data file — starting with empty inventory.");
            System.err.println("  Cause: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Saves the complete book list to the JSON file, creating parent
     * directories if necessary.
     */
    @Override
    public void save(List<Book> books) {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.writeString(filePath, toJson(books));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save data file: " + filePath, e);
        }
    }

    // ---------- JSON serialization (no external library) ----------

    /**
     * Converts a list of books to a formatted JSON array string.
     */
    private String toJson(List<Book> books) {
        if (books.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": ").append(b.getId()).append(",\n");
            sb.append("    \"title\": \"").append(escapeJson(b.getTitle())).append("\",\n");
            sb.append("    \"author\": \"").append(escapeJson(b.getAuthor())).append("\",\n");
            sb.append("    \"isbn\": \"").append(escapeJson(b.getIsbn())).append("\",\n");
            sb.append("    \"publicationYear\": ").append(b.getPublicationYear()).append("\n");
            sb.append("  }");
            if (i < books.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Escapes special characters for JSON string values.
     */
    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
    }

    // ---------- JSON deserialization (simple regex-based parser) ----------

    /** Pattern to match a single book JSON object. */
    private static final Pattern BOOK_PATTERN = Pattern.compile(
            "\\{[^}]*\"id\"\\s*:\\s*(\\d+)[^}]*\"title\"\\s*:\\s*\"([^\"]*?)\"" +
            "[^}]*\"author\"\\s*:\\s*\"([^\"]*?)\"[^}]*\"isbn\"\\s*:\\s*\"([^\"]*?)\"" +
            "[^}]*\"publicationYear\"\\s*:\\s*(\\d+)[^}]*}",
            Pattern.DOTALL
    );

    /**
     * Parses a JSON array string into a list of Book objects.
     */
    private List<Book> parseBooks(String json) {
        List<Book> books = new ArrayList<>();
        Matcher matcher = BOOK_PATTERN.matcher(json);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            String title = unescapeJson(matcher.group(2));
            String author = unescapeJson(matcher.group(3));
            String isbn = unescapeJson(matcher.group(4));
            int year = Integer.parseInt(matcher.group(5));
            books.add(new Book(id, title, author, isbn, year));
        }
        return books;
    }

    /**
     * Reverses JSON escape sequences.
     */
    private String unescapeJson(String value) {
        return value.replace("\\\"", "\"")
                     .replace("\\\\", "\\")
                     .replace("\\n", "\n")
                     .replace("\\r", "\r")
                     .replace("\\t", "\t");
    }
}
