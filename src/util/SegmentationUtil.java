package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Enhanced utility class for Arabic morphological segmentation
 * Uses AlKhalil Stemmer API with fallback to rule-based segmentation
 */
public class SegmentationUtil {
    private static SegmentationUtil instance;
    private final ExternalNLPGateway gateway;
    private final CacheRepository cache;
    
    // Common Arabic prefixes
    private static final List<String> ARABIC_PREFIXES = Arrays.asList(
        "ب", "ل", "ك", "ف", "و", "ت", "ن", "أ", "ال", "بال", "كال", "فال", "وال", 
        "تال", "نال", "أل", "بل", "كل", "فل", "ول", "تل", "نل", "أل", "ب", "ل", 
        "ك", "ف", "و", "ت", "ن", "أ", "ال", "بال", "كال", "فال", "وال", "تال", "نال"
    );
    
    // Common Arabic suffixes
    private static final List<String> ARABIC_SUFFIXES = Arrays.asList(
        "ه", "ها", "هم", "هن", "ك", "كم", "كن", "ي", "نا", "هما", "كما", "يما",
        "ون", "ين", "ان", "ات", "ة", "ت", "ن", "ا", "و", "ي", "ن", "م", "ك", "ه"
    );
    
    private SegmentationUtil() {
        this.gateway = AlKhalilGateway.getInstance();
        this.cache = CacheRepository.getInstance();
        System.out.println("SegmentationUtil instance created with AlKhalil integration");
    }
    
    public static SegmentationUtil getInstance() {
        if (instance == null) {
            instance = new SegmentationUtil();
        }
        return instance;
    }
    
    /**
     * Segments an Arabic token into prefix, stem, and suffix
     * Uses AlKhalil Stemmer API with fallback to rule-based segmentation
     * @param token The Arabic token to segment
     * @return An array of three strings: [prefix, stem, suffix]
     */
    public String[] segmentToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new String[]{"", "", ""};
        }
        
        // Normalize token to improve prefix/stem detection
        String trimmedToken = normalizeToken(token.trim());
        String cacheKey = "segmentation:" + trimmedToken;
        
        // Check cache first
        String[] cachedResult = cache.get(cacheKey, String[].class);
        if (cachedResult != null) {
            System.out.println("Using cached segmentation for: " + trimmedToken);
            return cachedResult;
        }
        
        String[] result;
        
        // Try AlKhalil Stemmer API first
        if (gateway.isServiceAvailable()) {
            try {
                Map<String, String> segments = gateway.segmentToken(trimmedToken);
                if (segments != null && !segments.isEmpty() && "alkhalil".equals(segments.get("__source"))) {
                    String prefix = segments.getOrDefault("prefix", "");
                    String stem = segments.getOrDefault("stem", trimmedToken);
                    String suffix = segments.getOrDefault("suffix", "");
                    
                    // If AlKhalil returned no prefix but the token starts with a known
                    // clitic/definite article, prefer the rule-based segmentation.
                    if ((prefix == null || prefix.isEmpty()) && probableHasPrefix(trimmedToken)) {
                        System.out.println("AlKhalil returned empty prefix but token looks prefixed; falling back to rule-based segmentation for: " + trimmedToken);
                    } else {
                        // Validate that prefix + stem + suffix equals original token
                        if (validateSegmentation(trimmedToken, prefix, stem, suffix)) {
                            result = new String[]{prefix, stem, suffix};
                            cache.put(cacheKey, result);
                            System.out.println("AlKhalil segmented: '" + trimmedToken + "' -> prefix: '" + prefix + "', stem: '" + stem + "', suffix: '" + suffix + "'");
                            return result;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("AlKhalil Stemmer API error: " + e.getMessage());
            }
        }
        
        // Fallback to rule-based segmentation
        result = ruleBasedSegmentation(trimmedToken);
        cache.put(cacheKey, result);
        
        System.out.println("Rule-based segmented: '" + trimmedToken + "' -> prefix: '" + result[0] + "', stem: '" + result[1] + "', suffix: '" + result[2] + "'");
        return result;
    }

    /**
     * Normalize Arabic token for more robust segmentation:
     * - remove tatweel
     * - remove diacritics (harakat)
     * - normalize Alef variants to bare Alef
     * - normalize Alef Maksura to Yaa
     * - remove zero-width characters
     */
    private String normalizeToken(String token) {
        if (token == null) return "";
        String s = token;
        // Remove tatweel
        s = s.replaceAll("\u0640", "");
        // Remove Arabic diacritics (harakat)
        s = s.replaceAll("[\u0610-\u061A\u064B-\u065F\u06D6-\u06ED]", "");
        // Remove zero-width and control chars
        s = s.replaceAll("[\u200C\u200D\uFEFF]", "");
        // Normalize Alef variations to bare Alef
        s = s.replaceAll("[\u0622\u0623\u0625]", "\u0627");
        // Normalize Alef Maksura (0629 or 0649) to Ya (064A)
        s = s.replaceAll("\u0649", "\u064A");
        // Normalize ligature lam+alef forms to separate letters (lam + alef)
        s = s.replaceAll("\uFEFB|\uFEFC|\uFEF7|\uFEF8", "\u0644\u0627");
        // Trim any leftover whitespace
        s = s.trim();
        return s;
    }
    
    /**
     * Validate that segmentation components reconstruct the original token
     */
    private boolean validateSegmentation(String original, String prefix, String stem, String suffix) {
        String reconstructed = prefix + stem + suffix;
        return reconstructed.equals(original) || reconstructed.length() >= original.length() * 0.8;
    }
    
    /**
     * Rule-based segmentation fallback
     */
    private String[] ruleBasedSegmentation(String token) {
        String prefix = "";
        String stem = "";
        String suffix = "";
        
        // Try to identify prefix
        prefix = extractPrefix(token);
        String remaining = token.substring(prefix.length());
        
        // Try to identify suffix
        String extractedSuffix = extractSuffix(remaining);
        suffix = extractedSuffix;
        stem = remaining.substring(0, remaining.length() - suffix.length());
        
        // If stem is empty after removing prefix and suffix, use the whole token as stem
        if (stem.isEmpty() && prefix.isEmpty() && suffix.isEmpty()) {
            stem = token;
        }
        
        return new String[]{prefix, stem, suffix};
    }

    /**
     * Heuristic: does the token start with a known prefix (clitic/definite article)?
     */
    private boolean probableHasPrefix(String token) {
        if (token == null || token.isEmpty()) return false;
        List<String> sortedPrefixes = new ArrayList<>(ARABIC_PREFIXES);
        sortedPrefixes.sort((a, b) -> Integer.compare(b.length(), a.length()));
        for (String p : sortedPrefixes) {
            if (!p.isEmpty() && token.startsWith(p)) return true;
        }
        // additional quick checks: starts with Lam-Alef or Alif+Lam
        if (token.startsWith("ال")) return true;
        if (token.length() > 2 && "وفبكلس".indexOf(token.charAt(0)) >= 0 && token.startsWith("ال", 1)) return true;
        return false;
    }
    
    /**
     * Extracts the prefix from an Arabic token
     */
    private String extractPrefix(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        
        // Sort prefixes by length (longest first) to match longer prefixes first
        List<String> sortedPrefixes = new ArrayList<>(ARABIC_PREFIXES);
        sortedPrefixes.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        for (String prefix : sortedPrefixes) {
            if (token.startsWith(prefix)) {
                return prefix;
            }
        }
        
        return "";
    }
    
    /**
     * Extracts the suffix from an Arabic token
     */
    private String extractSuffix(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        
        // Sort suffixes by length (longest first) to match longer suffixes first
        List<String> sortedSuffixes = new ArrayList<>(ARABIC_SUFFIXES);
        sortedSuffixes.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        for (String suffix : sortedSuffixes) {
            if (token.endsWith(suffix) && token.length() > suffix.length()) {
                return suffix;
            }
        }
        
        return "";
    }
    
    /**
     * Segments multiple tokens
     */
    public List<String[]> segmentTokens(List<String> tokens) {
        List<String[]> segments = new ArrayList<>();
        for (String token : tokens) {
            segments.add(segmentToken(token));
        }
        return segments;
    }
}

