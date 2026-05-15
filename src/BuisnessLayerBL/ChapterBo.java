
 package BuisnessLayerBL;

import java.util.ArrayList;
import java.util.List;
import ModelDTO.Sentence;
import java.io.File;
import DataAcessLayerDAL.DataAccessFacacde;
import ModelDTO.Chapter;

public class ChapterBo implements IChapterBo {
    private DataAccessFacacde df;

    public ChapterBo(DataAccessFacacde df) {
        this.df = df;
    }

    // ✅ ADD CHAPTER
    public boolean addChapter(String bookName, String chapterName) {
        if (bookName == null || bookName.trim().isEmpty() ||
            chapterName == null || chapterName.trim().isEmpty()) {
            System.out.println("Book name and chapter name cannot be empty!");
            return false;
        }

        int bookId = df.searchBook(bookName.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookName);
            return false;
        }

        // delegate to DAL
        return df.addChapter(bookId, chapterName.trim());
    }

    // ✅ UPDATE CHAPTER (by chapterId)
    public boolean updateChapter(String bookName, int chapterId, String newChapterName) {
        if (bookName == null || bookName.trim().isEmpty() ||
            newChapterName == null || newChapterName.trim().isEmpty()) {
            System.out.println("Book name and new chapter name cannot be empty!");
            return false;
        }

        int bookId = df.searchBook(bookName.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookName);
            return false;
        }

        // Optional: verify chapter belongs to book (if you want)
        Chapter existing = df.retrieveChapter(chapterId);
        if (existing == null) {
            System.out.println("Chapter not found with id: " + chapterId);
            return false;
        }
        if (existing.getBookId() != bookId) {
            System.out.println("Chapter " + chapterId + " does not belong to book: " + bookName);
            return false;
        }

        return df.updateChapter(chapterId, newChapterName.trim());
    }

    // ✅ DELETE CHAPTER
    public boolean deleteChapter(String bookName, int chapterId) {
        if (bookName == null || bookName.trim().isEmpty()) {
            System.out.println("Book name cannot be empty!");
            return false;
        }

        int bookId = df.searchBook(bookName.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookName);
            return false;
        }

        // Optional safety check: ensure chapter belongs to this book
        Chapter existing = df.retrieveChapter(chapterId);
        if (existing == null) {
            System.out.println("Chapter not found with id: " + chapterId);
            return false;
        }
        if (existing.getBookId() != bookId) {
            System.out.println("Chapter " + chapterId + " does not belong to book: " + bookName);
            return false;
        }

        return df.deleteChapter(chapterId);
    }

    // ✅ RETRIEVE ONE CHAPTER
    public Chapter retrieveChapter(String bookName, int chapterId) {
        if (bookName == null || bookName.trim().isEmpty()) {
            System.out.println("Book name cannot be empty!");
            return null;
        }

        int bookId = df.searchBook(bookName.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookName);
            return null;
        }

        Chapter ch = df.retrieveChapter(chapterId);
        if (ch == null) {
            System.out.println("Chapter not found with id: " + chapterId);
            return null;
        }

        if (ch.getBookId() != bookId) {
            System.out.println("Chapter " + chapterId + " does not belong to book: " + bookName);
            return null;
        }

        return ch;
    }

    // ✅ RETRIEVE ALL CHAPTERS FOR A BOOK
    public List<Chapter> retrieveChaptersByBook(String bookName) {
        List<Chapter> empty = new ArrayList<>();

        if (bookName == null || bookName.trim().isEmpty()) {
            System.out.println("Book name cannot be empty!");
            return empty;
        }

        int bookId = df.searchBook(bookName.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookName);
            return empty;
        }

        List<Chapter> list = df.retrieveChaptersByBook(bookId);
        return (list == null) ? empty : list;
    }

    @Override
    public boolean importChapterFromFile(String bookName, String chapterName, String filePath) {
        if (bookName == null || bookName.trim().isEmpty() ||
            chapterName == null || chapterName.trim().isEmpty() ||
            filePath == null || filePath.trim().isEmpty()) {
            System.out.println("Book name, chapter name, and file path cannot be empty!");
            return false;
        }

        int bookId = df.searchBook(bookName.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookName);
            return false;
        }

        // Check if file exists
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found: " + filePath);
            return false;
        }

        return df.importChapterFromFile(bookId, chapterName.trim(), filePath);
    }

    @Override
    public List<Sentence> retrieveSentencesByChapter(String chapterName) {
        if (chapterName == null || chapterName.trim().isEmpty()) {
            System.out.println("Chapter name cannot be empty!");
            return new ArrayList<>();
        }

        int chapterId = df.searchChapter(chapterName.trim());
        if (chapterId == -1) {
            System.out.println("Chapter not found: " + chapterName);
            return new ArrayList<>();
        }

        List<Sentence> sentences = df.retrieveSentencesByChapter(chapterId);
        return sentences != null ? sentences : new ArrayList<>();
    }

    @Override
    public String getChapterNameById(int chapterId) {
        if (chapterId <= 0) {
            System.err.println("Invalid chapter ID: " + chapterId);
            return null;
        }

        Chapter chapter = df.retrieveChapter(chapterId);
        if (chapter == null) {
            System.err.println("Chapter not found with id: " + chapterId);
            return null;
        }

        System.out.println("Retrieved chapter name: " + chapter.getChapterName() + " for chapter_id: " + chapterId);
        return chapter.getChapterName();
    }

    @Override
    public Chapter retrieveChapter(int chapterId) {
        return df.retrieveChapter(chapterId);
    }
}