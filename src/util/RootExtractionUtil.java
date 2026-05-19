package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enhanced utility class for Arabic root extraction using AlKhalil Root Extractor
 * Uses AlKhalil API with caching and fallback mechanisms
 * Reference: https://alkhalil.oujda-nlp-team.net/AlKhalil-RootExtractor.php
 */
public class RootExtractionUtil {
    private static RootExtractionUtil instance;
    private final ExternalNLPGateway gateway;
    private final CacheRepository cache;
    
    private RootExtractionUtil() {
        this.gateway = AlKhalilGateway.getInstance();
        this.cache = CacheRepository.getInstance();
        System.out.println("RootExtractionUtil instance created with AlKhalil integration");
    }
    
    public static RootExtractionUtil getInstance() {
        if (instance == null) {
            instance = new RootExtractionUtil();
        }
        return instance;
    }
    
    /**
     * Extract roots for a single token using AlKhalil Root Extractor API
     * @param tokenText The Arabic token text
     * @return List of root information as strings (format: root|pattern|confidence)
     */
    public List<String> extractRoots(String tokenText) {
        List<String> roots = new ArrayList<>();
        
        if (tokenText == null || tokenText.trim().isEmpty()) {
            return roots;
        }
        
        String trimmedToken = tokenText.trim();
        String cacheKey = "roots:" + trimmedToken;
        
        // Check cache first
        @SuppressWarnings("unchecked")
        List<String> cachedRoots = cache.get(cacheKey, List.class);
        if (cachedRoots != null) {
            System.out.println("Using cached roots for: " + trimmedToken);
            return cachedRoots;
        }
        
        // Try AlKhalil Root Extractor API first
        if (gateway.isServiceAvailable()) {
            try {
                List<Map<String, String>> rootMaps = gateway.extractRoot(trimmedToken);
                if (rootMaps != null && !rootMaps.isEmpty()) {
                    for (Map<String, String> rootMap : rootMaps) {
                        String root = rootMap.getOrDefault("root", "");
                        String pattern = rootMap.getOrDefault("pattern", "unknown");
                        String confidence = rootMap.getOrDefault("confidence", "0.8");
                        roots.add(root + "|" + pattern + "|" + confidence);
                    }
                    
                    if (!roots.isEmpty()) {
                        cache.put(cacheKey, roots);
                        System.out.println("AlKhalil extracted " + roots.size() + " roots for token: " + trimmedToken);
                        return roots;
                    }
                }
            } catch (Exception e) {
                System.err.println("AlKhalil Root Extractor API error: " + e.getMessage());
            }
        }
        
        // Fallback to rule-based extraction
        roots = extractRootsFallback(trimmedToken);
        if (!roots.isEmpty()) {
            cache.put(cacheKey, roots);
        }
        
        System.out.println("Fallback extracted " + roots.size() + " roots for token: " + trimmedToken);
        return roots;
    }
    
    
    /**
     * Fallback root extraction method when API is unavailable
     * Uses basic morphological analysis
     */
    private List<String> extractRootsFallback(String tokenText) {
        List<String> roots = new ArrayList<>();
        
        if (tokenText == null || tokenText.trim().isEmpty()) {
            return roots;
        }
        
        // Basic fallback: try to extract root by removing common affixes
        String cleaned = tokenText.trim();
        
        // Remove common prefixes
        String[] prefixes = {"ال", "بال", "كال", "فال", "وال", "تال", "نال", "ب", "ل", "ك", "ف", "و", "ت", "ن", "أ"};
        for (String prefix : prefixes) {
            if (cleaned.startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length());
                break;
            }
        }
        
        // Remove common suffixes
        String[] suffixes = {"ون", "ين", "ان", "ات", "ة", "ت", "ن", "ا", "و", "ي", "م", "ك", "ه", "ها", "هم", "هن"};
        for (String suffix : suffixes) {
            if (cleaned.endsWith(suffix) && cleaned.length() > suffix.length()) {
                cleaned = cleaned.substring(0, cleaned.length() - suffix.length());
                break;
            }
        }
        
        // If we have a reasonable root candidate (3-5 characters typical for Arabic roots)
        if (cleaned.length() >= 3 && cleaned.length() <= 6) {
            roots.add(cleaned + "|fallback|0.5");
        } else {
            // Use original token as fallback
            roots.add(tokenText + "|fallback|0.3");
        }
        
        return roots;
    }
    
    /**
     * Extract roots for multiple tokens
     */
    public List<List<String>> extractRootsForTokens(List<String> tokens) {
        List<List<String>> allRoots = new ArrayList<>();
        for (String token : tokens) {
            allRoots.add(extractRoots(token));
        }
        return allRoots;
    }
}

