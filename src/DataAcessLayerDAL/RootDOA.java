package DataAcessLayerDAL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ModelDTO.Root;

public class RootDOA implements IRootDOA {
    public RootDOA() {
        // No cached connection - acquire per-method to allow DBConnection to reconnect
    }
    
    @Override
    public boolean addRoot(Root root) {
        String sql = "INSERT INTO roots (token_id, root_text, pattern, confidence_score) VALUES (?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, root.getTokenId());
            ps.setString(2, root.getRootText());
            ps.setString(3, root.getPattern());
            ps.setDouble(4, root.getConfidenceScore());
            
            int result = ps.executeUpdate();
            boolean success = result > 0;
            System.out.println("Added root for token_id: " + root.getTokenId() + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding root for token_id: " + root.getTokenId() + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean addRootsForToken(int tokenId, List<Root> roots) {
        // First, delete existing roots for this token
        deleteRootsByTokenId(tokenId);
        
        if (roots == null || roots.isEmpty()) {
            return true;
        }
        
        String sql = "INSERT INTO roots (token_id, root_text, pattern, confidence_score) VALUES (?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Root root : roots) {
                ps.setInt(1, tokenId);
                ps.setString(2, root.getRootText());
                ps.setString(3, root.getPattern());
                ps.setDouble(4, root.getConfidenceScore());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            boolean success = results.length == roots.size();
            System.out.println("Added " + roots.size() + " roots for token_id: " + tokenId + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding roots for token_id: " + tokenId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateRoot(Root root) {
        String sql = "UPDATE roots SET root_text = ?, pattern = ?, confidence_score = ? WHERE root_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, root.getRootText());
            ps.setString(2, root.getPattern());
            ps.setDouble(3, root.getConfidenceScore());
            ps.setInt(4, root.getRootId());
            
            int result = ps.executeUpdate();
            boolean success = result > 0;
            System.out.println("Updated root_id: " + root.getRootId() + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating root_id: " + root.getRootId() + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<Root> retrieveRootsByTokenId(int tokenId) {
        List<Root> roots = new ArrayList<>();
        String sql = "SELECT root_id, token_id, root_text, pattern, confidence_score " +
                     "FROM roots WHERE token_id = ? ORDER BY confidence_score DESC";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Root root = new Root(
                    rs.getInt("root_id"),
                    rs.getInt("token_id"),
                    rs.getString("root_text"),
                    rs.getString("pattern"),
                    rs.getDouble("confidence_score")
                );
                roots.add(root);
            }
            System.out.println("Retrieved " + roots.size() + " roots for token_id: " + tokenId);
        } catch (SQLException e) {
            System.err.println("Error retrieving roots for token_id: " + tokenId + ": " + e.getMessage());
        }
        return roots;
    }
    
    @Override
    public List<Root> retrieveRootsBySentenceId(int sentenceId) {
        List<Root> roots = new ArrayList<>();
        String sql = "SELECT r.root_id, r.token_id, r.root_text, r.pattern, r.confidence_score " +
                     "FROM roots r " +
                     "INNER JOIN tokens t ON r.token_id = t.token_id " +
                     "WHERE t.sentence_id = ? " +
                     "ORDER BY t.position, r.confidence_score DESC";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sentenceId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Root root = new Root(
                    rs.getInt("root_id"),
                    rs.getInt("token_id"),
                    rs.getString("root_text"),
                    rs.getString("pattern"),
                    rs.getDouble("confidence_score")
                );
                roots.add(root);
            }
            System.out.println("Retrieved " + roots.size() + " roots for sentence_id: " + sentenceId);
        } catch (SQLException e) {
            System.err.println("Error retrieving roots for sentence_id: " + sentenceId + ": " + e.getMessage());
        }
        return roots;
    }
    
    @Override
    public boolean deleteRootsByTokenId(int tokenId) {
        String sql = "DELETE FROM roots WHERE token_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            int result = ps.executeUpdate();
            System.out.println("Deleted roots for token_id: " + tokenId + " - " + (result > 0 ? "Success" : "No roots found"));
            return true; // Return true even if no rows deleted
        } catch (SQLException e) {
            System.err.println("Error deleting roots for token_id: " + tokenId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteRoot(int rootId) {
        String sql = "DELETE FROM roots WHERE root_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rootId);
            int result = ps.executeUpdate();
            boolean success = result > 0;
            System.out.println("Deleted root_id: " + rootId + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting root_id: " + rootId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<String> getAllRoots() {
        List<String> roots = new ArrayList<>();
        String sql = "SELECT DISTINCT root_text FROM roots WHERE root_text IS NOT NULL AND TRIM(root_text) <> '' ORDER BY root_text";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roots.add(rs.getString("root_text"));
            }
            System.out.println("Retrieved " + roots.size() + " distinct roots from database.");
        } catch (SQLException e) {
            System.err.println("Error retrieving distinct roots: " + e.getMessage());
        }
        return roots;
    }
    @Override
    public java.util.Map<String, Integer> getRootFrequencyInChapter(int chapterId) {
        java.util.Map<String, Integer> frequencies = new java.util.HashMap<>();
        String sql = "SELECT r.root_text, COUNT(*) as frequency " +
                     "FROM roots r " +
                     "JOIN tokens t ON r.token_id = t.token_id " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "WHERE s.chapter_id = ? " +
                     "GROUP BY r.root_text";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    frequencies.put(rs.getString("root_text"), rs.getInt("frequency"));
                }
            }
            System.out.println("Retrieved root frequencies for chapter_id: " + chapterId);
        } catch (SQLException e) {
            System.err.println("Error retrieving root frequencies for chapter_id " + chapterId + ": " + e.getMessage());
        }
        return frequencies;
    }

    @Override
    public java.util.Map<String, Integer> getRootFrequencyInBook(int bookId) {
        java.util.Map<String, Integer> frequencies = new java.util.HashMap<>();
        String sql = "SELECT r.root_text, COUNT(*) as frequency " +
                     "FROM roots r " +
                     "JOIN tokens t ON r.token_id = t.token_id " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "JOIN chapters c ON s.chapter_id = c.chapter_id " +
                     "WHERE c.book_id = ? " +
                     "GROUP BY r.root_text";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    frequencies.put(rs.getString("root_text"), rs.getInt("frequency"));
                }
            }
            System.out.println("Retrieved root frequencies for book_id: " + bookId);
        } catch (SQLException e) {
            System.err.println("Error retrieving root frequencies for book_id " + bookId + ": " + e.getMessage());
        }
        return frequencies;
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getRootFrequencyBreakdownByBook(int bookId) {
        java.util.Map<String, java.util.Map<String, Integer>> breakdown = new java.util.HashMap<>();
        String sql = "SELECT r.root_text, c.chapter_name, COUNT(*) as frequency " +
                     "FROM roots r " +
                     "JOIN tokens t ON r.token_id = t.token_id " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "JOIN chapters c ON s.chapter_id = c.chapter_id " +
                     "WHERE c.book_id = ? " +
                     "GROUP BY r.root_text, c.chapter_name";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String root = rs.getString("root_text");
                    String chapter = rs.getString("chapter_name");
                    int freq = rs.getInt("frequency");
                    
                    breakdown.computeIfAbsent(root, k -> new java.util.HashMap<>()).put(chapter, freq);
                }
            }
            System.out.println("Retrieved root frequency breakdown for book_id: " + bookId);
        } catch (SQLException e) {
            System.err.println("Error retrieving root frequency breakdown for book_id " + bookId + ": " + e.getMessage());
        }
        return breakdown;
    }
}






