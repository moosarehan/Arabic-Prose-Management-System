package BuisnessLayerBL;
import java.util.ArrayList;

import ModelDTO.Author;
public interface IAuthorBo {
   public boolean addAuthor(String name,String biography );
   public boolean updateAuthor(String oldname,String newname,String biography);
   public boolean deleteAuthor(String authorname);
   public Author retrieveAuthor (String keyword);
   public int searchAuthor(String authorname);
   
   public ArrayList<Author> retrieveAllAuthors(); 
   public String getAuthorByID(int id);
}