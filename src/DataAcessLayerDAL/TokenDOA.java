package DataAcessLayerDAL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ModelDTO.TokenData;

public class TokenDOA implements ITokenDOA {
    public TokenDOA() {
        // No cached connection; acquire per-method to allow DBConnection reconnection
    }
    
 // Update in TokenDOA.java
    @Override
    public boolean addTokensForSentence(int sentenceId, List<TokenData> tokens) {
        String sql = "INSERT INTO tokens (sentence_id, token_text, lemma, position) VALUES (?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (TokenData td : tokens) {
                ps.setInt(1, sentenceId);
                ps.setString(2, td.getTokenText());
                
                // Ensure lemma is never null
                String lemma = td.getLemma();
                if (lemma == null || lemma.trim().isEmpty()) {
                    // If lemma is not provided, use the token text as fallback
                    lemma = td.getTokenText();
                }
                ps.setString(3, lemma);
                
                ps.setInt(4, td.getPosition());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            boolean success = results.length == tokens.size();
            System.out.println("Added " + tokens.size() + " tokens for sentence_id: " + sentenceId + " - " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding tokens for sentence_id: " + sentenceId + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateTokensForSentence(int sentenceId, List<TokenData> tokens) {
        String deleteSql = "DELETE FROM tokens WHERE sentence_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            deletePs.setInt(1, sentenceId);
            int deleted = deletePs.executeUpdate();
            System.out.println("Deleted " + deleted + " old tokens for sentence_id: " + sentenceId);
        } catch (SQLException e) {
            System.err.println("Error deleting old tokens for sentence_id: " + sentenceId + ": " + e.getMessage());
            return false;
        }
        return addTokensForSentence(sentenceId, tokens);
    }
    
    public List<TokenData> retrieveTokensBySentenceId(int sentenceId) {
        List<TokenData> tokens = new ArrayList<>();
        String query = "SELECT token_id, sentence_id, token_text, lemma, position "
                     + "FROM tokens WHERE sentence_id = ? ORDER BY position";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, sentenceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TokenData token = new TokenData(
                        rs.getInt("token_id"),
                        rs.getInt("sentence_id"),
                        rs.getString("token_text"),
                        rs.getString("lemma"),
                        rs.getInt("position")
                    );
                    tokens.add(token);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving tokens for sentence_id " 
                               + sentenceId + ": " + e.getMessage());
        }

        System.out.println("✅ Retrieved " + tokens.size() 
                           + " tokens for sentence_id " + sentenceId);

        return tokens;
    }

    
    @Override
    public List<TokenData> retrieveTokensForSentence(int sentenceId) {
        List<TokenData> tokens = new ArrayList<>();
        String sql = "SELECT token_id, sentence_id, token_text, lemma, position FROM tokens WHERE sentence_id = ? ORDER BY position";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sentenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(new TokenData(
                            rs.getInt("token_id"),
                            rs.getInt("sentence_id"),
                            rs.getString("token_text"),
                            rs.getString("lemma"),
                            rs.getInt("position")));
                }
            }
            System.out.println("Retrieved " + tokens.size() + " tokens for sentence_id: " + sentenceId);
        } catch (SQLException e) {
            System.err.println("Error retrieving tokens for sentence_id: " + sentenceId + ": " + e.getMessage());
        }
        return tokens;
    }

    @Override
    public List<TokenData> retrieveTokensBySentence(int sentenceId) {
        List<TokenData> tokens = new ArrayList<>();
        String query = "SELECT token_id, sentence_id, token_text, lemma, position FROM tokens WHERE sentence_id = ? ORDER BY position";

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, sentenceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TokenData token = new TokenData(
                        rs.getInt("token_id"),
                        rs.getInt("sentence_id"),
                        rs.getString("token_text"),
                        rs.getString("lemma"),
                        rs.getInt("position")
                    );
                    tokens.add(token);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving tokens for sentence_id " + sentenceId + ": " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("✅ Retrieved " + tokens.size() + " tokens for sentence_id: " + sentenceId);
        return tokens;
    }
 // In DataAcessLayerDAL/TokenDOA.java, update the findSentencesByToken method:
    public List<Map<String, String>> findSentencesByToken(String tokenText) {
        List<Map<String, String>> results = new ArrayList<>();
        String sql = "SELECT s.sentence_id, s.text AS sentence_text, " +
                    "b.book_id, b.title AS book_title, " +
                    "c.chapter_id, c.chapter_name, " +
                    "a.name AS author_name " +
                    "FROM tokens t " +
                    "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                    "JOIN chapters c ON s.chapter_id = c.chapter_id " +
                    "JOIN books b ON c.book_id = b.book_id " +
                    "JOIN authors a ON b.author_id = a.author_id " +
                    "WHERE t.token_text = ? " +
                    "ORDER BY b.title, c.chapter_name, s.sentence_number";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenText);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("sentence_id", String.valueOf(rs.getInt("sentence_id")));
                    row.put("sentence_text", rs.getString("sentence_text"));
                    row.put("book_id", String.valueOf(rs.getInt("book_id")));
                    row.put("book_title", rs.getString("book_title"));
                    row.put("chapter_id", String.valueOf(rs.getInt("chapter_id")));
                    row.put("chapter_name", rs.getString("chapter_name"));
                    row.put("author_name", rs.getString("author_name"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding token occurrences: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error while finding token occurrences", e);
        }
        return results;
    }
    
    @Override
    public List<TokenData> getTokensByRoot(String rootText) {
        List<TokenData> tokens = new ArrayList<>();
        if (rootText == null || rootText.trim().isEmpty()) {
            return tokens;
        }
        
        String sql = "SELECT t.token_id, t.sentence_id, t.token_text, t.lemma, t.position " +
                     "FROM tokens t " +
                     "INNER JOIN roots r ON t.token_id = r.token_id " +
                     "WHERE r.root_text = ? " +
                     "ORDER BY t.token_text ASC";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rootText);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(new TokenData(
                        rs.getInt("token_id"),
                        rs.getInt("sentence_id"),
                        rs.getString("token_text"),
                        rs.getString("lemma"),
                        rs.getInt("position")
                    ));
                }
            }
            System.out.println("Retrieved " + tokens.size() + " tokens for root: " + rootText);
        } catch (SQLException e) {
            System.err.println("Error retrieving tokens for root '" + rootText + "': " + e.getMessage());
        }
        return tokens;
    }

    @Override
    public List<String> getAllDistinctTokens() {
        List<String> tokens = new ArrayList<>();
        String sql = "SELECT DISTINCT token_text FROM tokens ORDER BY token_text ASC";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tokens.add(rs.getString("token_text"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving distinct tokens: " + e.getMessage());
        }
        System.out.println("Retrieved " + tokens.size() + " distinct tokens from DB");
        return tokens;
    }

    @Override
    public List<String> getAllDistinctLemmas() {
        List<String> lemmas = new ArrayList<>();
        String sql = "SELECT DISTINCT lemma FROM tokens WHERE lemma IS NOT NULL AND TRIM(lemma) <> '' ORDER BY lemma ASC";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lemmas.add(rs.getString("lemma"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving distinct lemmas: " + e.getMessage());
        }
        System.out.println("Retrieved " + lemmas.size() + " distinct lemmas from DB");
        return lemmas;
    }

    @Override
    public List<TokenData> getTokensByLemma(String lemma) {
        List<TokenData> tokens = new ArrayList<>();
        if (lemma == null || lemma.trim().isEmpty()) return tokens;

        String sql = "SELECT token_id, sentence_id, token_text, lemma, position FROM tokens WHERE lemma = ? ORDER BY token_text ASC";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lemma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(new TokenData(
                        rs.getInt("token_id"),
                        rs.getInt("sentence_id"),
                        rs.getString("token_text"),
                        rs.getString("lemma"),
                        rs.getInt("position")
                    ));
                }
            }
            System.out.println("Retrieved " + tokens.size() + " tokens for lemma: " + lemma);
        } catch (SQLException e) {
            System.err.println("Error retrieving tokens for lemma '" + lemma + "': " + e.getMessage());
        }
        return tokens;
    }

    @Override
    public List<String> getAllDistinctSegments() {
        List<String> segments = new ArrayList<>();
        String sql = "SELECT DISTINCT token_text FROM tokens";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            java.util.Set<String> set = new java.util.TreeSet<>();
            while (rs.next()) {
                String tokenText = rs.getString("token_text");
                String[] seg = util.SegmentationUtil.getInstance().segmentToken(tokenText);
                String stem = seg.length > 1 ? seg[1] : tokenText;
                if (stem != null && !stem.trim().isEmpty()) set.add(stem);
            }
            segments.addAll(set);
        } catch (SQLException e) {
            System.err.println("Error retrieving distinct segments: " + e.getMessage());
        }
        System.out.println("Retrieved " + segments.size() + " distinct segments from DB");
        return segments;
    }

    @Override
    public List<TokenData> getTokensBySegment(String segment) {
        List<TokenData> tokens = new ArrayList<>();
        if (segment == null || segment.trim().isEmpty()) return tokens;

        // First find token_text values whose stem equals the requested segment
        String distinctSql = "SELECT DISTINCT token_text FROM tokens";
        List<String> matchingTokenTexts = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(distinctSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tokenText = rs.getString("token_text");
                String[] seg = util.SegmentationUtil.getInstance().segmentToken(tokenText);
                String stem = seg.length > 1 ? seg[1] : tokenText;
                if (segment.equals(stem)) matchingTokenTexts.add(tokenText);
            }
        } catch (SQLException e) {
            System.err.println("Error finding token texts for segment: " + e.getMessage());
            return tokens;
        }

        if (matchingTokenTexts.isEmpty()) return tokens;

        // Now fetch full token rows for those token_text values
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < matchingTokenTexts.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }
        String sql = "SELECT token_id, sentence_id, token_text, lemma, position FROM tokens WHERE token_text IN (" + inClause.toString() + ") ORDER BY token_text";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < matchingTokenTexts.size(); i++) ps.setString(i + 1, matchingTokenTexts.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(new TokenData(
                        rs.getInt("token_id"),
                        rs.getInt("sentence_id"),
                        rs.getString("token_text"),
                        rs.getString("lemma"),
                        rs.getInt("position")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving tokens for segment '" + segment + "': " + e.getMessage());
        }

        System.out.println("Retrieved " + tokens.size() + " tokens for segment: " + segment);
        return tokens;
    }

    public List<String> getLemmasByRoot(String rootText) {
        List<String> lemmas = new ArrayList<>();
        if (rootText == null || rootText.trim().isEmpty()) return lemmas;
        
        String sql = "SELECT DISTINCT t.lemma " +
                     "FROM tokens t " +
                     "INNER JOIN roots r ON t.token_id = r.token_id " +
                     "WHERE r.root_text = ? AND t.lemma IS NOT NULL AND TRIM(t.lemma) <> '' " +
                     "ORDER BY t.lemma ASC";
                     
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rootText);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lemmas.add(rs.getString("lemma"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lemmas for root '" + rootText + "': " + e.getMessage());
        }
        return lemmas;
    }

    @Override
    public java.util.Map<String, Integer> getTokenFrequencyInChapter(int chapterId) {
        java.util.Map<String, Integer> frequencies = new java.util.HashMap<>();
        String sql = "SELECT t.token_text, COUNT(*) as frequency " +
                     "FROM tokens t " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "WHERE s.chapter_id = ? " +
                     "GROUP BY t.token_text";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    frequencies.put(rs.getString("token_text"), rs.getInt("frequency"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving token frequencies for chapter_id " + chapterId + ": " + e.getMessage());
        }
        return frequencies;
    }

    @Override
    public java.util.Map<String, Integer> getLemmaFrequencyInChapter(int chapterId) {
        java.util.Map<String, Integer> frequencies = new java.util.HashMap<>();
        String sql = "SELECT t.lemma, COUNT(*) as frequency " +
                     "FROM tokens t " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "WHERE s.chapter_id = ? AND t.lemma IS NOT NULL AND TRIM(t.lemma) <> '' " +
                     "GROUP BY t.lemma";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    frequencies.put(rs.getString("lemma"), rs.getInt("frequency"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lemma frequencies for chapter_id " + chapterId + ": " + e.getMessage());
        }
        return frequencies;
    }

    @Override
    public java.util.Map<String, Integer> getTokenFrequencyInBook(int bookId) {
        java.util.Map<String, Integer> frequencies = new java.util.HashMap<>();
        String sql = "SELECT t.token_text, COUNT(*) as frequency " +
                     "FROM tokens t " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "JOIN chapters c ON s.chapter_id = c.chapter_id " +
                     "WHERE c.book_id = ? " +
                     "GROUP BY t.token_text";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    frequencies.put(rs.getString("token_text"), rs.getInt("frequency"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving token frequencies for book_id " + bookId + ": " + e.getMessage());
        }
        return frequencies;
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getTokenFrequencyBreakdownByBook(int bookId) {
        java.util.Map<String, java.util.Map<String, Integer>> breakdown = new java.util.HashMap<>();
        String sql = "SELECT t.token_text, c.chapter_name, COUNT(*) as frequency " +
                     "FROM tokens t " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "JOIN chapters c ON s.chapter_id = c.chapter_id " +
                     "WHERE c.book_id = ? " +
                     "GROUP BY t.token_text, c.chapter_name";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String token = rs.getString("token_text");
                    String chapter = rs.getString("chapter_name");
                    int freq = rs.getInt("frequency");
                    breakdown.computeIfAbsent(token, k -> new java.util.HashMap<>()).put(chapter, freq);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving token frequency breakdown for book_id " + bookId + ": " + e.getMessage());
        }
        return breakdown;
    }

    @Override
    public java.util.Map<String, Integer> getLemmaFrequencyInBook(int bookId) {
        java.util.Map<String, Integer> frequencies = new java.util.HashMap<>();
        String sql = "SELECT t.lemma, COUNT(*) as frequency " +
                     "FROM tokens t " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "JOIN chapters c ON s.chapter_id = c.chapter_id " +
                     "WHERE c.book_id = ? AND t.lemma IS NOT NULL AND TRIM(t.lemma) <> '' " +
                     "GROUP BY t.lemma";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    frequencies.put(rs.getString("lemma"), rs.getInt("frequency"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lemma frequencies for book_id " + bookId + ": " + e.getMessage());
        }
        return frequencies;
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getLemmaFrequencyBreakdownByBook(int bookId) {
        java.util.Map<String, java.util.Map<String, Integer>> breakdown = new java.util.HashMap<>();
        String sql = "SELECT t.lemma, c.chapter_name, COUNT(*) as frequency " +
                     "FROM tokens t " +
                     "JOIN sentences s ON t.sentence_id = s.sentence_id " +
                     "JOIN chapters c ON s.chapter_id = c.chapter_id " +
                     "WHERE c.book_id = ? AND t.lemma IS NOT NULL AND TRIM(t.lemma) <> '' " +
                     "GROUP BY t.lemma, c.chapter_name";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String lemma = rs.getString("lemma");
                    String chapter = rs.getString("chapter_name");
                    int freq = rs.getInt("frequency");
                    breakdown.computeIfAbsent(lemma, k -> new java.util.HashMap<>()).put(chapter, freq);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lemma frequency breakdown for book_id " + bookId + ": " + e.getMessage());
        }
        return breakdown;
    }
}