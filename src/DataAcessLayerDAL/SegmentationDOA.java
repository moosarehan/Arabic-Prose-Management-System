package DataAcessLayerDAL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ModelDTO.TokenSegmentation;

public class SegmentationDOA implements ISegmentationDOA {
    public SegmentationDOA() {
        // Acquire connection per-method via DBConnection
    }
    
    @Override
    public boolean addSegmentation(int tokenId, TokenSegmentation segmentation) {
        String sql = "INSERT INTO token_segmentation (token_id, prefix, stem, suffix) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE prefix = VALUES(prefix), stem = VALUES(stem), suffix = VALUES(suffix)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.setString(2, segmentation.getPrefix());
            ps.setString(3, segmentation.getStem());
            ps.setString(4, segmentation.getSuffix());
            
            int result = ps.executeUpdate();
            boolean success = result > 0;
            System.out.println("Added segmentation for token_id: " + tokenId + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding segmentation for token_id: " + tokenId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean addSegmentationsForTokens(List<TokenSegmentation> segmentations) {
        String sql = "INSERT INTO token_segmentation (token_id, prefix, stem, suffix) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE prefix = VALUES(prefix), stem = VALUES(stem), suffix = VALUES(suffix)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (TokenSegmentation seg : segmentations) {
                ps.setInt(1, seg.getTokenId());
                ps.setString(2, seg.getPrefix());
                ps.setString(3, seg.getStem());
                ps.setString(4, seg.getSuffix());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            boolean success = results.length == segmentations.size();
            System.out.println("Added " + segmentations.size() + " segmentations - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding segmentations: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateSegmentation(int tokenId, TokenSegmentation segmentation) {
        String sql = "UPDATE token_segmentation SET prefix = ?, stem = ?, suffix = ? WHERE token_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, segmentation.getPrefix());
            ps.setString(2, segmentation.getStem());
            ps.setString(3, segmentation.getSuffix());
            ps.setInt(4, tokenId);
            
            int result = ps.executeUpdate();
            boolean success = result > 0;
            System.out.println("Updated segmentation for token_id: " + tokenId + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating segmentation for token_id: " + tokenId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public TokenSegmentation retrieveSegmentationByTokenId(int tokenId) {
        String sql = "SELECT segmentation_id, token_id, prefix, stem, suffix " +
                     "FROM token_segmentation WHERE token_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TokenSegmentation segmentation = new TokenSegmentation(
                        rs.getInt("segmentation_id"),
                        rs.getInt("token_id"),
                        rs.getString("prefix"),
                        rs.getString("stem"),
                        rs.getString("suffix")
                    );
                    System.out.println("Retrieved segmentation for token_id: " + tokenId);
                    return segmentation;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving segmentation for token_id: " + tokenId + ": " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<TokenSegmentation> retrieveSegmentationsBySentenceId(int sentenceId) {
        List<TokenSegmentation> segmentations = new ArrayList<>();
        String sql = "SELECT ts.segmentation_id, ts.token_id, ts.prefix, ts.stem, ts.suffix " +
                     "FROM token_segmentation ts " +
                     "INNER JOIN tokens t ON ts.token_id = t.token_id " +
                     "WHERE t.sentence_id = ? " +
                     "ORDER BY t.position";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sentenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TokenSegmentation segmentation = new TokenSegmentation(
                        rs.getInt("segmentation_id"),
                        rs.getInt("token_id"),
                        rs.getString("prefix"),
                        rs.getString("stem"),
                        rs.getString("suffix")
                    );
                    segmentations.add(segmentation);
                }
            }
            System.out.println("Retrieved " + segmentations.size() + " segmentations for sentence_id: " + sentenceId);
        } catch (SQLException e) {
            System.err.println("Error retrieving segmentations for sentence_id: " + sentenceId + ": " + e.getMessage());
        }
        return segmentations;
    }
    
    @Override
    public boolean deleteSegmentation(int tokenId) {
        String sql = "DELETE FROM token_segmentation WHERE token_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            int result = ps.executeUpdate();
            boolean success = result > 0;
            System.out.println("Deleted segmentation for token_id: " + tokenId + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting segmentation for token_id: " + tokenId + ": " + e.getMessage());
            return false;
        }
    }
}

