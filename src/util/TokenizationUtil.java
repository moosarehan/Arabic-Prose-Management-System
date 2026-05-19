package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enhanced utility class for Arabic tokenization
 * Handles Arabic-specific word boundaries and punctuation
 */
public class TokenizationUtil {
    private static TokenizationUtil instance;
    
    // Arabic punctuation marks
    private static final String ARABIC_PUNCTUATION = "،؛؟";
    // Arabic diacritics (should be preserved but not used as word boundaries)
    private static final Pattern ARABIC_DIACRITICS = Pattern.compile("[\\u064B-\\u065F\\u0670]");
    
    private TokenizationUtil() {
        System.out.println("TokenizationUtil instance created");
    }

    public static TokenizationUtil getInstance() {
        if (instance == null) {
            instance = new TokenizationUtil();
        }
        return instance;
    }

    /**
     * Tokenize Arabic text with proper word boundary handling
     * @param text The Arabic text to tokenize
     * @return List of tokens
     */
    public List<String> tokenizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.err.println("Cannot tokenize null or empty text");
            return new ArrayList<>();
        }
        
        // Normalize Arabic text
        String normalized = normalizeArabicText(text);
        
        // Split on whitespace and Arabic punctuation
        // Pattern: whitespace OR Arabic punctuation (with optional spaces)
        String[] tokens = normalized.split("\\s+|[" + ARABIC_PUNCTUATION + "]");
        
        List<String> tokenList = new ArrayList<>();
        for (String token : tokens) {
            // Clean token: remove leading/trailing punctuation but preserve Arabic characters
            String cleaned = cleanToken(token);
            if (!cleaned.isEmpty() && containsArabicCharacters(cleaned)) {
                tokenList.add(cleaned);
            }
        }
        
        System.out.println("Tokenized text: " + text + " -> " + tokenList);
        return tokenList;
    }
    
    /**
     * Normalize Arabic text for tokenization
     */
    private String normalizeArabicText(String text) {
        // Remove zero-width characters
        text = text.replaceAll("\\u200B", ""); // Zero-width space
        text = text.replaceAll("\\u200C", ""); // Zero-width non-joiner
        text = text.replaceAll("\\u200D", ""); // Zero-width joiner
        text = text.replaceAll("\\uFEFF", ""); // Zero-width no-break space
        
        // Normalize Arabic characters (optional - can be enhanced)
        // For now, just trim
        return text.trim();
    }
    
    /**
     * Clean a token by removing unwanted characters
     */
    private String cleanToken(String token) {
        // Remove common punctuation but keep Arabic characters and diacritics
        token = token.replaceAll("[^\\u0600-\\u06FF\\u064B-\\u065F\\u0670\\u200C\\u200D]", "");
        return token.trim();
    }
    
    /**
     * Check if string contains Arabic characters
     */
    private boolean containsArabicCharacters(String str) {
        return str.matches(".*[\\u0600-\\u06FF].*");
    }
    
    /**
     * Tokenize text preserving punctuation as separate tokens
     */
    public List<String> tokenizeWithPunctuation(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else if (ARABIC_PUNCTUATION.indexOf(c) >= 0) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(String.valueOf(c));
            } else {
                currentToken.append(c);
            }
        }
        
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        
        return tokens;
    }
}





















 
