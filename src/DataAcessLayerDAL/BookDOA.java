package DataAcessLayerDAL;

import java.sql.*;
import java.util.ArrayList;
import ModelDTO.Book;

public class BookDOA implements IBookDOA {
    public BookDOA() {
        // Acquire connection per-method via DBConnection.getInstance().getConnection()
    	
    }

    // ✅ CREATE (Insert new Book)
    @Override
    public boolean addBook(String title, int authorId, String era, String authorname) {
        String sql = "INSERT INTO books (title, author_id, era) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, authorId);
            ps.setString(3, era);

            int rows = ps.executeUpdate();
            return rows == 1;

        } catch (SQLException e) {
            System.err.println("Error adding book:");
            e.printStackTrace();
            return false;
        }
    }

    // ✅ UPDATE (Change existing Book details)
    @Override
    public boolean updateBook(int bookId, int authorId, String newBookName, String era) {
        String sql = "UPDATE books SET title = ?, era = ?, author_id = ? WHERE book_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newBookName);
            ps.setString(2, era);
            ps.setInt(3, authorId);
            ps.setInt(4, bookId);

            int rows = ps.executeUpdate();
            return rows > 0; // ✅ true if at least one record updated
        } catch (SQLException e) {
            System.err.println("❌ Error updating book:");
            e.printStackTrace();
            return false;
        }
    }


    // ✅ DELETE (Remove Book by ID)
    @Override
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting book:");
            e.printStackTrace();
            return false;
        }
    }

    // ✅ READ (Retrieve a Book by keyword)
    @Override
    public Book retrieveBook(int bookID) {
        String sql = "SELECT book_id, title, author_id, era FROM books WHERE book_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int bookId = rs.getInt("book_id");
                    String title = rs.getString("title");
                    int authorId = rs.getInt("author_id");
                    String era = rs.getString("era");

                    return new Book(bookId, title, authorId, era);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving book:");
            e.printStackTrace();
        }
        return null; // no book found
    }


    // ✅ SEARCH (Return book_id for a given title)
    @Override
    public int searchBook(String title) {
        String sql = "SELECT book_id FROM books WHERE title = ? LIMIT 1";
        Connection conn = DBConnection.getInstance().getConnection();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("book_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching book:");
            e.printStackTrace();
        }
        return -1; // not found
    }

    // ✅ RETRIEVE ALL
    @Override
    public ArrayList<Book> retrieveAllBooks() {
        ArrayList<Book> bookList = new ArrayList<>();
        String sql = "SELECT book_id, title, author_id, era FROM books";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                int authorId = rs.getInt("author_id");
                String era = rs.getString("era");

                Book book = new Book(bookId, title, authorId, era);
                bookList.add(book);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving books:");
            e.printStackTrace();
        }

        return bookList;
    }
}