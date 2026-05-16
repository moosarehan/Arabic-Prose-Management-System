package ModelDTO;

/**
 * DTO for Arabic root extraction data
 * Represents a possible root for an Arabic token
 */
public class Root {
    private int rootId;
    private int tokenId;
    private String rootText;
    private String pattern;
    private double confidenceScore;
    
    public Root(int rootId, int tokenId, String rootText, String pattern, double confidenceScore) {
        this.rootId = rootId;
        this.tokenId = tokenId;
        this.rootText = rootText != null ? rootText : "";
        this.pattern = pattern != null ? pattern : "";
        this.confidenceScore = confidenceScore;
    }
    
    public Root(int tokenId, String rootText, String pattern, double confidenceScore) {
        this(0, tokenId, rootText, pattern, confidenceScore);
    }
    
    // Getters
    public int getRootId() {
        return rootId;
    }
    
    public int getTokenId() {
        return tokenId;
    }
    
    public String getRootText() {
        return rootText;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    // Setters
    public void setRootId(int rootId) {
        this.rootId = rootId;
    }
    
    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }
    
    public void setRootText(String rootText) {
        this.rootText = rootText != null ? rootText : "";
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern != null ? pattern : "";
    }
    
    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    @Override
    public String toString() {
        return String.format("Root{rootId=%d, tokenId=%d, rootText='%s', pattern='%s', confidenceScore=%.2f}", 
                rootId, tokenId, rootText, pattern, confidenceScore);
    }
}






