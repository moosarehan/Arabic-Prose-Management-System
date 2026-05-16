
 package DataAcessLayerDAL;

import ModelDTO.Sentence;
import java.util.List;
import ModelDTO.TokenData;

public interface ISentenceDOA {
    boolean addSentence(int chapterID, String text, String textDiacritized, String translation, String notes);
    boolean updateSentence(int chapterID, int sentenceNumber, String newText, String newDiacritized, String newTranslation, String newNotes);
    boolean deleteSentence(int chapterID, int sentenceNumber);
    Sentence retrieveSentence(int chapterID, int sentenceNumber);
    List<Sentence> retrieveSentencesByChapter(int chapterID);
    List<Sentence> retrieveSentencesByBook(int bookId);
    
    List<Sentence> retrieveAllSentences();
    int getLastInsertedSentenceId();
    Sentence retrieveSentenceById(int sentenceId);
    
    /**
     * Search sentences whose plain text contains the given phrase
     * using a LIKE '%phrase%' predicate on the sentences table only.
     */
    List<Sentence> searchSentencesByExactString(String phrase);

}