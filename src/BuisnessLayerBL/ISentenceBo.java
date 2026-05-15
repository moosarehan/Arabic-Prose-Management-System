

package BuisnessLayerBL;
import java.util.List;
import java.util.Map;
import ModelDTO.Sentence;

public interface ISentenceBo {
    boolean addSentence(String chapterName, String text, String textDiacritized, String translation, String notes);
    boolean updateSentence(String chapterName, int sentenceNumber, String newText, String newDiacritized, String newTranslation, String newNotes);
    boolean deleteSentence(String chapterName, int sentenceNumber);
    Sentence retrieveSentence(String chapterName, int sentenceNumber);
    List<Sentence> retrieveSentencesByChapter(String chapterName);
    List<Sentence> retrieveSentencesByBook(String bookName);
    List<Sentence> retrieveAllSentences();
    int getLastInsertedSentenceId();
    Sentence retrieveSentenceById(int sentenceId);
    
    /**
     * Search sentences whose text contains the given phrase using
     * an exact string LIKE '%phrase%' match at the DAO layer.
     */
    List<Sentence> searchSentencesByExactString(String phrase);
    
    /**
     * Regex-based sentence search that returns sentence metadata as a
     * list of maps for UI consumption.
     */
    List<Map<String, String>> searchSentencesByRegex(String pattern);

    /**
     * Performs a trigram-based similarity search.
     * @param query The input sentence/phrase.
     * @param thresholdPercentage The similarity threshold (0-100).
     * @return List of matching sentences with similarity scores.
     */
    List<ModelDTO.SentenceSearchResult> performSimilaritySearch(String query, double thresholdPercentage);

 
}
