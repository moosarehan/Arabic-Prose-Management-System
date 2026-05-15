
package BuisnessLayerBL;

import java.util.List;

import ModelDTO.Chapter;
import ModelDTO.Sentence;

public interface IChapterBo {
    boolean addChapter(String bookName, String chapterName);
    boolean updateChapter(String bookName, int chapterId, String newChapterName);
    boolean deleteChapter(String bookName, int chapterId);
    Chapter retrieveChapter(String bookName, int chapterId);
    List<Chapter> retrieveChaptersByBook(String bookName);
    List<Sentence> retrieveSentencesByChapter(String chapterName);
    boolean importChapterFromFile(String bookName, String chapterName, String filePath);
	String getChapterNameById(int chapterId);
    Chapter retrieveChapter(int chapterId);

}