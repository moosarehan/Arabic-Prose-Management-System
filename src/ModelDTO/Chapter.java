
 package ModelDTO;

public class Chapter {
    private int chapterId;
    private int bookId;
    private String chapterName;

    public Chapter() {}

    // recommended constructor (id, bookId, name)
    public Chapter(int chapterId, int bookId, String chapterName) {
        this.chapterId = chapterId;
        this.bookId = bookId;
        this.chapterName = chapterName;
    }

    // convenience constructor for creating before DB insert
    public Chapter(int bookId, String chapterName) {
        this.bookId = bookId;
        this.chapterName = chapterName;
    }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getChapterName() { return chapterName; }
    public void setChapterName(String chapterName) { this.chapterName = chapterName; }

    @Override
    public String toString() { return chapterName; }
}
