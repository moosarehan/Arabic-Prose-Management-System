

package DataAcessLayerDAL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ModelDTO.Sentence;

public class SentenceDOA implements ISentenceDOA {
    public SentenceDOA() {
        // Acquire connection per-method via DBConnection
    }

    @Override
    public boolean addSentence(int chapterID, String text, String textDiacritized, String translation, String notes) {
        // Step 1: Get the maximum sentence_number for the chapter
        String selectSql = "SELECT COALESCE(MAX(sentence_number), 0) FROM sentences WHERE chapter_id = ?";
        int nextSentenceNumber = 1; // Default to 1 if no sentences exist
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
            selectPs.setInt(1, chapterID);
            ResultSet rs = selectPs.executeQuery();
            if (rs.next()) {
                nextSentenceNumber = rs.getInt(1) + 1;
            }
            System.out.println("✅ Calculated next sentence_number: " + nextSentenceNumber + " for chapter_id: " + chapterID);
        } catch (SQLException e) {
            System.err.println("❌ Error calculating sentence_number for chapter_id: " + chapterID + ": " + e.getMessage());
            return false;
        }

        // Step 2: Insert the sentence
        String insertSql = "INSERT INTO sentences (chapter_id, sentence_number, text, text_diacritized, translation, notes) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn1 = DBConnection.getInstance().getConnection();
        try (PreparedStatement insertPs = conn1.prepareStatement(insertSql)) {
            insertPs.setInt(1, chapterID);
            insertPs.setInt(2, nextSentenceNumber);
            insertPs.setString(3, text);
            insertPs.setString(4, textDiacritized != null ? textDiacritized : "");
            insertPs.setString(5, translation != null ? translation : "");
            insertPs.setString(6, notes != null ? notes : "");
            int rows = insertPs.executeUpdate();
            System.out.println("✅ Added sentence to chapter_id: " + chapterID + ", sentence_number: " + nextSentenceNumber + ", text: " + text);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding sentence to chapter_id: " + chapterID + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public int getLastInsertedSentenceId() {
        String sql = "SELECT LAST_INSERT_ID()";
        Connection conn = DBConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("✅ Retrieved last inserted sentence_id: " + id);
                return id;
            }
            System.err.println("❌ No last inserted ID found");
            return -1;
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving last inserted ID: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public Sentence retrieveSentenceById(int sentenceId) {
        String sql = "SELECT * FROM sentences WHERE sentence_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sentenceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Sentence sentence = new Sentence(
                    rs.getInt("sentence_id"),
                    rs.getInt("chapter_id"),
                    rs.getInt("sentence_number"),
                    rs.getString("text"),
                    rs.getString("text_diacritized"),
                    rs.getString("translation"),
                    rs.getString("notes")
                );
                System.out.println("✅ Retrieved sentence_id: " + sentenceId);
                return sentence;
            }
            System.err.println("❌ Sentence not found for sentence_id: " + sentenceId);
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving sentence_id: " + sentenceId + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public Sentence retrieveSentence(int chapterID, int sentenceNumber) {
        String sql = "SELECT * FROM sentences WHERE chapter_id = ? AND sentence_number = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterID);
            ps.setInt(2, sentenceNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Sentence sentence = new Sentence(
                    rs.getInt("sentence_id"),
                    rs.getInt("chapter_id"),
                    rs.getInt("sentence_number"),
                    rs.getString("text"),
                    rs.getString("text_diacritized"),
                    rs.getString("translation"),
                    rs.getString("notes")
                );
                System.out.println("✅ Retrieved sentence: chapter_id=" + chapterID + ", sentence_number=" + sentenceNumber);
                return sentence;
            }
            System.err.println("❌ Sentence not found: chapter_id=" + chapterID + ", sentence_number=" + sentenceNumber);
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving sentence: chapter_id=" + chapterID + ", sentence_number=" + sentenceNumber + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Sentence> retrieveSentencesByChapter(int chapterID) {
        List<Sentence> sentences = new ArrayList<>();
        String sql = "SELECT * FROM sentences WHERE chapter_id = ? ORDER BY sentence_number";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sentences.add(new Sentence(
                    rs.getInt("sentence_id"),
                    rs.getInt("chapter_id"),
                    rs.getInt("sentence_number"),
                    rs.getString("text"),
                    rs.getString("text_diacritized"),
                    rs.getString("translation"),
                    rs.getString("notes")
                ));
            }
            System.out.println("✅ Retrieved " + sentences.size() + " sentences for chapter_id: " + chapterID);
            return sentences;
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving sentences for chapter_id: " + chapterID + ": " + e.getMessage());
            return sentences;
        }
    }

    @Override
    public List<Sentence> retrieveSentencesByBook(int bookId) {
        List<Sentence> sentences = new ArrayList<>();
        String sql = "SELECT s.* FROM sentences s JOIN chapters c ON s.chapter_id = c.chapter_id WHERE c.book_id = ? ORDER BY s.chapter_id, s.sentence_number";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sentences.add(new Sentence(
                    rs.getInt("sentence_id"),
                    rs.getInt("chapter_id"),
                    rs.getInt("sentence_number"),
                    rs.getString("text"),
                    rs.getString("text_diacritized"),
                    rs.getString("translation"),
                    rs.getString("notes")
                ));
            }
            System.out.println("✅ Retrieved " + sentences.size() + " sentences for book_id: " + bookId);
            return sentences;
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving sentences for book_id: " + bookId + ": " + e.getMessage());
            return sentences;
        }
    }

    @Override
    public List<Sentence> retrieveAllSentences() {
        List<Sentence> sentences = new ArrayList<>();
        String sql = "SELECT * FROM sentences ORDER BY chapter_id, sentence_number";
        Connection conn = DBConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sentences.add(new Sentence(
                    rs.getInt("sentence_id"),
                    rs.getInt("chapter_id"),
                    rs.getInt("sentence_number"),
                    rs.getString("text"),
                    rs.getString("text_diacritized"),
                    rs.getString("translation"),
                    rs.getString("notes")
                ));
            }
            System.out.println("✅ Retrieved " + sentences.size() + " sentences from all books");
            return sentences;
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving all sentences: " + e.getMessage());
            return sentences;
        }
    }

    @Override
    public boolean updateSentence(int chapterID, int sentenceNumber, String newText, String newDiacritized, String newTranslation, String newNotes) {
        String sql = "UPDATE sentences SET text = ?, text_diacritized = ?, translation = ?, notes = ? WHERE chapter_id = ? AND sentence_number = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newText);
            ps.setString(2, newDiacritized != null ? newDiacritized : "");
            ps.setString(3, newTranslation != null ? newTranslation : "");
            ps.setString(4, newNotes != null ? newNotes : "");
            ps.setInt(5, chapterID);
            ps.setInt(6, sentenceNumber);
            int rows = ps.executeUpdate();
            System.out.println("✅ Updated sentence: chapter_id=" + chapterID + ", sentence_number=" + sentenceNumber);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error updating sentence: chapter_id=" + chapterID + ", sentence_number=" + sentenceNumber + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSentence(int chapterID, int sentenceNumber) {
        String sql = "DELETE FROM sentences WHERE chapter_id = ? AND sentence_number = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterID);
            ps.setInt(2, sentenceNumber);
            int rows = ps.executeUpdate();
            System.out.println("✅ Deleted sentence: chapter_id=" + chapterID + ", sentence_number=" + sentenceNumber);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error deleting sentence: chapter_id=" + chapterID + ", sentence_number=" + sentenceNumber + ": " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<Sentence> searchSentencesByExactString(String phrase) {
        List<Sentence> sentences = new ArrayList<>();
        String normalized = normalizePhrase(phrase);
        if (normalized.isEmpty()) {
            return sentences;
        }

        // Split the phrase into tokens
        String[] tokens = normalized.split("\\s+");
        StringBuilder whereClause = new StringBuilder();
        List<String> params = new ArrayList<>();

        if (tokens.length == 1) {
            // For single token, match exact word with word boundaries
            whereClause.append("(text REGEXP ?)");
            params.add("(^|\\s)" + Pattern.quote(tokens[0]) + "(\\s|$)");
        } else {
            // For multiple tokens, match exact phrase with word boundaries
            whereClause.append("(text REGEXP ?)");
            String regex = "(^|\\s)" + 
                          Arrays.stream(tokens)
                               .map(Pattern::quote)
                               .collect(Collectors.joining("\\s+")) + 
                          "(\\s|$)";
            params.add(regex);
        }

        String sql = "SELECT * FROM sentences WHERE " + whereClause + " ORDER BY chapter_id, sentence_number";
        Connection activeConnection = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = activeConnection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sentences.add(new Sentence(
                    rs.getInt("sentence_id"),
                    rs.getInt("chapter_id"),
                    rs.getInt("sentence_number"),
                    rs.getString("text"),
                    rs.getString("text_diacritized"),
                    rs.getString("translation"),
                    rs.getString("notes")
                ));
            }
            System.out.println("✅ Exact string search for '" + normalized + "' returned " + sentences.size() + " sentences");
        } catch (SQLException e) {
            System.err.println("❌ Error searching sentences by exact string '" + normalized + "': " + e.getMessage());
        }
        return sentences;
    } 
    /*
    @Override
    public List<Sentence> searchSentencesByExactString(String phrase) {
        List<Sentence> sentences = new ArrayList<>();
        String normalized = normalizePhrase(phrase);
        if (normalized.isEmpty()) {
            return sentences;
        }

        String sql = "SELECT * FROM sentences WHERE text LIKE ? ORDER BY chapter_id, sentence_number";
        Connection activeConnection = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = activeConnection.prepareStatement(sql)) {
            ps.setString(1, "%" + normalized + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sentences.add(new Sentence(
                    rs.getInt("sentence_id"),
                    rs.getInt("chapter_id"),
                    rs.getInt("sentence_number"),
                    rs.getString("text"),
                    rs.getString("text_diacritized"),
                    rs.getString("translation"),
                    rs.getString("notes")
                ));
            }
            System.out.println("✅ Exact string search for '" + normalized + "' returned " + sentences.size() + " sentences");
        } catch (SQLException e) {
            System.err.println("❌ Error searching sentences by exact string '" + normalized + "': " + e.getMessage());
        }
        return sentences;
    }
*/
    private String normalizePhrase(String phrase) {
        if (phrase == null) {
            return "";
        }
        return phrase.trim().replaceAll("\\s+", " ");
    }
    
    public List<Map<String, String>> searchSentencesByRegex(String pattern) {
        List<Map<String, String>> results = new ArrayList<>();
        if (pattern == null || pattern.trim().isEmpty()) {
            return results;
        }
        
        String sql = "SELECT sentence_id, chapter_id, sentence_number, text AS sentence_text " +
                     "FROM sentences WHERE text REGEXP ? ORDER BY chapter_id, sentence_number";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("sentence_id", String.valueOf(rs.getInt("sentence_id")));
                    row.put("chapter_id", String.valueOf(rs.getInt("chapter_id")));
                    row.put("sentence_number", String.valueOf(rs.getInt("sentence_number")));
                    row.put("sentence_text", rs.getString("sentence_text"));
                    results.add(row);
                }
            }
            System.out.println("✅ Regex search '" + pattern + "' returned " + results.size() + " sentences");
        } catch (SQLException e) {
            System.err.println("❌ Error performing regex search '" + pattern + "': " + e.getMessage());
        }
        return results;
    }
} 
