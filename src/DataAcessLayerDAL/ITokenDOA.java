
 package DataAcessLayerDAL;
import java.util.List;
import ModelDTO.TokenData;

public interface ITokenDOA {
    boolean addTokensForSentence(int sentenceId, List<TokenData> tokens);
    boolean updateTokensForSentence(int sentenceId, List<TokenData> tokens);
    List<TokenData> retrieveTokensForSentence(int sentenceId);
    List<TokenData> retrieveTokensBySentence(int sentenceId);
    List<TokenData> getTokensByRoot(String rootText);
    List<String> getAllDistinctTokens();
    List<String> getAllDistinctLemmas();
    List<TokenData> getTokensByLemma(String lemma);
    List<String> getAllDistinctSegments();
    List<TokenData> getTokensBySegment(String segment);
    public java.util.List<java.util.Map<String, String>> findSentencesByToken(String tokenText);
    public java.util.List<String> getLemmasByRoot(String rootText);
    
    // Frequency Analysis
    java.util.Map<String, Integer> getTokenFrequencyInChapter(int chapterId);
    java.util.Map<String, Integer> getLemmaFrequencyInChapter(int chapterId);
    
    java.util.Map<String, Integer> getTokenFrequencyInBook(int bookId);
    java.util.Map<String, java.util.Map<String, Integer>> getTokenFrequencyBreakdownByBook(int bookId);
    
    java.util.Map<String, Integer> getLemmaFrequencyInBook(int bookId);
    java.util.Map<String, java.util.Map<String, Integer>> getLemmaFrequencyBreakdownByBook(int bookId);
}