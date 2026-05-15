package BuisnessLayerBL;

import DataAcessLayerDAL.IDataFacade;
import ModelDTO.Root;
import ModelDTO.TokenData;
import ModelDTO.Sentence;
import util.RootExtractionUtil;
import java.util.ArrayList;
import java.util.List;

public class RootBo implements IRootBo {
    private final IDataFacade df;
    private final RootExtractionUtil rootExtractionUtil;
    
    public RootBo(IDataFacade df) {
        this.df = df;
        this.rootExtractionUtil = RootExtractionUtil.getInstance();
    }
    
    @Override
    public boolean extractRootsForToken(int tokenId, String tokenText) {
        if (tokenText == null || tokenText.trim().isEmpty()) {
            System.err.println("Cannot extract roots for empty token");
            return false;
        }
        
        // Extract roots using AlKhalil API
        List<String> rootStrings = rootExtractionUtil.extractRoots(tokenText);
        
        if (rootStrings.isEmpty()) {
            System.out.println("No roots found for token: " + tokenText);
            return false;
        }
        
        // Convert to Root objects
        List<Root> roots = new ArrayList<>();
        for (String rootString : rootStrings) {
            // Parse format: "root|pattern|confidence"
            String[] parts = rootString.split("\\|");
            String rootText = parts.length > 0 ? parts[0] : rootString;
            String pattern = parts.length > 1 ? parts[1] : "unknown";
            double confidence = parts.length > 2 ? parseDouble(parts[2], 0.5) : 0.5;
            
            Root root = new Root(tokenId, rootText, pattern, confidence);
            roots.add(root);
        }
        
        // Save roots to database
        boolean success = df.addRootsForToken(tokenId, roots);
        System.out.println("Extracted and saved " + roots.size() + " roots for token_id: " + tokenId + " - " + (success ? "Success" : "Failed"));
        return success;
    }
    
    @Override
    public boolean extractRootsForSentence(String chapterName, int sentenceNumber) {
        int chapterID = df.searchChapter(chapterName);
        if (chapterID == -1) {
            System.err.println("Chapter not found: " + chapterName);
            return false;
        }
        
        Sentence sentence = df.retrieveSentence(chapterID, sentenceNumber);
        if (sentence == null) {
            System.err.println("Sentence not found: chapter=" + chapterName + ", number=" + sentenceNumber);
            return false;
        }
        
        // Retrieve all tokens for the sentence
        List<TokenData> tokens = df.retrieveTokensBySentence(sentence.getSentenceId());
        if (tokens == null || tokens.isEmpty()) {
            System.err.println("No tokens found for sentence_id: " + sentence.getSentenceId());
            return false;
        }
        
        // Extract roots for each token
        int successCount = 0;
        for (TokenData token : tokens) {
            boolean success = extractRootsForToken(token.getTokenId(), token.getTokenText());
            if (success) {
                successCount++;
            }
        }
        
        System.out.println("Extracted roots for " + successCount + " out of " + tokens.size() + " tokens in sentence_id: " + sentence.getSentenceId());
        return successCount > 0;
    }
    
    @Override
    public List<Root> retrieveRootsByTokenId(int tokenId) {
        return df.retrieveRootsByTokenId(tokenId);
    }
    
    @Override
    public List<Root> retrieveRootsBySentence(String chapterName, int sentenceNumber) {
        int chapterID = df.searchChapter(chapterName);
        if (chapterID == -1) {
            System.err.println("Chapter not found: " + chapterName);
            return new ArrayList<>();
        }
        
        Sentence sentence = df.retrieveSentence(chapterID, sentenceNumber);
        if (sentence == null) {
            System.err.println("Sentence not found: chapter=" + chapterName + ", number=" + sentenceNumber);
            return new ArrayList<>();
        }
        
        return df.retrieveRootsBySentenceId(sentence.getSentenceId());
    }
    
    @Override
    public boolean deleteRootsByTokenId(int tokenId) {
        return df.deleteRootsByTokenId(tokenId);
    }
    
    @Override
    public boolean updateRoot(Root root) {
        return df.updateRoot(root);
    }
    
    @Override
    public List<String> getAllRoots() {
        return df.getAllRoots();
    }
    
    @Override
    public java.util.Map<String, Integer> getRootFrequencyInChapter(int chapterId) {
        return df.getRootFrequencyInChapter(chapterId);
    }

    @Override
    public java.util.Map<String, Integer> getRootFrequencyInBook(int bookId) {
        return df.getRootFrequencyInBook(bookId);
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getRootFrequencyBreakdownByBook(int bookId) {
        return df.getRootFrequencyBreakdownByBook(bookId);
    }

    /**
     * Helper method to parse double with default value
     */
    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}






