package PresentationLayerPL;


import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import BuisnessLayerBL.IBusinessFacade;
import BuisnessLayerBL.ITokenOccurrenceBo;
import ModelDTO.Sentence;
import util.TextHighlighter;

public class TokenOccurrencePanel extends JPanel {
 private final ITokenOccurrenceBo tokenOccurrenceBo;
 private final IBusinessFacade facade;
 private JComboBox<String> tokenComboBox;
 private JTextPane resultsPane;
 private JLabel statusLabel;

 public TokenOccurrencePanel(ITokenOccurrenceBo tokenOccurrenceBo, IBusinessFacade facade) {
     this.tokenOccurrenceBo = tokenOccurrenceBo;
     this.facade = facade;
     initializeUI();
 }

 private void initializeUI() {
     setLayout(new BorderLayout(10, 10));
     setBorder(new EmptyBorder(10, 10, 10, 10));

     // Search panel
     JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
     JLabel searchLabel = new JLabel("Search Token / Phrase:");
     tokenComboBox = new JComboBox<>();
     tokenComboBox.setEditable(true);
     JButton searchButton = new JButton("Search Tokens");
     JButton sentenceSearchButton = new JButton("Search Sentences (Exact)");
     JButton regexSearchButton = new JButton("Regex Search");
     
     searchButton.addActionListener(e -> searchTokenOccurrences());
     sentenceSearchButton.addActionListener(e -> searchSentencesExact());
     regexSearchButton.addActionListener(e -> searchSentencesByRegex());
     tokenComboBox.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyPressed(java.awt.event.KeyEvent e) {
             if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                 searchTokenOccurrences();
             }
         }
     });

     JPanel buttonPanel = new JPanel(new java.awt.GridLayout(1, 3, 5, 0));
     buttonPanel.add(searchButton);
     buttonPanel.add(sentenceSearchButton);
     buttonPanel.add(regexSearchButton);

     searchPanel.add(searchLabel, BorderLayout.WEST);
     searchPanel.add(tokenComboBox, BorderLayout.CENTER);
     searchPanel.add(buttonPanel, BorderLayout.EAST);

     // Results area
     resultsPane = new JTextPane();
     resultsPane.setContentType("text/html");
     resultsPane.setEditable(false);
     JScrollPane scrollPane = new JScrollPane(resultsPane);

     // Status bar
     statusLabel = new JLabel("Ready");
     statusLabel.setBorder(BorderFactory.createEtchedBorder());

     add(searchPanel, BorderLayout.NORTH);
     add(scrollPane, BorderLayout.CENTER);
     add(statusLabel, BorderLayout.SOUTH);
 }

 private void searchTokenOccurrences() {
     String token = (String) tokenComboBox.getEditor().getItem();
     if (token == null || token.trim().isEmpty()) {
         JOptionPane.showMessageDialog(this, "Please enter a token to search", 
             "Input Error", JOptionPane.WARNING_MESSAGE);
         return;
     }

     try {
         List<Map<String, String>> results = tokenOccurrenceBo.getSentencesByToken(token);
         displayResults(results, token);
         statusLabel.setText("Found " + results.size() + " occurrences");
     } catch (Exception e) {
         statusLabel.setText("Error: " + e.getMessage());
         resultsPane.setText("<html><body>Error: " + e.getMessage() + "</body></html>");
     }
 }

private void searchSentencesExact() {
    String phraseInput = (String) tokenComboBox.getEditor().getItem();
    String phrase = normalizePhrase(phraseInput);
    if (phrase.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a phrase to search", 
            "Input Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        java.util.List<Sentence> sentences = facade.searchSentencesByExactString(phrase);
        displayExactSentenceResults(sentences, phrase);
        statusLabel.setText("Found " + sentences.size() + " sentences matching exact phrase");
    } catch (Exception e) {
        statusLabel.setText("Error: " + e.getMessage());
        resultsPane.setText("<html><body>Error: " + e.getMessage() + "</body></html>");
    }
}

private void searchSentencesByRegex() {
    String pattern = (String) tokenComboBox.getEditor().getItem();
    if (pattern == null || pattern.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a regex pattern to search", 
            "Input Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        List<Map<String, String>> results = facade.searchSentencesByRegex(pattern);
        displayRegexResults(results, pattern);
        statusLabel.setText("Found " + results.size() + " sentences matching regex");
    } catch (PatternSyntaxException e) {
        JOptionPane.showMessageDialog(this, "Invalid regex pattern:\n" + e.getMessage(),
            "Regex Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Invalid regex pattern");
    } catch (Exception e) {
        statusLabel.setText("Error: " + e.getMessage());
        resultsPane.setText("<html><body>Error: " + e.getMessage() + "</body></html>");
    }
}

 private void displayResults(List<Map<String, String>> results, String token) {
     StringBuilder html = new StringBuilder();
     html.append("<html><body style='font-family: Arial; padding: 10px;'>");
     
     if (results.isEmpty()) {
         html.append("<p>No occurrences found for token: ").append(token).append("</p>");
     } else {
         html.append("<h3>Occurrences of '").append(token).append("':</h3>");
         html.append("<ul style='margin-top: 0;'>");
         
         for (Map<String, String> row : results) {
             String highlighted = TextHighlighter.highlightToken(
                 row.get("sentence_text"), token);
             
             html.append("<li style='margin-bottom: 10px;'>")
                .append("<b>").append(row.get("chapter_title")).append("</b><br>")
                .append(highlighted).append("</li>");
         }
         
         html.append("</ul>");
     }
     html.append("</body></html>");
     
     resultsPane.setText(html.toString());
     resultsPane.setCaretPosition(0);
 }
private void displayExactSentenceResults(java.util.List<Sentence> sentences, String phrase) {
    StringBuilder html = new StringBuilder();
    html.append("<html><body style='font-family: Arial; padding: 10px;'>");
    
    if (sentences.isEmpty()) {
        html.append("<p>No sentences found containing phrase: ").append(phrase).append("</p>");
    } else {
        html.append("<h3>Sentences containing '").append(phrase).append("':</h3>");
        html.append("<ul style='margin-top: 0;'>");
        
        for (Sentence sentence : sentences) {
            String highlighted = TextHighlighter.highlightExactPhrase(
                sentence.getText(), phrase);
            
            html.append("<li style='margin-bottom: 10px;'>")
               .append("<b>Sentence #")
               .append(sentence.getSentenceNumber())
               .append(" (ID ")
               .append(sentence.getSentenceId())
               .append(")</b><br>")
               .append(highlighted)
               .append("</li>");
        }
        
        html.append("</ul>");
    }
    html.append("</body></html>");
    
    resultsPane.setText(html.toString());
    resultsPane.setCaretPosition(0);
}

private void displayRegexResults(List<Map<String, String>> results, String pattern) {
    StringBuilder html = new StringBuilder();
    html.append("<html><body style='font-family: Arial; padding: 10px;'>");
    
    if (results.isEmpty()) {
        html.append("<p>No sentences found matching regex: ").append(pattern).append("</p>");
    } else {
        html.append("<h3>Regex matches for '").append(pattern).append("':</h3>");
        html.append("<ul style='margin-top: 0;'>");
        
        try {
            for (Map<String, String> row : results) {
                String sentenceText = row.get("sentence_text");
                String highlighted = TextHighlighter.highlightRegexMatches(sentenceText, pattern);
                
                html.append("<li style='margin-bottom: 10px;'>")
                   .append("<b>Sentence ID ")
                   .append(row.get("sentence_id"))
                   .append(" (Chapter ")
                   .append(row.get("chapter_id"))
                   .append(", #")
                   .append(row.get("sentence_number"))
                   .append(")</b><br>")
                   .append(highlighted)
                   .append("</li>");
            }
        } catch (PatternSyntaxException e) {
            // Surface a friendly error to the UI
            html.setLength(0);
            html.append("<html><body style='font-family: Arial; padding: 10px;'>")
                .append("<p style='color:red;'>Invalid regex pattern: ")
                .append(e.getMessage())
                .append("</p></body></html>");
            resultsPane.setText(html.toString());
            resultsPane.setCaretPosition(0);
            throw e;
        }
        
        html.append("</ul>");
    }
    html.append("</body></html>");
    
    resultsPane.setText(html.toString());
    resultsPane.setCaretPosition(0);
}

private String normalizePhrase(String phrase) {
    if (phrase == null) {
        return "";
    }
    return phrase.trim().replaceAll("\\s+", " ");
}
}