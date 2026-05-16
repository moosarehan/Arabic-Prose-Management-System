package ModelDTO;

/**
 * DTO for token segmentation data
 * Represents the morphological components of an Arabic token
 */
public class TokenSegmentation {
    private int segmentationId;
    private int tokenId;
    private String prefix;
    private String stem;
    private String suffix;
    
    public TokenSegmentation(int segmentationId, int tokenId, String prefix, String stem, String suffix) {
        this.segmentationId = segmentationId;
        this.tokenId = tokenId;
        this.prefix = prefix != null ? prefix : "";
        this.stem = stem != null ? stem : "";
        this.suffix = suffix != null ? suffix : "";
    }
    
    public TokenSegmentation(int tokenId, String prefix, String stem, String suffix) {
        this(0, tokenId, prefix, stem, suffix);
    }
    
    // Getters
    public int getSegmentationId() {
        return segmentationId;
    }
    
    public int getTokenId() {
        return tokenId;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getStem() {
        return stem;
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    // Setters
    public void setSegmentationId(int segmentationId) {
        this.segmentationId = segmentationId;
    }
    
    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }
    
    public void setStem(String stem) {
        this.stem = stem != null ? stem : "";
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix != null ? suffix : "";
    }
    
    @Override
    public String toString() {
        return String.format("TokenSegmentation{tokenId=%d, prefix='%s', stem='%s', suffix='%s'}", 
                tokenId, prefix, stem, suffix);
    }
}






