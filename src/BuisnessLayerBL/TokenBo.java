package BuisnessLayerBL;

import DataAcessLayerDAL.IDataFacade;
import ModelDTO.TokenData;
import ModelDTO.Sentence;
import java.util.ArrayList;
import java.util.List;

public class TokenBo implements ITokenBo {
    private final IDataFacade df;
    
    public TokenBo(IDataFacade df) {
        this.df = df;
    }
    
    @Override
    public boolean addTokensForSentence(String chapterName, int sentenceNumber, List<TokenData> tokens) {
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
        boolean result = df.addTokensForSentence(sentence.getSentenceId(), tokens);
        System.out.println("Add tokens for sentence_id: " + sentence.getSentenceId() + " - " + (result ? "Success" : "Failed"));
        return result;
    }
    
    @Override
    public boolean updateTokensForSentence(String chapterName, int sentenceNumber, List<TokenData> tokens) {
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
        boolean result = df.updateTokensForSentence(sentence.getSentenceId(), tokens);
        System.out.println("Update tokens for sentence_id: " + sentence.getSentenceId() + " - " + (result ? "Success" : "Failed"));
        return result;
    }
    
    @Override
    public List<TokenData> retrieveTokensForSentence(String chapterName, int sentenceNumber) {
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
        List<TokenData> tokens = df.retrieveTokensForSentence(sentence.getSentenceId());
        System.out.println("Retrieved " + tokens.size() + " tokens for sentence_id: " + sentence.getSentenceId());
        return tokens;
    }
    
    @Override
    public List<TokenData> retrieveTokensBySentence(int sentenceId) {
        return df.retrieveTokensBySentence(sentenceId);
    }
    
    @Override
    public List<TokenData> getTokensByRoot(String rootText) {
        return df.getTokensByRoot(rootText);
    }

    @Override
    public List<String> getAllDistinctTokens() {
        return df.getAllDistinctTokens();
    }

    @Override
    public List<String> getAllDistinctLemmas() {
        return df.getAllDistinctLemmas();
    }

    @Override
    public List<TokenData> getTokensByLemma(String lemma) {
        return df.getTokensByLemma(lemma);
    }

    @Override
    public List<String> getAllDistinctSegments() {
        return df.getAllDistinctSegments();
    }

    @Override
    public List<TokenData> getTokensBySegment(String segment) {
        return df.getTokensBySegment(segment);
    }
    
    @Override
    public List<java.util.Map<String, String>> getSentencesByToken(String tokenText) {
        if (tokenText == null || tokenText.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return df.findSentencesByToken(tokenText);
    }

    @Override
    public List<String> getLemmasByRoot(String rootText) {
        return df.getLemmasByRoot(rootText);
    }

    @Override
    public java.util.Map<String, Integer> getTokenFrequencyInChapter(int chapterId) {
        return df.getTokenFrequencyInChapter(chapterId);
    }

    @Override
    public java.util.Map<String, Integer> getLemmaFrequencyInChapter(int chapterId) {
        return df.getLemmaFrequencyInChapter(chapterId);
    }

    @Override
    public java.util.Map<String, Integer> getTokenFrequencyInBook(int bookId) {
        return df.getTokenFrequencyInBook(bookId);
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getTokenFrequencyBreakdownByBook(int bookId) {
        return df.getTokenFrequencyBreakdownByBook(bookId);
    }

    @Override
    public java.util.Map<String, Integer> getLemmaFrequencyInBook(int bookId) {
        return df.getLemmaFrequencyInBook(bookId);
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getLemmaFrequencyBreakdownByBook(int bookId) {
        return df.getLemmaFrequencyBreakdownByBook(bookId);
    }
}