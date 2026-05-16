package ModelDTO;

public class Author {
	private int authorId;
    private String name;
    private String biography;
 
    public Author() {}
    public Author(String name,String biography) {
    	this.name=name;
    	this.biography=biography;
    }
    public Author(int authorId, String name, String biography) {
        this.authorId = authorId;
        this.name = name;
        this.biography = biography;
    }
    //getters and setters 
    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    @Override
    public String toString() {
        return name;
    }
}