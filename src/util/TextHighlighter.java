// In util/TextHighlighter.java
package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextHighlighter {
    public static String highlightToken(String text, String token) {
        if (text == null || token == null || token.isEmpty()) {
            return text;
        }
        return text.replaceAll("(" + token + ")", "<span style='background-color:yellow'>$1</span>");
    }
    
    /**
     * Highlight all exact occurrences of the given phrase within the text,
     * using a literal, case-sensitive match and wrapping matches in the same
     * HTML span style as token highlighting.
     */
    public static String highlightExactPhrase(String text, String phrase) {
        if (text == null || phrase == null || phrase.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile(Pattern.quote(phrase));
        Matcher matcher = pattern.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(text, lastEnd, matcher.start());
            result.append("<span style='background-color:yellow'>");
            result.append(matcher.group());
            result.append("</span>");
            lastEnd = matcher.end();
        }
        result.append(text.substring(lastEnd));
        return result.toString();
    }
    
    /**
     * Highlight all substrings of the sentence that match the given
     * regular expression, using Java's regex engine.
     */
    public static String highlightRegexMatches(String text, String regexPattern) {
        if (text == null || regexPattern == null || regexPattern.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(text, lastEnd, matcher.start());
            result.append("<span style='background-color:yellow'>");
            result.append(matcher.group());
            result.append("</span>");
            lastEnd = matcher.end();
        }
        result.append(text.substring(lastEnd));
        return result.toString();
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}