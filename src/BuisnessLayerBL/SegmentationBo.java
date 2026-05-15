package BuisnessLayerBL;

import DataAcessLayerDAL.IDataFacade;
import ModelDTO.TokenSegmentation;
import ModelDTO.TokenData;
import ModelDTO.Sentence;
import util.SegmentationUtil;
import util.TokenizationUtil;
import java.util.ArrayList;
import java.util.List;

public class SegmentationBo implements ISegmentationBo {
    private final IDataFacade df;
    private final SegmentationUtil segmentationUtil;
    private final ILemmatizationBo lemmatizationBo = new LemmatizationBo();
    
    public SegmentationBo(IDataFacade df) {
        this.df = df;
        this.segmentationUtil = SegmentationUtil.getInstance();
    }
    
    @Override
    public boolean segmentToken(int tokenId, String tokenText) {
        if (tokenText == null || tokenText.trim().isEmpty()) {
            System.err.println("Cannot segment empty token");
            return false;
        }
        
        // Perform segmentation
        String[] segments = segmentationUtil.segmentToken(tokenText);
        String prefix = segments[0];
        String stem = segments[1];
        String suffix = segments[2];
        
        // Create segmentation object
        TokenSegmentation segmentation = new TokenSegmentation(tokenId, prefix, stem, suffix);
        
        // Check if segmentation already exists
        TokenSegmentation existing = df.retrieveSegmentationByTokenId(tokenId);
        if (existing != null) {
            // Update existing segmentation
            return df.updateSegmentation(tokenId, segmentation);
        } else {
            // Add new segmentation
            return df.addSegmentation(tokenId, segmentation);
        }
    }
    
    @Override
    public boolean segmentTokensForSentence(String chapterName, int sentenceNumber) {
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
        
        // Segment each token
        List<TokenSegmentation> segmentations = new ArrayList<>();
        for (TokenData token : tokens) {
            String[] segments = segmentationUtil.segmentToken(token.getTokenText());
            TokenSegmentation segmentation = new TokenSegmentation(
                token.getTokenId(),
                segments[0], // prefix
                segments[1], // stem
                segments[2]  // suffix
            );
            segmentations.add(segmentation);
        }
        
        // Save all segmentations
        boolean success = df.addSegmentationsForTokens(segmentations);
        System.out.println("Segmented " + segmentations.size() + " tokens for sentence_id: " + sentence.getSentenceId() + " - " + (success ? "Success" : "Failed"));
        return success;
    }
    
    @Override
    public TokenSegmentation retrieveSegmentationByTokenId(int tokenId) {
        return df.retrieveSegmentationByTokenId(tokenId);
    }
    
    @Override
    public List<TokenSegmentation> retrieveSegmentationsBySentence(String chapterName, int sentenceNumber) {
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
        
        return df.retrieveSegmentationsBySentenceId(sentence.getSentenceId());
    }
    
    @Override
    public boolean updateSegmentation(int tokenId, String prefix, String stem, String suffix) {
        TokenSegmentation segmentation = new TokenSegmentation(tokenId, prefix, stem, suffix);
        return df.updateSegmentation(tokenId, segmentation);
    }
    
    @Override
    public boolean deleteSegmentation(int tokenId) {
        return df.deleteSegmentation(tokenId);
    }
    
 // Update in SentenceBo.java
    private List<TokenData> processTokens(String text, int sentenceId) {
        List<TokenData> tokenDataList = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return tokenDataList;
        }

        // Tokenize the text
        List<String> tokens = TokenizationUtil.getInstance().tokenizeText(text);
        System.out.println("Tokenized text: " + text + " -> " + tokens);

        // Process each token
        for (int i = 0; i < tokens.size(); i++) {
            String tokenText = tokens.get(i);
            
            // Get lemma using the lemmatization service
            String lemma = lemmatizationBo.lemmatizeToken(tokenText);
            System.out.println("Token: " + tokenText + " -> Lemma: " + lemma);
            
            // Create token data with position (1-based index)
            TokenData tokenData = new TokenData(0, sentenceId, tokenText, lemma, i + 1);
            tokenDataList.add(tokenData);
        }

        return tokenDataList;
    }
}






