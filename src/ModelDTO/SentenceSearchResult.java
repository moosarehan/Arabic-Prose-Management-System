package ModelDTO;

public class SentenceSearchResult {
    private Sentence sentence;
    private double similarityScore;

    public SentenceSearchResult(Sentence sentence, double similarityScore) {
        this.sentence = sentence;
        this.similarityScore = similarityScore;
    }

    public Sentence getSentence() {
        return sentence;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }
    
    public double getSimilarityPercentage() {
        return similarityScore * 100.0;
    }
}
