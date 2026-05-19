package util;

import java.util.List;
import java.util.Map;

public class LemmatizationUtil {
    private static LemmatizationUtil instance;
    private final AlKhalilGateway alKhalilGateway;
    private final boolean useFallback = true; // Always use fallback for now

    private LemmatizationUtil() {
        this.alKhalilGateway = AlKhalilGateway.getInstance();
        System.out.println("LemmatizationUtil initialized with fallback lemmatization");
    }

    public static synchronized LemmatizationUtil getInstance() {
        if (instance == null) {
            instance = new LemmatizationUtil();
        }
        return instance;
    }

    public String lemmatize(String token) {
        if (token == null || token.trim().isEmpty()) {
            return "";
        }

        try {
            if (!useFallback) {
                Map<String, String> result = alKhalilGateway.lemmatizeToken(token.trim());
                String lemma = result.get("lemma");
                if (lemma != null && !lemma.trim().isEmpty() && !lemma.equals(token)) {
                    return lemma;
                }
            }
            
            // Always use fallback
            List<Map<String, String>> roots = alKhalilGateway.extractRoot(token);
            String root = (roots != null && !roots.isEmpty()) ? roots.get(0).get("root") : null;
            return ArabicLemmaConverter.lemmatize(token, root);
            
        } catch (Exception e) {
            System.err.println("Error in lemmatization, using fallback: " + e.getMessage());
            return ArabicLemmaConverter.lemmatize(token, null);
        }
    }
}