package BuisnessLayerBL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ModelDTO.Sentence;
import ModelDTO.SentenceSearchResult;

public class TrigramSimilarityService {

    /**
     * Generates a set of character trigrams from the input text.
     * Case-insensitive and ignores non-alphanumeric characters if needed, 
     * but for now we'll just lowercase and trim.
     */
    public Set<String> generateTrigrams(String text) {
        Set<String> trigrams = new HashSet<>();
        if (text == null || text.length() < 3) {
            return trigrams;
        }

        // Normalize: Lowercase and maybe remove extra spaces
        // Depending on requirements, we might want to keep diacritics or remove them.
        // For now, we use the text as is, just lowercased.
        String normalized = text.trim().toLowerCase();
        
        // If we want to be robust against punctuation, we might strip it.
        // normalized = normalized.replaceAll("[^\\p{L}\\p{N}\\s]", "");

        for (int i = 0; i <= normalized.length() - 3; i++) {
            trigrams.add(normalized.substring(i, i + 3));
        }
        return trigrams;
    }

    /**
     * Computes Jaccard similarity between two sets of trigrams.
     * J(A, B) = |A ∩ B| / |A ∪ B|
     */
    public double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    public List<SentenceSearchResult> search(String input, double thresholdPercentage, List<Sentence> allSentences) {
        List<SentenceSearchResult> results = new ArrayList<>();
        Set<String> inputTrigrams = generateTrigrams(input);

        if (inputTrigrams.isEmpty()) {
            return results;
        }

        double threshold = thresholdPercentage / 100.0;

        for (Sentence sentence : allSentences) {
            // We can use text or textDiacritized. Using raw text for now as it's more likely what user types.
            // Or we could try both. Let's stick to 'text' field.
            Set<String> sentenceTrigrams = generateTrigrams(sentence.getText());
            
            double similarity = calculateJaccardSimilarity(inputTrigrams, sentenceTrigrams);
            
            if (similarity >= threshold) {
                results.add(new SentenceSearchResult(sentence, similarity));
            }
        }
        
        // Sort by similarity descending
        results.sort((r1, r2) -> Double.compare(r2.getSimilarityScore(), r1.getSimilarityScore()));
        
        return results;
    }
    
    // Quick test main method
    public static void main(String[] args) {
        TrigramSimilarityService service = new TrigramSimilarityService();
        String s1 = "hello world";
        String s2 = "hello word";
        
        System.out.println("Trigrams s1: " + service.generateTrigrams(s1));
        System.out.println("Trigrams s2: " + service.generateTrigrams(s2));
        System.out.println("Similarity: " + service.calculateJaccardSimilarity(service.generateTrigrams(s1), service.generateTrigrams(s2)));
    }
}
