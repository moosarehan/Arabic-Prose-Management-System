 
 package DataAcessLayerDAL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ModelDTO.Author;

public class AuthorDOA implements IAuthorDOA {
    public AuthorDOA() {
        // Acquire connection per-method via DBConnection
    	Connection conn = DBConnection.getInstance().getConnection();
    }

    // ✅ CREATE (Insert new Author)
    @Override
    public boolean addAuthor(String name, String biography) {
        String sql = "INSERT INTO authors (name, biography) VALUES (?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, biography);

            int rows = ps.executeUpdate();
            return rows == 1;

        } catch (SQLException e) {
            System.err.println("Error adding author:");
            e.printStackTrace();
            return false;
        }
    }

    // ✅ UPDATE (Change existing Author)
    @Override
    public boolean updateAuthor(String oldname, String newname, String biography) {
        String sql = "UPDATE authors SET name = ?, biography = ? WHERE name = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newname);
            ps.setString(2, biography);
            ps.setString(3, oldname);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating author:");
            e.printStackTrace();
            return false;
        }
    }

    // ✅ DELETE (Remove Author)
    @Override
    public boolean deleteAuthor(String authorname) {
        String sql = "DELETE FROM authors WHERE name = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, authorname);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting author:");
            e.printStackTrace();
            return false;
        }
    }

    // ✅ READ (Retrieve author by keyword)
    @Override
    public Author retrieveAuthor(String keyword) {
        String sql = "SELECT author_id, name, biography FROM authors WHERE name LIKE ? LIMIT 1";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Author a = new Author();
                    a.setAuthorId(rs.getInt("author_id"));
                    a.setName(rs.getString("name"));
                    a.setBiography(rs.getString("biography"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving author:");
            e.printStackTrace();
        }
        return null; // not found
    }

    // ✅ SEARCH (Return author_id for given name)
    @Override
    public int searchAuthor(String authorname) {
        String sql = "SELECT author_id FROM authors WHERE name = ? LIMIT 1";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authorname);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("author_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching author:");
            e.printStackTrace();
        }
        return -1; // not found
    }

    @Override
    public ArrayList<Author> retrieveAllAuthors() {
        ArrayList<Author> authorList = new ArrayList<>();

        String sql = "SELECT author_id, name, biography FROM authors"; // table name = author

           Connection conn = DBConnection.getInstance().getConnection();
           try (PreparedStatement ps = conn.prepareStatement(sql);
               ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int authorId = rs.getInt("author_id");
                String name = rs.getString("name");
                String biography = rs.getString("biography");

                // Fill DTO
                Author author = new Author(authorId, name, biography);
                authorList.add(author);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving authors:");
            e.printStackTrace();
        }

        return authorList;
    }

    @Override
    public String getAuthorByID(int id) {
        String authorName = null;
        String sql = "SELECT name FROM authors WHERE author_id = ?";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    authorName = rs.getString("name");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving author by ID:");
            e.printStackTrace();
        }

        return authorName;
    }


}