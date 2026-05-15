package BuisnessLayerBL;

import ModelDTO.TokenSegmentation;
import java.util.List;

public interface ISegmentationBo {
    /**
     * Segment a token and save the segmentation
     */
    boolean segmentToken(int tokenId, String tokenText);
    
    /**
     * Segment all tokens in a sentence
     */
    boolean segmentTokensForSentence(String chapterName, int sentenceNumber);
    
    /**
     * Retrieve segmentation for a specific token
     */
    TokenSegmentation retrieveSegmentationByTokenId(int tokenId);
    
    /**
     * Retrieve all segmentations for tokens in a sentence
     */
    List<TokenSegmentation> retrieveSegmentationsBySentence(String chapterName, int sentenceNumber);
    
    /**
     * Update segmentation for a token
     */
    boolean updateSegmentation(int tokenId, String prefix, String stem, String suffix);
    
    /**
     * Delete segmentation for a token
     */
    boolean deleteSegmentation(int tokenId);
}






