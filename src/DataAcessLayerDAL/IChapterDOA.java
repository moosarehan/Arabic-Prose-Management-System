 package DataAcessLayerDAL;

import ModelDTO.Chapter;
import java.util.List;

public interface IChapterDOA {
    boolean addChapter(int bookId, String chapterName);
    boolean updateChapter(int chapterId, String newName);
    boolean deleteChapter(int chapterId);
    Chapter retrieveChapter(int chapterId);
    List<Chapter> retrieveChaptersByBook(int bookId);
    boolean importChapterFromFile(int bookId, String chapterName, String filePath);
    List<Integer> getChapterIdsByBook(int bookId);
    
    int searchChapter(String chapterName);
    Chapter getChapterById(int chapterId);
	String getChapterNameById(int chapterId);

}