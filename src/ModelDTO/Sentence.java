
 package ModelDTO;

import ModelDTO.TokenData;
import java.util.ArrayList;
import java.util.List;

public class Sentence {
    private int sentenceId;
    private int chapterId;
    private int sentenceNumber;
    private String text;
    private String textDiacritized;
    private String translation;
    private String notes;
    private List<TokenData> tokens = new ArrayList<>();
    
    public Sentence() {}
    
    public Sentence(int sentenceId, int chapterId, int sentenceNumber,
                    String text, String textDiacritized, String translation, String notes) {
        this.sentenceId = sentenceId;
        this.chapterId = chapterId;
        this.sentenceNumber = sentenceNumber;
        this.text = text;
        this.textDiacritized = textDiacritized;
        this.translation = translation;
        this.notes = notes;
        this.tokens = new ArrayList<>();
    }
    
    public int getSentenceId() { return sentenceId; }
    public void setSentenceId(int sentenceId) { this.sentenceId = sentenceId; }
    
    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }
    
    public int getSentenceNumber() { return sentenceNumber; }
    public void setSentenceNumber(int sentenceNumber) { this.sentenceNumber = sentenceNumber; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getTextDiacritized() { return textDiacritized; }
    public void setTextDiacritized(String textDiacritized) { this.textDiacritized = textDiacritized; }
    
    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<TokenData> getTokens() { return tokens; }
    public void setTokens(List<TokenData> tokens) { this.tokens = tokens; }
    
    @Override
    public String toString() {
        return "Sentence{" +
               "sentenceId=" + sentenceId +
               ", chapterId=" + chapterId +
               ", sentenceNumber=" + sentenceNumber +
               ", text='" + text + '\'' +
               ", textDiacritized='" + textDiacritized + '\'' +
               ", translation='" + translation + '\'' +
               ", notes='" + notes + '\'' +
               ", tokens=" + tokens +
               '}';
    }
}