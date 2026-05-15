package BuisnessLayerBL;

import java.util.ArrayList;

import DataAcessLayerDAL.DataAccessFacacde;
import ModelDTO.Author;

public class AuthorBo implements IAuthorBo{
	DataAccessFacacde df;
	public AuthorBo(DataAccessFacacde df)
	{
		this.df=df;
	}
	public boolean addAuthor(String name,String biography )
	{
		return df.addAuthor(name, biography);
	}
	public boolean updateAuthor(String oldname,String newname,String biography)
	  { 
		if (oldname == null || newname == null || oldname.trim().isEmpty() || newname.trim().isEmpty()) {
	        System.out.println("Author names cannot be empty!");
	        return false;
	    }

		  // we check if author is present or not
		   int authorid=df.searchAuthor(oldname.trim());
		   if(authorid==-1) {
			 
			   return false;
		   }
		   
		   return df.updateAuthor(oldname.trim(), newname.trim(), biography);
	  }
	   public boolean deleteAuthor(String authorname)
	   {
		   int authorid=df.searchAuthor(authorname);
		   if(authorid==-1) {
			   return false;
		   }
		   return df.deleteAuthor(authorname);
	   }
	   public Author retrieveAuthor (String keyword)
	   {
		   if(keyword.trim().isEmpty()||keyword==null)
		   {
			   return null;
		   }
		  
		   
		  Author author=  df.retrieveAuthor(keyword.trim());
		  if(author==null)
		  {
			  return null;
		  }
		  return author;
	   }
	   public int searchAuthor(String authorname)
	   {
		   if(authorname==null||authorname.trim().isEmpty())
		   {
			   System.out.println("INVALID AUTHOR NAME");
		   }
			
			   return df.searchAuthor(authorname.trim());
	   }
	   @Override
	   public ArrayList<Author> retrieveAllAuthors() {
		return df.retrieveAllAuthors();
	   }
	   @Override
	   public String getAuthorByID(int id) {
		return df.getAuthorByID(id);
	   }
}
