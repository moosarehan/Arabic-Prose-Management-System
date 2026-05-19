package util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//In ArabicLemmaConverter.java, enhance the lemmatization logic
public class ArabicLemmaConverter {
 private static final Set<String> DEFINITE_ARTICLE = new HashSet<>(Arrays.asList("ال", "وال", "فال", "بال", "كال"));
 private static final Set<String> VERB_PREFIXES = new HashSet<>(Arrays.asList("ي", "ت", "أ", "ن", "ا", "است", "س", "سي", "سوف", "لن", "لم", "لا"));
 private static final Set<String> VERB_SUFFIXES = new HashSet<>(Arrays.asList("ون", "ان", "ين", "وا", "ت", "نا", "تم", "تن", "تما", "تمو", "تن", "ي", "ا", "ت"));
 private static final Set<String> NOUN_SUFFIXES = new HashSet<>(Arrays.asList("ة", "ات", "ون", "ين", "ان", "تان", "ين", "ون", "وا", "ي", "ه", "ها", "هم", "هن", "كما", "كم"));

 public static String lemmatize(String token, String root) {
     if (token == null || token.trim().isEmpty()) {
         return token;
     }
     
     String result = token.trim();
     
     // Remove diacritics first
     result = removeDiacritics(result);
     
     // Remove definite article
     result = removeDefiniteArticle(result);
     
     // Try to determine if it's a verb or noun
     if (isLikelyVerb(result)) {
         result = lemmatizeVerb(result, root);
     } else {
         result = lemmatizeNoun(result, root);
     }
     
     return result;
 }
 
 private static String removeDiacritics(String text) {
     return text.replaceAll("[ًٌٍَُِّْ~ٰٓ]", "");
 }
 
 private static String removeDefiniteArticle(String text) {
     for (String article : DEFINITE_ARTICLE) {
         if (text.startsWith(article)) {
             return text.substring(article.length());
         }
     }
     return text;
 }
 
 private static boolean isLikelyVerb(String text) {
     // Simple heuristic: if it starts with a verb prefix, it's likely a verb
     return VERB_PREFIXES.stream().anyMatch(text::startsWith);
 }
 
 private static String lemmatizeVerb(String text, String root) {
     // Remove verb prefixes
     for (String prefix : VERB_PREFIXES) {
         if (text.startsWith(prefix)) {
             text = text.substring(prefix.length());
             break;
         }
     }
     
     // Remove verb suffixes
     for (String suffix : VERB_SUFFIXES) {
         if (text.endsWith(suffix)) {
             text = text.substring(0, text.length() - suffix.length());
             break;
         }
     }
     
     // If we have a root, try to construct the base form
     if (root != null && root.length() >= 3) {
         char[] rootChars = root.toCharArray();
         if (text.length() >= 3) {
             return String.valueOf(rootChars[0]) + "َ" + 
                    String.valueOf(rootChars[1]) + "َ" + 
                    String.valueOf(rootChars[2]) + "َ";
         }
     }
     
     return text;
 }
 
 private static String lemmatizeNoun(String text, String root) {
     // Remove noun suffixes
     for (String suffix : NOUN_SUFFIXES) {
         if (text.endsWith(suffix)) {
             text = text.substring(0, text.length() - suffix.length());
             break;
         }
     }
     
     // If we have a root, try to use it
     if (root != null && root.length() >= 3) {
         // Simple pattern matching for broken plurals
         if (text.length() > 3 && text.contains(root)) {
             return root;
         }
     }
     
     return text;
 }
}