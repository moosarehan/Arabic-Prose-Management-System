package DataAcessLayerDAL;

import ModelDTO.TokenSegmentation;
import java.util.List;

public interface ISegmentationDOA {
    /**
     * Add segmentation data for a token
     */
    boolean addSegmentation(int tokenId, TokenSegmentation segmentation);
    
    /**
     * Add segmentations for multiple tokens
     */
    boolean addSegmentationsForTokens(List<TokenSegmentation> segmentations);
    
    /**
     * Update segmentation for a token
     */
    boolean updateSegmentation(int tokenId, TokenSegmentation segmentation);
    
    /**
     * Retrieve segmentation for a specific token
     */
    TokenSegmentation retrieveSegmentationByTokenId(int tokenId);
    
    /**
     * Retrieve all segmentations for tokens in a sentence
     */
    List<TokenSegmentation> retrieveSegmentationsBySentenceId(int sentenceId);
    
    /**
     * Delete segmentation for a token
     */
    boolean deleteSegmentation(int tokenId);
}






