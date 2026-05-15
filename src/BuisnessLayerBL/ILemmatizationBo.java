package BuisnessLayerBL;

public interface ILemmatizationBo {
    /**
     * Returns the lemma (dictionary form) for the provided token text.
     * 
     * @param tokenText the original token
     * @return normalized lemma or empty string if unavailable
     */
    String lemmatizeToken(String tokenText);
}






