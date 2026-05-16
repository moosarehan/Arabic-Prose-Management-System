 package ModelDTO;

public class TokenData {
    private int tokenId;
    private int sentenceId;
    private String tokenText;
    private String lemma;
    private int position;
    
    public TokenData(int tokenId, int sentenceId, String tokenText, String lemma, int position) {
        this.tokenId = tokenId;
        this.sentenceId = sentenceId;
        this.tokenText = tokenText;
        this.lemma = lemma;
        this.position = position;
    }
    
    public TokenData(int sentenceId, String tokenText, String lemma, int position) {
        this(0, sentenceId, tokenText, lemma, position);
    }

    public int getTokenId() { 
        return tokenId; 
    }
    
    public int getSentenceId() { 
        return sentenceId; 
    }
    
    public String getTokenText() { 
        return tokenText; 
    }
    
    
    public int getPosition() { 
        return position; 
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public String getLemma() {
        // Ensure we never return null
        return lemma != null ? lemma : tokenText;
    }

    public void setLemma(String lemma) {
        // Store the lemma, but don't allow null
        this.lemma = lemma != null ? lemma : (tokenText != null ? tokenText : "");
    }
}