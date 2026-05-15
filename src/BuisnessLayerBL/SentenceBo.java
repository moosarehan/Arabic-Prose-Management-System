package BuisnessLayerBL;

import DataAcessLayerDAL.IDataFacade;
import ModelDTO.TokenData;
import ModelDTO.Sentence;
import util.TokenizationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SentenceBo implements ISentenceBo {
    private final IDataFacade df;
    private final ITokenBo tokenBo;
    private final ILemmatizationBo lemmatizationBo;

    public SentenceBo(IDataFacade df, ITokenBo tokenBo, ILemmatizationBo lemmatizationBo) {
        this.df = df;
        this.tokenBo = tokenBo;
        this.lemmatizationBo = lemmatizationBo;
    }

    @Override
    public boolean addSentence(String chapterName, String text, String textDiacritized, String translation, String notes) {
        System.out.println("Attempting to add sentence: chapterName=" + chapterName + ", text=" + text);
        
        // Validate inputs
        if (chapterName == null || chapterName.trim().isEmpty()) {
            System.err.println("❌ Error: Chapter name is null or empty");
            return false;
        }
        if (text == null || text.trim().isEmpty()) {
            System.err.println("❌ Error: Sentence text is null or empty");
            return false;
        }

        // Find chapter
        int chapterID = df.searchChapter(chapterName);
        if (chapterID == -1) {
            System.err.println("❌ Error: Chapter not found: " + chapterName);
            return false;
        }
        System.out.println("✅ Found chapter_id: " + chapterID);

        // Add sentence to database
        boolean sentenceAdded = df.addSentence(chapterID, text, textDiacritized, translation, notes);
        if (!sentenceAdded) {
            System.err.println("❌ Error: Failed to add sentence to database");
            return false;
        }
        System.out.println("✅ Sentence added to database");

        // Get last inserted sentence ID
        int sentenceId = df.getLastInsertedSentenceId();
        if (sentenceId == -1) {
            System.err.println("❌ Error: Failed to retrieve last inserted sentence ID");
            return false;
        }
        System.out.println("✅ Retrieved sentence_id: " + sentenceId);

        // Retrieve the new sentence
        Sentence newSentence = df.retrieveSentenceById(sentenceId);
        if (newSentence == null) {
            System.err.println("❌ Error: Failed to retrieve sentence with ID: " + sentenceId);
            return false;
        }
        System.out.println("✅ Retrieved sentence: number=" + newSentence.getSentenceNumber());

        // Tokenize the text
        List<String> tokens;
        try {
            tokens = TokenizationUtil.getInstance().tokenizeText(text);
            System.out.println("✅ Tokenized text: " + text + " -> " + tokens);
        } catch (Exception e) {
            System.err.println("❌ Error tokenizing text: " + text + ": " + e.getMessage());
            return false;
        }

        // Prepare token data
        List<TokenData> tokenDataList = new ArrayList<>();
        for (int pos = 0; pos < tokens.size(); pos++) {
            String tokenText = tokens.get(pos);
            String lemma = lemmatizationBo != null ? lemmatizationBo.lemmatizeToken(tokenText) : tokenText;
            tokenDataList.add(new TokenData(0, sentenceId, tokenText, lemma, pos + 1));
        }
        if (tokenDataList.isEmpty()) {
            System.out.println("⚠ Warning: No tokens generated for text: " + text);
        }

        // Add tokens
        boolean tokensAdded = tokenBo.addTokensForSentence(chapterName, newSentence.getSentenceNumber(), tokenDataList);
        if (!tokensAdded) {
            System.err.println("❌ Error: Failed to add tokens for sentence_id: " + sentenceId);
            return false;
        }

        System.out.println("✅ Successfully added sentence_id: " + sentenceId + " with " + tokenDataList.size() + " tokens");
        return true;
    }

    @Override
    public boolean updateSentence(String chapterName, int sentenceNumber, String newText, String newDiacritized, String newTranslation, String newNotes) {
        if (chapterName == null || chapterName.trim().isEmpty()) {
            System.err.println("Chapter name cannot be null or empty");
            return false;
        }
        if (newText == null || newText.trim().isEmpty()) {
            System.err.println("New text cannot be null or empty");
            return false;
        }
        int chapterID = df.searchChapter(chapterName);
        if (chapterID == -1) {
            System.err.println("Chapter not found: " + chapterName);
            return false;
        }
        Sentence existing = df.retrieveSentence(chapterID, sentenceNumber);
        if (existing == null) {
            System.err.println("Sentence not found: chapter=" + chapterName + ", number=" + sentenceNumber);
            return false;
        }
        boolean updated = df.updateSentence(chapterID, sentenceNumber, newText, newDiacritized, newTranslation, newNotes);
        if (updated) {
            List<String> tokens;
            try {
                tokens = TokenizationUtil.getInstance().tokenizeText(newText);
            } catch (Exception e) {
                System.err.println("Error tokenizing new text: " + newText + ": " + e.getMessage());
                return false;
            }
            List<TokenData> tokenDataList = new ArrayList<>();
            for (int pos = 0; pos < tokens.size(); pos++) {
                String tokenText = tokens.get(pos);
                String lemma = lemmatizationBo != null ? lemmatizationBo.lemmatizeToken(tokenText) : tokenText;
                tokenDataList.add(new TokenData(0, existing.getSentenceId(), tokenText, lemma, pos + 1));
            }
            boolean tokensUpdated = tokenBo.updateTokensForSentence(chapterName, sentenceNumber, tokenDataList);
            System.out.println("Updated sentence_id: " + existing.getSentenceId() + ", tokens updated: " + tokensUpdated);
            return tokensUpdated;
        }
        return false;
    }

    @Override
    public boolean deleteSentence(String chapterName, int sentenceNumber) {
        if (chapterName == null || chapterName.trim().isEmpty()) {
            System.err.println("Chapter name cannot be null or empty");
            return false;
        }
        int chapterID = df.searchChapter(chapterName);
        if (chapterID == -1) {
            System.err.println("Chapter not found: " + chapterName);
            return false;
        }
        Sentence existing = df.retrieveSentence(chapterID, sentenceNumber);
        if (existing == null) {
            System.err.println("Sentence not found: chapter=" + chapterName + ", number=" + sentenceNumber);
            return false;
        }
        boolean deleted = df.deleteSentence(chapterID, sentenceNumber);
        if (deleted) {
            System.out.println("Deleted sentence_id: " + existing.getSentenceId());
        }
        return deleted;
    }

    @Override
    public Sentence retrieveSentence(String chapterName, int sentenceNumber) {
        if (chapterName == null || chapterName.trim().isEmpty()) {
            System.err.println("Chapter name cannot be null or empty");
            return null;
        }
        int chapterID = df.searchChapter(chapterName);
        if (chapterID == -1) {
            System.err.println("Chapter not found: " + chapterName);
            return null;
        }
        Sentence sentence = df.retrieveSentence(chapterID, sentenceNumber);
        if (sentence == null) {
            System.err.println("Sentence not found: chapter=" + chapterName + ", number=" + sentenceNumber);
        } else {
            System.out.println("Retrieved sentence_id: " + sentence.getSentenceId());
        }
        return sentence;
    }

    @Override
    public List<Sentence> retrieveSentencesByChapter(String chapterName) {
        if (chapterName == null || chapterName.trim().isEmpty()) {
            System.err.println("Chapter name cannot be null or empty");
            return new ArrayList<>();
        }
        int chapterID = df.searchChapter(chapterName);
        if (chapterID == -1) {
            System.err.println("Chapter not found: " + chapterName);
            return new ArrayList<>();
        }
        List<Sentence> sentences = df.retrieveSentencesByChapter(chapterID);
        System.out.println("Retrieved " + sentences.size() + " sentences for chapter: " + chapterName);
        return sentences;
    }

    @Override
    public List<Sentence> retrieveSentencesByBook(String bookName) {
        if (bookName == null || bookName.trim().isEmpty()) {
            System.err.println("Book name cannot be null or empty");
            return new ArrayList<>();
        }
        int bookID = df.searchBook(bookName);
        if (bookID == -1) {
            System.err.println("Book not found: " + bookName);
            return new ArrayList<>();
        }
        List<Sentence> sentences = df.retrieveSentencesByBook(bookID);
        System.out.println("Retrieved " + sentences.size() + " sentences for book: " + bookName);
        return sentences;
    }

    @Override
    public List<Sentence> retrieveAllSentences() {
        List<Sentence> sentences = df.retrieveAllSentences();
        System.out.println("Retrieved " + sentences.size() + " sentences from all books");
        return sentences;
    }

    @Override
    public int getLastInsertedSentenceId() {
        int id = df.getLastInsertedSentenceId();
        if (id == -1) {
            System.err.println("Failed to retrieve last inserted sentence ID");
        } else {
            System.out.println("Retrieved last inserted sentence_id: " + id);
        }
        return id;
    }
    
    @Override
    public Sentence retrieveSentenceById(int sentenceId) {
        return df.retrieveSentenceById(sentenceId);
    }
    
    @Override
    public List<Sentence> searchSentencesByExactString(String phrase) {
        String normalized = normalizePhrase(phrase);
        if (normalized.isEmpty()) {
            System.err.println("Search phrase cannot be null or empty");
            return new ArrayList<>();
        }
        List<Sentence> sentences = df.searchSentencesByExactString(normalized);
        System.out.println("Exact sentence search for '" + normalized + "' returned " + sentences.size() + " results");
        return sentences;
    }

    private String normalizePhrase(String phrase) {
        if (phrase == null) {
            return "";
        }
        return phrase.trim().replaceAll("\\s+", " ");
    }
    
    @Override
    public List<Map<String, String>> searchSentencesByRegex(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            System.err.println("Regex pattern cannot be null or empty");
            return new ArrayList<>();
        }
        List<Map<String, String>> results = df.searchSentencesByRegex(pattern.trim());
        System.out.println("Regex search for '" + pattern + "' returned " + results.size() + " results");
        return results;
    }

    @Override
    public List<ModelDTO.SentenceSearchResult> performSimilaritySearch(String query, double thresholdPercentage) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Fetch all sentences (could be optimized, but per requirements we fetch all)
        List<Sentence> allSentences = df.retrieveAllSentences();
        
        TrigramSimilarityService similarityService = new TrigramSimilarityService();
        return similarityService.search(query, thresholdPercentage, allSentences);
    }
} 