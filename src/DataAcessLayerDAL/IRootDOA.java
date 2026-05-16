package DataAcessLayerDAL;

import ModelDTO.Root;
import java.util.List;

public interface IRootDOA {
    /**
     * Add a root for a token
     */
    boolean addRoot(Root root);
    
    /**
     * Add multiple roots for tokens
     */
    boolean addRootsForToken(int tokenId, List<Root> roots);
    
    /**
     * Update a root
     */
    boolean updateRoot(Root root);
    
    /**
     * Retrieve all roots for a specific token
     */
    List<Root> retrieveRootsByTokenId(int tokenId);
    
    /**
     * Retrieve all roots for tokens in a sentence
     */
    List<Root> retrieveRootsBySentenceId(int sentenceId);
    
    /**
     * Delete all roots for a token
     */
    boolean deleteRootsByTokenId(int tokenId);
    
    /**
     * Delete a specific root
     */
    boolean deleteRoot(int rootId);
    
    /**
     * Retrieve all distinct root strings available in the system.
     */
    List<String> getAllRoots();
    
    // Frequency Analysis
    java.util.Map<String, Integer> getRootFrequencyInChapter(int chapterId);
    java.util.Map<String, Integer> getRootFrequencyInBook(int bookId);
    java.util.Map<String, java.util.Map<String, Integer>> getRootFrequencyBreakdownByBook(int bookId);
}






