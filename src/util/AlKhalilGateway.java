package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of ExternalNLPGateway for AlKhalil services
 * Includes circuit breaker pattern and retry mechanisms
 */
public class AlKhalilGateway implements ExternalNLPGateway {
    private static AlKhalilGateway instance;
    
    // AlKhalil API endpoints
    private static final String ROOT_EXTRACTOR_URL = "http://oujda-nlp-team.net:8080/api/Racine";
    private static final String STEMMER_URL = "http://oujda-nlp-team.net:8080/api/Stemmer";
    
    // Circuit breaker configuration
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5; // Failures before opening circuit
    private static final long CIRCUIT_BREAKER_TIMEOUT = 60000; // 60 seconds
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY = 1000; // 1 second
    
    // Circuit breaker state
    private enum CircuitState { CLOSED, OPEN, HALF_OPEN }
    private volatile CircuitState circuitState = CircuitState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    
    private AlKhalilGateway() {
        System.out.println("AlKhalilGateway instance created");
    }
    
    public static synchronized AlKhalilGateway getInstance() {
        if (instance == null) {
            instance = new AlKhalilGateway();
        }
        return instance;
    }
    
 // In AlKhalilGateway.java, add this constant with other URL constants
    private static final String LEMMATIZER_URL = "http://oujda-nlp-team.net:8080/api/Lemmatizer";

    // Add this method to AlKhalilGateway
    private String callAlKhalilLemmatizer(String token) throws IOException {
        if (!isCircuitClosed()) {
            throw new IOException("Circuit breaker is open");
        }

        URL url = java.net.URI.create(LEMMATIZER_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        String urlParameters = "word=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = urlParameters.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }

 // In AlKhalilGateway.java, update the lemmatizeToken method:

    @Override
    public Map<String, String> lemmatizeToken(String tokenText) {
        Map<String, String> result = new HashMap<>();
        if (tokenText == null || tokenText.trim().isEmpty()) {
            result.put("lemma", "");
            result.put("source", "empty");
            return result;
        }

        // Check circuit breaker
        if (!isCircuitClosed()) {
            System.out.println("Circuit breaker is OPEN, using fallback lemmatization");
            String fallback = ArabicLemmaConverter.lemmatize(tokenText.trim(), null);
            result.put("lemma", fallback);
            result.put("source", "circuit_breaker_fallback");
            return result;
        }

        try {
            // Call the lemmatizer API
            String response = callAlKhalilLemmatizer(tokenText);
            String lemma = parseLemmatizerResponse(response);
            
            if (lemma != null && !lemma.trim().isEmpty() && !lemma.equals(tokenText)) {
                result.put("lemma", lemma);
                result.put("source", "alkhalil");
            } else {
                // Fallback to root-based lemmatization
                List<Map<String, String>> roots = extractRoot(tokenText);
                String root = (roots != null && !roots.isEmpty()) ? roots.get(0).get("root") : null;
                String fallbackLemma = ArabicLemmaConverter.lemmatize(tokenText, root);
                
                result.put("lemma", fallbackLemma);
                result.put("source", "fallback");
            }
        } catch (Exception e) {
            System.err.println("Error in AlKhalil lemmatization: " + e.getMessage());
            // Last resort fallback
            String fallback = ArabicLemmaConverter.lemmatize(tokenText, null);
            result.put("lemma", fallback);
            result.put("source", "error_fallback");
        }

        return result;
    }

 // Replace the existing parseLemmatizerResponse method with this one
    private String parseLemmatizerResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        try {
            // Simple JSON parsing without external dependencies
            // Looking for patterns like: "lemma": "value" or "result": {"lemma": "value"}
            String lowerResponse = response.toLowerCase();
            int lemmaIdx = lowerResponse.indexOf("\"lemma\"");
            
            if (lemmaIdx != -1) {
                // Find the value after "lemma": 
                int valueStart = response.indexOf(':', lemmaIdx) + 1;
                if (valueStart > 0) {
                    // Find the opening quote
                    int quoteStart = response.indexOf('"', valueStart);
                    if (quoteStart != -1) {
                        // Find the closing quote
                        int quoteEnd = response.indexOf('"', quoteStart + 1);
                        if (quoteEnd != -1) {
                            return response.substring(quoteStart + 1, quoteEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing lemmatizer response: " + e.getMessage());
        }
        return null;
    }
    
    
    @Override
    public List<Map<String, String>> extractRoot(String tokenText) {
        if (tokenText == null || tokenText.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Check circuit breaker
        if (!isCircuitClosed()) {
            System.out.println("Circuit breaker is OPEN, skipping API call");
            return new ArrayList<>();
        }
        
        List<Map<String, String>> roots = new ArrayList<>();
        
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String response = callAlKhalilRootExtractor(tokenText);
                if (response != null && !response.isEmpty()) {
                    roots = parseRootExtractorResponse(response);
                    recordSuccess();
                    break;
                }
            } catch (Exception e) {
                System.err.println("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        long delay = INITIAL_RETRY_DELAY * (long) Math.pow(2, attempt);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    recordFailure();
                }
            }
        }
        
        return roots;
    }
    
    @Override
    public Map<String, String> segmentToken(String tokenText) {
        if (tokenText == null || tokenText.trim().isEmpty()) {
            Map<String, String> empty = new HashMap<>();
            empty.put("prefix", "");
            empty.put("stem", tokenText);
            empty.put("suffix", "");
            return empty;
        }
        
        // Check circuit breaker
        if (!isCircuitClosed()) {
            System.out.println("Circuit breaker is OPEN, skipping API call");
            Map<String, String> fallback = new HashMap<>();
            fallback.put("prefix", "");
            fallback.put("stem", tokenText);
            fallback.put("suffix", "");
            return fallback;
        }
        
        Map<String, String> segments = new HashMap<>();
        boolean apiSuccess = false;
        
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String response = callAlKhalilStemmer(tokenText);
                if (response != null && !response.isEmpty()) {
                    segments = parseStemmerResponse(response, tokenText);
                    recordSuccess();
                    apiSuccess = true;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Stemmer attempt " + (attempt + 1) + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        long delay = INITIAL_RETRY_DELAY * (long) Math.pow(2, attempt);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    recordFailure();
                }
            }
        }
        
        if (apiSuccess && !segments.isEmpty()) {
            if (!segments.containsKey("prefix")) segments.put("prefix", "");
            if (!segments.containsKey("stem")) segments.put("stem", tokenText);
            if (!segments.containsKey("suffix")) segments.put("suffix", "");
            segments.put("__source", "alkhalil");
            return segments;
        }
        
        // return empty map to signal failure so caller can fallback
        return new HashMap<>();
    }
    
    @Override
    public boolean isServiceAvailable() {
        return isCircuitClosed();
    }
    
 

    private String cleanStem(String stem) {
        if (stem == null || stem.isEmpty()) return stem;
        
        // Common prefixes to remove
        String[] prefixes = {"ال", "بال", "فال", "وال", "كال", "ولل", "فلل", "وبال", "فبال", "فال", "ولل", "فلل"};
        String[] suffixes = {"ة", "ه", "ها", "نا", "كما", "كم", "كن", "هن", "هم", "هما", "كنت", "ت", "نا", "وا", "ا", "ي", "ن", "تما", "تم", "تن", "وا", "ان", "ين", "ون", "ات"};
        
        String cleaned = stem;
        
        // Remove prefixes
        for (String prefix : prefixes) {
            if (cleaned.startsWith(prefix) && cleaned.length() > prefix.length()) {
                cleaned = cleaned.substring(prefix.length());
                break;
            }
        }
        
        // Remove suffixes
        for (String suffix : suffixes) {
            if (cleaned.endsWith(suffix) && cleaned.length() > suffix.length()) {
                cleaned = cleaned.substring(0, cleaned.length() - suffix.length());
                break;
            }
        }
        
        return cleaned.isEmpty() ? stem : cleaned;
    }
    
   
  
    
    @Override
    public String getServiceStatus() {
        switch (circuitState) {
            case CLOSED:
                return "Service available";
            case OPEN:
                return "Service unavailable (circuit breaker open)";
            case HALF_OPEN:
                return "Service testing availability";
            default:
                return "Unknown status";
        }
    }
    
    // Circuit breaker methods
    private boolean isCircuitClosed() {
        if (circuitState == CircuitState.CLOSED) {
            return true;
        } else if (circuitState == CircuitState.OPEN) {
            // Check if timeout has passed
            long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
            if (timeSinceLastFailure > CIRCUIT_BREAKER_TIMEOUT) {
                circuitState = CircuitState.HALF_OPEN;
                return true;
            }
            return false;
        } else { // HALF_OPEN
            return true;
        }
    }
    
    private void recordSuccess() {
        if (circuitState == CircuitState.HALF_OPEN) {
            circuitState = CircuitState.CLOSED;
        }
        failureCount.set(0);
    }
    
    private void recordFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        
        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            circuitState = CircuitState.OPEN;
            System.err.println("Circuit breaker OPENED after " + failures + " failures");
        }
    }
    
    // API call methods
    private String callAlKhalilRootExtractor(String tokenText) throws Exception {
        URL url = java.net.URI.create(ROOT_EXTRACTOR_URL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        String postData = "textinput=" + URLEncoder.encode(tokenText, StandardCharsets.UTF_8.toString());
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            throw new Exception("HTTP error code: " + responseCode);
        }
    }
    
    private String callAlKhalilStemmer(String tokenText) throws Exception {
        URL url = java.net.URI.create(STEMMER_URL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        String postData = "textinput=" + URLEncoder.encode(tokenText, StandardCharsets.UTF_8.toString());
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            throw new Exception("HTTP error code: " + responseCode);
        }
    }
    
    // Response parsing methods
    private List<Map<String, String>> parseRootExtractorResponse(String response) {
        List<Map<String, String>> roots = new ArrayList<>();
        
        // Try to parse JSON response
        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
            roots = parseJSONRootResponse(response);
        } else {
            // Text format - extract roots
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && line.matches(".*[\\u0600-\\u06FF].*")) {
                    Map<String, String> rootInfo = new HashMap<>();
                    rootInfo.put("root", line);
                    rootInfo.put("pattern", "unknown");
                    rootInfo.put("confidence", "0.8");
                    roots.add(rootInfo);
                }
            }
        }
        
        return roots;
    }
    
    private List<Map<String, String>> parseJSONRootResponse(String json) {
        List<Map<String, String>> roots = new ArrayList<>();
        
        // Simple JSON parsing - look for root patterns
        // Format: {"root": "ك ت ب", "pattern": "...", ...}
        java.util.regex.Pattern rootPattern = java.util.regex.Pattern.compile(
            "\"root\"\\s*:\\s*\"([^\"]+)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Pattern patternPattern = java.util.regex.Pattern.compile(
            "\"pattern\"\\s*:\\s*\"([^\"]+)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        
        java.util.regex.Matcher rootMatcher = rootPattern.matcher(json);
        java.util.regex.Matcher patternMatcher = patternPattern.matcher(json);
        
        while (rootMatcher.find()) {
            Map<String, String> rootInfo = new HashMap<>();
            rootInfo.put("root", rootMatcher.group(1));
            
            if (patternMatcher.find()) {
                rootInfo.put("pattern", patternMatcher.group(1));
            } else {
                rootInfo.put("pattern", "unknown");
            }
            
            rootInfo.put("confidence", "0.8");
            roots.add(rootInfo);
        }
        
        return roots;
    }
    
    private Map<String, String> parseStemmerResponse(String response, String originalToken) {
        Map<String, String> segments = new HashMap<>();
        
        // Try to parse JSON response
        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
            segments = parseJSONStemmerResponse(response, originalToken);
        } else {
            // Text format - try to extract segments
            // AlKhalil stemmer typically returns: prefix|stem|suffix or similar format
            String[] parts = response.split("\\|");
            if (parts.length >= 3) {
                segments.put("prefix", parts[0].trim());
                segments.put("stem", parts[1].trim());
                segments.put("suffix", parts[2].trim());
            } else {
                // Fallback: use original token as stem
                segments.put("prefix", "");
                segments.put("stem", originalToken);
                segments.put("suffix", "");
            }
        }
        
        return segments;
    }
    
    private Map<String, String> parseJSONStemmerResponse(String json, String originalToken) {
        Map<String, String> segments = new HashMap<>();
        
        // Simple JSON parsing
        java.util.regex.Pattern prefixPattern = java.util.regex.Pattern.compile(
            "\"prefix\"\\s*:\\s*\"([^\"]*)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Pattern stemPattern = java.util.regex.Pattern.compile(
            "\"stem\"\\s*:\\s*\"([^\"]+)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Pattern suffixPattern = java.util.regex.Pattern.compile(
            "\"suffix\"\\s*:\\s*\"([^\"]*)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        
        java.util.regex.Matcher prefixMatcher = prefixPattern.matcher(json);
        java.util.regex.Matcher stemMatcher = stemPattern.matcher(json);
        java.util.regex.Matcher suffixMatcher = suffixPattern.matcher(json);
        
        if (prefixMatcher.find()) {
            segments.put("prefix", prefixMatcher.group(1));
        } else {
            segments.put("prefix", "");
        }
        
        if (stemMatcher.find()) {
            segments.put("stem", stemMatcher.group(1));
        } else {
            segments.put("stem", originalToken);
        }
        
        if (suffixMatcher.find()) {
            segments.put("suffix", suffixMatcher.group(1));
        } else {
            segments.put("suffix", "");
        }
        
        return segments;
    }
}

