
package BuisnessLayerBL;

import java.util.ArrayList;

import DataAcessLayerDAL.DataAccessFacacde;
import ModelDTO.Book;

public class BookBo implements IBookBo {
    private DataAccessFacacde df;

    public BookBo(DataAccessFacacde df) {
        this.df = df;
    }

    // ✅ ADD BOOK
    @Override
    public boolean addBook(String title, String authorName, String era, String authorname) {
        if (title == null || title.trim().isEmpty() ||
            authorName == null || authorName.trim().isEmpty()) {
            System.out.println("Book title and author name cannot be empty!");
            return false;
        }

        int authorId = df.searchAuthor(authorName.trim());
        if (authorId == -1) {
            System.out.println("Author not found: " + authorName);
            return false;
        }

        // Check if book already exists
        int existingBookId = df.searchBook(title.trim());
        if (existingBookId != -1) {
            System.out.println("Book already exists: " + title);
            return false;
        }

        return df.addBook(title.trim(), authorId, era, authorname.trim());
    }

    // ✅ UPDATE BOOK
    @Override
    public boolean updateBook(String bookName, String newBookName, String authorName, String era) {
        if (bookName == null || newBookName == null ||
            bookName.trim().isEmpty() || newBookName.trim().isEmpty()) {
            System.out.println("Book names cannot be empty!");
            return false;
        }

        int bookId = df.searchBook(bookName.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookName);
            return false;
        }

        int authorId = df.searchAuthor(authorName.trim());
        if (authorId == -1) {
            System.out.println("Author not found: " + authorName);
            return false;
        }

        if (bookName.trim().equalsIgnoreCase(newBookName.trim())) {
            System.out.println("Old and new names are same. Nothing to update.");
            return false;
        }

        return df.updateBook(bookId, authorId, newBookName.trim(), era);
    }

    // ✅ DELETE BOOK
    @Override
    public boolean deleteBook(String bookTitle) {
        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            System.out.println("Book title cannot be empty!");
            return false;
        }

        int bookId = df.searchBook(bookTitle.trim());
        if (bookId == -1) {
            System.out.println("Book not found: " + bookTitle);
            return false;
        }

        return df.deleteBook(bookId);
    }

    // ✅ RETRIEVE ONE BOOK
    @Override
    public Book retrieveBook(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Keyword cannot be null or empty!");
            return null;
        }
        int bookID=df.searchBook(keyword);
        if(bookID==-1) {
        	return null; 
        }

        Book book = df.retrieveBook(bookID);
        if (book == null) {
            System.out.println("No book found for keyword: " + keyword);
            return null;
        }

        return book;
    }

    @Override
    public Book retrieveBook(int id) {
        return df.retrieveBook(id);
    }

    // ✅ SEARCH BOOK ID BY TITLE
    @Override
    public int searchBook(String title) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Book title cannot be null or empty!");
            return -1;
        }
        return df.searchBook(title.trim());
    }

    // ✅ RETRIEVE ALL BOOKS
    @Override
    public ArrayList<Book> retrieveAllBooks() {
        return df.retrieveAllBooks();
    }
}