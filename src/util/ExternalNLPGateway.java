package util;

import java.util.List;
import java.util.Map;

/**
 * Gateway interface for external NLP services (AlKhalil)
 * Provides abstraction for external API calls
 */
public interface ExternalNLPGateway {
    /**
     * Extract root using AlKhalil Root Extractor
     * @param tokenText The Arabic token
     * @return List of root information maps with keys: root, pattern, confidence
     */
    List<Map<String, String>> extractRoot(String tokenText);
    
    /**
     * Segment token using AlKhalil Stemmer
     * @param tokenText The Arabic token
     * @return Map with keys: prefix, stem, suffix
     */
    Map<String, String> segmentToken(String tokenText);
    
    /**
     * Check if the external service is available
     * @return true if service is available
     */
    boolean isServiceAvailable();
    
    /**
     * Get service health status
     * @return Service status message
     */
    String getServiceStatus();
    
    /**
     * Lemmatize token using AlKhalil
     * @param tokenText The Arabic token
     * @return Map containing the lemma and other analysis information
     */
    Map<String, String> lemmatizeToken(String tokenText);
}






