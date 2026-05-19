package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator for coordinating all Arabic morphological analysis steps
 * Provides unified interface for tokenization, segmentation, and root extraction
 */
public class AnalysisOrchestrator {
    private static AnalysisOrchestrator instance;
    private final TokenizationUtil tokenizationUtil;
    private final SegmentationUtil segmentationUtil;
    private final RootExtractionUtil rootExtractionUtil;
    private final ExternalNLPGateway gateway;
    
    /**
     * Result class for complete morphological analysis
     */
    public static class AnalysisResult {
        private String token;
        private String prefix;
        private String stem;
        private String suffix;
        private List<String> roots;
        private double confidenceScore;
        private String source; // "alkhalil" or "fallback"
        
        public AnalysisResult(String token, String prefix, String stem, String suffix, 
                             List<String> roots, double confidenceScore, String source) {
            this.token = token;
            this.prefix = prefix;
            this.stem = stem;
            this.suffix = suffix;
            this.roots = roots;
            this.confidenceScore = confidenceScore;
            this.source = source;
        }
        
        // Getters
        public String getToken() { return token; }
        public String getPrefix() { return prefix; }
        public String getStem() { return stem; }
        public String getSuffix() { return suffix; }
        public List<String> getRoots() { return roots; }
        public double getConfidenceScore() { return confidenceScore; }
        public String getSource() { return source; }
        
        @Override
        public String toString() {
            return String.format("AnalysisResult{token='%s', prefix='%s', stem='%s', suffix='%s', roots=%s, confidence=%.2f, source='%s'}", 
                    token, prefix, stem, suffix, roots, confidenceScore, source);
        }
    }
    
    private AnalysisOrchestrator() {
        this.tokenizationUtil = TokenizationUtil.getInstance();
        this.segmentationUtil = SegmentationUtil.getInstance();
        this.rootExtractionUtil = RootExtractionUtil.getInstance();
        this.gateway = AlKhalilGateway.getInstance();
        System.out.println("AnalysisOrchestrator instance created");
    }
    
    public static synchronized AnalysisOrchestrator getInstance() {
        if (instance == null) {
            instance = new AnalysisOrchestrator();
        }
        return instance;
    }
    
    /**
     * Perform complete morphological analysis on a text
     * @param text The Arabic text to analyze
     * @return List of AnalysisResult objects, one per token
     */
    public List<AnalysisResult> analyzeText(String text) {
        List<AnalysisResult> results = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return results;
        }
        
        // Step 1: Tokenization
        List<String> tokens = tokenizationUtil.tokenizeText(text);
        
        // Step 2: For each token, perform segmentation and root extraction
        for (String token : tokens) {
            AnalysisResult result = analyzeToken(token);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Perform complete morphological analysis on a single token
     * @param token The Arabic token to analyze
     * @return AnalysisResult with segmentation and roots
     */
    public AnalysisResult analyzeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new AnalysisResult("", "", "", "", new ArrayList<>(), 0.0, "empty");
        }
        
        String trimmedToken = token.trim();
        double confidenceScore = 0.0;
        String source = "fallback";
        
        // Check if AlKhalil services are available
        boolean usingAlKhalil = gateway.isServiceAvailable();
        
        // Step 1: Segmentation
        String[] segments = segmentationUtil.segmentToken(trimmedToken);
        String prefix = segments[0];
        String stem = segments[1];
        String suffix = segments[2];
        
        // Step 2: Root Extraction
        List<String> rootStrings = rootExtractionUtil.extractRoots(trimmedToken);
        List<String> roots = new ArrayList<>();
        
        // Parse root strings and calculate confidence
        double totalConfidence = 0.0;
        for (String rootString : rootStrings) {
            String[] parts = rootString.split("\\|");
            if (parts.length > 0) {
                roots.add(parts[0]); // Just the root text
                if (parts.length > 2) {
                    try {
                        totalConfidence += Double.parseDouble(parts[2]);
                    } catch (NumberFormatException e) {
                        totalConfidence += 0.5; // Default confidence
                    }
                }
            }
        }
        
        // Calculate average confidence
        if (!roots.isEmpty()) {
            confidenceScore = totalConfidence / roots.size();
        } else {
            confidenceScore = 0.3; // Low confidence if no roots found
        }
        
        // Determine source
        if (usingAlKhalil && confidenceScore > 0.5) {
            source = "alkhalil";
        } else {
            source = "fallback";
            confidenceScore = Math.max(confidenceScore, 0.3); // Minimum confidence for fallback
        }
        
        // Validate segmentation
        String reconstructed = prefix + stem + suffix;
        if (!reconstructed.equals(trimmedToken) && reconstructed.length() < trimmedToken.length() * 0.8) {
            // Segmentation seems incorrect, adjust confidence
            confidenceScore *= 0.7;
        }
        
        return new AnalysisResult(trimmedToken, prefix, stem, suffix, roots, confidenceScore, source);
    }
    
    /**
     * Analyze multiple tokens in batch
     * @param tokens List of tokens to analyze
     * @return List of AnalysisResult objects
     */
    public List<AnalysisResult> analyzeTokens(List<String> tokens) {
        List<AnalysisResult> results = new ArrayList<>();
        for (String token : tokens) {
            results.add(analyzeToken(token));
        }
        return results;
    }
    
    /**
     * Get analysis statistics
     * @param results List of analysis results
     * @return Map with statistics
     */
    public Map<String, Object> getStatistics(List<AnalysisResult> results) {
        Map<String, Object> stats = new HashMap<>();
        
        int totalTokens = results.size();
        int alkhalilCount = 0;
        int fallbackCount = 0;
        double totalConfidence = 0.0;
        int tokensWithRoots = 0;
        
        for (AnalysisResult result : results) {
            if ("alkhalil".equals(result.getSource())) {
                alkhalilCount++;
            } else {
                fallbackCount++;
            }
            
            totalConfidence += result.getConfidenceScore();
            
            if (!result.getRoots().isEmpty()) {
                tokensWithRoots++;
            }
        }
        
        stats.put("totalTokens", totalTokens);
        stats.put("alkhalilCount", alkhalilCount);
        stats.put("fallbackCount", fallbackCount);
        stats.put("averageConfidence", totalTokens > 0 ? totalConfidence / totalTokens : 0.0);
        stats.put("tokensWithRoots", tokensWithRoots);
        stats.put("rootExtractionRate", totalTokens > 0 ? (double) tokensWithRoots / totalTokens : 0.0);
        
        return stats;
    }
}






