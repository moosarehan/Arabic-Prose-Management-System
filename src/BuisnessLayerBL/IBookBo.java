
 
package BuisnessLayerBL;
import java.util.ArrayList;

import ModelDTO.Book;
public interface IBookBo {
   public boolean addBook(String title,String authorName,String era,String authorname);
   public boolean updateBook(String bookName,String newBookName,String authorName,String era);
   public boolean deleteBook(String bookTitle);
   public Book retrieveBook(String keyword);
   public Book retrieveBook(int id);
   public int searchBook(String title);
   
   public ArrayList<Book> retrieveAllBooks();
   
   
}