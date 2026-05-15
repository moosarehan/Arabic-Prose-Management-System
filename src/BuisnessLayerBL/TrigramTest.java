package BuisnessLayerBL;

import java.util.Set;

public class TrigramTest {
    public static void main(String[] args) {
        TrigramSimilarityService service = new TrigramSimilarityService();
        
        String s1 = "hello world";
        String s2 = "hello word";
        
        Set<String> t1 = service.generateTrigrams(s1);
        Set<String> t2 = service.generateTrigrams(s2);
        
        System.out.println("Trigrams 1: " + t1);
        System.out.println("Trigrams 2: " + t2);
        
        double similarity = service.calculateJaccardSimilarity(t1, t2);
        System.out.println("Similarity: " + similarity);
        
        if (similarity > 0.0 && similarity < 1.0) {
            System.out.println("✅ Similarity calculation seems correct.");
        } else {
            System.out.println("❌ Similarity calculation might be wrong.");
        }
    }
}
