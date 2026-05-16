 package DataAcessLayerDAL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/arabic?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root"; // Update if your MySQL user is different
    private static final String PASSWORD = ""; // Update if your MySQL password is set
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Private constructor for singleton
    private DBConnection() {
        try {
            // Load MySQL JDBC driver
            Class.forName(DRIVER);
            // Establish connection
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connection established: arbic_db");
            // Initialize database and tables
            initializeTables();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("Failed to load JDBC driver", e);
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    // Get singleton instance
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // Get connection
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("Connection closed, reconnecting...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Reconnected to database: arbic_db");
                initializeTables();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking/reconnecting database: " + e.getMessage());
            throw new RuntimeException("Failed to reconnect to database", e);
        }
        return connection;
    }

    // Initialize database and tables
    private void initializeTables() {
        try (Statement stmt = connection.createStatement()) {
            // Create database if not exists
            String createDatabase = "CREATE DATABASE IF NOT EXISTS arbic_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.executeUpdate(createDatabase);
            System.out.println("✅ Database created/verified: arbic_db");

            // Use the database
            stmt.executeUpdate("USE arbic_db");

            // Create authors table
            String createAuthorsTable = """
                CREATE TABLE IF NOT EXISTS authors (
                    author_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL UNIQUE,
                    biography TEXT,
                    INDEX idx_name (name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.executeUpdate(createAuthorsTable);
            System.out.println("✅ Authors table created/verified");

            // Create books table
            String createBooksTable = """
                CREATE TABLE IF NOT EXISTS books (
                    book_id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    author_id INT NOT NULL,
                    era VARCHAR(255),
                    FOREIGN KEY (author_id) REFERENCES authors(author_id) ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_title (title)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.executeUpdate(createBooksTable);
            System.out.println("✅ Books table created/verified");

            // Create chapters table
            String createChaptersTable = """
                CREATE TABLE IF NOT EXISTS chapters (
                    chapter_id INT AUTO_INCREMENT PRIMARY KEY,
                    book_id INT NOT NULL,
                    chapter_name VARCHAR(255) NOT NULL,
                    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_chapter_name (chapter_name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.executeUpdate(createChaptersTable);
            System.out.println("✅ Chapters table created/verified");

            // Create sentences table
            String createSentencesTable = """
                CREATE TABLE IF NOT EXISTS sentences (
                    sentence_id INT AUTO_INCREMENT PRIMARY KEY,
                    chapter_id INT NOT NULL,
                    sentence_number INT NOT NULL,
                    text TEXT NOT NULL,
                    text_diacritized TEXT,
                    translation TEXT,
                    notes TEXT,
                    FOREIGN KEY (chapter_id) REFERENCES chapters(chapter_id) ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_chapter_id (chapter_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.executeUpdate(createSentencesTable);
            System.out.println("✅ Sentences table created/verified");

            // Create tokens table
            String createTokensTable = """
                CREATE TABLE IF NOT EXISTS tokens (
                    token_id INT AUTO_INCREMENT PRIMARY KEY,
                    sentence_id INT NOT NULL,
                    token_text VARCHAR(255) NOT NULL,
                    lemma VARCHAR(255),
                    position INT NOT NULL,
                    FOREIGN KEY (sentence_id) REFERENCES sentences(sentence_id) ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_sentence_id (sentence_id),
                    INDEX idx_token_text (token_text)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.executeUpdate(createTokensTable);
            System.out.println("✅ Tokens table created/verified");
            
            try {
                String addLemmaColumn = "ALTER TABLE tokens ADD COLUMN lemma VARCHAR(255)";
                stmt.executeUpdate(addLemmaColumn);
                System.out.println("✅ Lemma column added to tokens table");
            } catch (SQLException lemmaEx) {
                if (lemmaEx.getMessage() != null && lemmaEx.getMessage().contains("Duplicate column name")) {
                    System.out.println("ℹ Lemma column already exists on tokens table");
                } else {
                    throw lemmaEx;
                }
            }

            // Create token_segmentation table
            String createTokenSegmentationTable = """
                CREATE TABLE IF NOT EXISTS token_segmentation (
                    segmentation_id INT AUTO_INCREMENT PRIMARY KEY,
                    token_id INT NOT NULL UNIQUE,
                    prefix VARCHAR(50),
                    stem VARCHAR(255) NOT NULL,
                    suffix VARCHAR(50),
                    FOREIGN KEY (token_id) REFERENCES tokens(token_id) ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_token_id (token_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.executeUpdate(createTokenSegmentationTable);
            System.out.println("✅ Token segmentation table created/verified");

            // Create roots table
            String createRootsTable = """
                CREATE TABLE IF NOT EXISTS roots (
                    root_id INT AUTO_INCREMENT PRIMARY KEY,
                    token_id INT NOT NULL,
                    root_text VARCHAR(255) NOT NULL,
                    pattern VARCHAR(100),
                    confidence_score DECIMAL(5,2) DEFAULT 0.00,
                    FOREIGN KEY (token_id) REFERENCES tokens(token_id) ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_token_id (token_id),
                    INDEX idx_root_text (root_text)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.executeUpdate(createRootsTable);
            System.out.println("✅ Roots table created/verified");

        } catch (SQLException e) {
            System.err.println("❌ Error initializing tables: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }

    // Close connection
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Database connection closed");
            } catch (SQLException e) {
                System.err.println("❌ Error closing database connection: " + e.getMessage());
            }
        }
        instance = null;
    }
}