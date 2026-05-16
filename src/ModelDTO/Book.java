package ModelDTO;

public class Book {
 private int bookid;
 private String title;
 private int authorid;
 private String era;
 public Book() {}

 public Book(int bookId, String title, int authorId, String era) {
     this.bookid = bookId;
     this.title = title;
     this.authorid = authorId;
     this.era = era;
 }

 public Book(String title, int authorId, String era) {
     this.title = title;
     this.authorid = authorId;
     this.era = era;
 }

 // Getters and Setters
 public int getBookId() {
     return bookid;
 }

 public void setBookId(int bookId) {
     this.bookid = bookId;
 }

 public String getTitle() {
     return title;
 }

 public void setTitle(String title) {
     this.title = title;
 }

 public int getAuthorId() {
     return authorid;
 }

 public void setAuthorId(int authorId) {
     this.authorid = authorId;
 }



 public String getEra() {
     return era;
 }

 public void setEra(String era) {
     this.era = era;
 }
}