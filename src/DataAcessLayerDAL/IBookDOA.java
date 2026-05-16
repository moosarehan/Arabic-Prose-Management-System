 package DataAcessLayerDAL;
import java.util.ArrayList;

import ModelDTO.Book;
public interface IBookDOA {
	public boolean addBook(String title,int authorId,String era,String authorname);
	   public boolean updateBook(int bookId,int authorID,String newBookName,String era);
	   public boolean deleteBook(int bookId);
	   public Book retrieveBook(int id);
	   public int searchBook(String title);
	   
	   public ArrayList<Book> retrieveAllBooks();
	   
}