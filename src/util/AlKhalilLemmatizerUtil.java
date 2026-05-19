// src/util/AlKhalilLemmatizerUtil.java
package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlKhalilLemmatizerUtil {
    private static AlKhalilLemmatizerUtil instance;
    private final AlKhalilGateway alKhalilGateway;

    private AlKhalilLemmatizerUtil() {
        this.alKhalilGateway = AlKhalilGateway.getInstance();
    }

    public static synchronized AlKhalilLemmatizerUtil getInstance() {
        if (instance == null) {
            instance = new AlKhalilLemmatizerUtil();
        }
        return instance;
    }

    public Map<String, String> lemmatize(String token) {
        Map<String, String> result = new HashMap<>();
        try {
            // Try to get lemma from AlKhalil web service
            Map<String, String> analysis = alKhalilGateway.lemmatizeToken(token);
            if (analysis != null && analysis.containsKey("lemma")) {
                String lemma = analysis.get("lemma");
                if (lemma != null && !lemma.trim().isEmpty()) {
                    result.put("lemma", normalizeLemma(lemma));
                    result.put("source", "alkhalil");
                    return result;
                }
            }
            
            // Fallback to root extraction
            List<Map<String, String>> roots = alKhalilGateway.extractRoot(token);
            if (roots != null && !roots.isEmpty() && roots.get(0) != null) {
                String root = roots.get(0).get("root");
                if (root != null && !root.trim().isEmpty()) {
                    result.put("lemma", normalizeLemma(root));
                    result.put("source", "root_extraction");
                    return result;
                }
            }
            
            // Final fallback to stem
            Map<String, String> segmentation = alKhalilGateway.segmentToken(token);
            if (segmentation != null && segmentation.containsKey("stem")) {
                String stem = segmentation.get("stem");
                if (stem != null && !stem.trim().isEmpty()) {
                    result.put("lemma", normalizeLemma(stem));
                    result.put("source", "stemming");
                    return result;
                }
            }
            
            // If all else fails, return the token itself
            result.put("lemma", token);
            result.put("source", "fallback");
            return result;
            
        } catch (Exception e) {
            System.err.println("Error in AlKhalil lemmatization: " + e.getMessage());
            result.put("lemma", token);
            result.put("source", "error");
            return result;
        }
    }

    private String normalizeLemma(String lemma) {
        if (lemma == null) return "";
        // Remove diacritics and normalize the lemma
        return lemma.replaceAll("[\\u064B-\\u065F\\u0670]", "").trim();
    }
}