package DataAcessLayerDAL;
import ModelDTO.Chapter;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ChapterDOA implements IChapterDOA {
    public ChapterDOA() {
        // Acquire connection per-method via DBConnection
    	
    }

    @Override
    public boolean addChapter(int bookId, String chapterName) {
        String sql = "INSERT INTO chapters (book_id, chapter_name) VALUES (?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setString(2, chapterName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding chapter:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateChapter(int chapterId, String newName) {
        String sql = "UPDATE chapters SET chapter_name = ? WHERE chapter_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, chapterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating chapter:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteChapter(int chapterId) {
        String sql = "DELETE FROM chapters WHERE chapter_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting chapter:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Chapter retrieveChapter(int chapterId) {
        String sql = "SELECT * FROM chapters WHERE chapter_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Chapter(
                        rs.getInt("chapter_id"),
                        rs.getInt("book_id"),
                        rs.getString("chapter_name")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving chapter:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Chapter> retrieveChaptersByBook(int bookId) {
        List<Chapter> list = new ArrayList<>();
        String sql = "SELECT * FROM chapters WHERE book_id = ? ORDER BY chapter_id ASC";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Chapter(
                        rs.getInt("chapter_id"),
                        rs.getInt("book_id"),
                        rs.getString("chapter_name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving chapters by book:");
            e.printStackTrace();
        }
        return list;
    }


    @Override
    public int searchChapter(String name) {
        String sql = "SELECT chapter_id FROM chapters WHERE chapter_name = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("chapter_id");
                System.out.println("✅ Found chapter: " + name + ", chapter_id: " + id);
                return id;
            }
            System.err.println("❌ Chapter not found: " + name);
            return -1;
        } catch (SQLException e) {
            System.err.println("❌ Error searching chapter: " + name + ": " + e.getMessage());
            return -1;
        }
    }
    
    @Override
    public Chapter getChapterById(int chapterId) {
        String sql = "SELECT * FROM chapters WHERE chapter_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Chapter chapter = new Chapter(rs.getInt("chapter_id"), rs.getInt("book_id"), rs.getString("chapter_name"));
                System.out.println("Retrieved chapter_id: " + chapterId);
                return chapter;
            }
            System.err.println("Chapter not found for chapter_id: " + chapterId);
        } catch (SQLException e) {
            System.err.println("Error retrieving chapter by ID: " + chapterId + ": " + e.getMessage());
        }
        return null;
    }
	    @Override
    public List<Integer> getChapterIdsByBook(int bookId) {
        List<Integer> chapterIds = new ArrayList<>();
        String sql = "SELECT chapter_id FROM chapters WHERE book_id = ? ORDER BY chapter_id";
        
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chapterIds.add(rs.getInt("chapter_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting chapter IDs by book:");
            e.printStackTrace();
        }
        return chapterIds;
    }

    @Override
    public boolean importChapterFromFile(int bookId, String chapterName, String filePath) {
    	Connection conn = DBConnection.getInstance().getConnection();
        try {
            // First, create the chapter
            String insertChapterSql = "INSERT INTO chapters (book_id, chapter_name) VALUES (?, ?)";
            int chapterId = -1;
            
            try (PreparedStatement chapterPs = conn.prepareStatement(insertChapterSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                chapterPs.setInt(1, bookId);
                chapterPs.setString(2, chapterName);
                int affectedRows = chapterPs.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Creating chapter failed, no rows affected.");
                }
                
                try (ResultSet generatedKeys = chapterPs.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        chapterId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating chapter failed, no ID obtained.");
                    }
                }
            }
            
            // Read file and insert sentences
            List<String> lines = readFileLines(filePath);
            if (lines.isEmpty()) {
            	System.out.println("No lines fount");
                return false;
            }
            
            // First line is title (optional - we already have chapterName)
            // Start from first line as content
            String insertSentenceSql = "INSERT INTO sentences (chapter_id, sentence_number, text) VALUES (?, ?, ?)";
            
            try (PreparedStatement sentencePs = conn.prepareStatement(insertSentenceSql)) {
                int sentenceNumber = 1;
                
                for (String line : lines) {
                    // Skip empty lines and footnotes
                    if (line.trim().isEmpty() || isFootnote(line)) {
                        continue;
                    }
                    
                    sentencePs.setInt(1, chapterId);
                    sentencePs.setInt(2, sentenceNumber);
                    sentencePs.setString(3, line.trim());
                    sentencePs.addBatch();
                    
                    sentenceNumber++;
                    
                    // Execute batch every 100 sentences
                    if (sentenceNumber % 100 == 0) {
                        sentencePs.executeBatch();
                    }
                }
                
                // Execute remaining batch
                sentencePs.executeBatch();
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error importing chapter from file:");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Error reading file:");
            e.printStackTrace();
            return false;
        }
    }
    
    private List<String> readFileLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return lines;
    }
    
    private boolean isFootnote(String line) {
        // Check if line is a footnote (contains numbers in brackets, asterisks, etc.)
        String trimmed = line.trim();
        return trimmed.matches(".*\\\\[\\\\d+\\\\].*") ||  // [1], [2], etc.
               trimmed.matches("^\\\\*.*") ||              // * footnote
               trimmed.matches("^\\\\d+\\\\.?.*") ||       // 1. numbered footnote
               trimmed.length() < 10;                      // Very short lines might be footnotes
    }
    
    @Override
    public String getChapterNameById(int chapterId) {
        if (chapterId <= 0) {
            System.err.println("Invalid chapter ID: " + chapterId);
            return null;
        }
        String sql = "SELECT chapter_name FROM chapters WHERE chapter_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String chapterName = rs.getString("chapter_name");
                System.out.println("Retrieved chapter name: " + chapterName + " for chapter_id: " + chapterId);
                return chapterName;
            }
            System.err.println("Chapter not found for chapter_id: " + chapterId);
        } catch (SQLException e) {
            System.err.println("Error retrieving chapter name for chapter_id: " + chapterId + ": " + e.getMessage());
        }
        return null;
    }
}
