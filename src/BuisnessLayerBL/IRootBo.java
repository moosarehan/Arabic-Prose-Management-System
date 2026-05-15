package BuisnessLayerBL;

import ModelDTO.Root;
// import ModelDTO.TokenData;
import java.util.List;

public interface IRootBo {
    /**
     * Extract and save roots for a token
     */
    boolean extractRootsForToken(int tokenId, String tokenText);
    
    /**
     * Extract and save roots for all tokens in a sentence
     */
    boolean extractRootsForSentence(String chapterName, int sentenceNumber);
    
    /**
     * Retrieve all roots for a specific token
     */
    List<Root> retrieveRootsByTokenId(int tokenId);
    
    /**
     * Retrieve all roots for tokens in a sentence
     */
    List<Root> retrieveRootsBySentence(String chapterName, int sentenceNumber);
    
    /**
     * Delete all roots for a token
     */
    boolean deleteRootsByTokenId(int tokenId);
    
    /**
     * Update root information
     */
    boolean updateRoot(Root root);
    
    /**
     * Retrieve all distinct root strings for browsing.
     */
    List<String> getAllRoots();
    
    // Frequency Analysis
    java.util.Map<String, Integer> getRootFrequencyInChapter(int chapterId);
    java.util.Map<String, Integer> getRootFrequencyInBook(int bookId);
    java.util.Map<String, java.util.Map<String, Integer>> getRootFrequencyBreakdownByBook(int bookId);
}






