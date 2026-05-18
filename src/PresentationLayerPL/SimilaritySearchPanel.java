package PresentationLayerPL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import BuisnessLayerBL.IBusinessFacade;
import ModelDTO.SentenceSearchResult;
import PresentationLayerPL.ArabicProseUserInterface.RoundedButton;

public class SimilaritySearchPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private IBusinessFacade facade;
    private JTextArea inputArea;
    private JSpinner thresholdSpinner;
    private JTextPane resultsPane;
    private ArabicProseUserInterface mainUI;

    public SimilaritySearchPanel(IBusinessFacade facade, ArabicProseUserInterface mainUI) {
        this.facade = facade;
        this.mainUI = mainUI;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 249, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // Top Panel: Input and Controls
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(new Color(255, 255, 255));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input Sentence
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel inputLabel = new JLabel("Input Sentence:");
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(inputLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        inputArea = new JTextArea(3, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        topPanel.add(new JScrollPane(inputArea), gbc);

        // Threshold
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel thresholdLabel = new JLabel("Similarity Threshold (%):");
        thresholdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(thresholdLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        SpinnerNumberModel model = new SpinnerNumberModel(30.0, 0.0, 100.0, 1.0);
        thresholdSpinner = new JSpinner(model);
        topPanel.add(thresholdSpinner, gbc);

        // Search Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton searchBtn = new RoundedButton("Perform Trigram Similarity Search", new Color(74, 109, 255));
        searchBtn.addActionListener(e -> performSearch());
        topPanel.add(searchBtn, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Results
        resultsPane = new JTextPane();
        resultsPane.setEditable(false);
        resultsPane.setContentType("text/html");
        
        // Add some CSS
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: Segoe UI, sans-serif; margin: 10px; }");
        styleSheet.addRule(".result-card { border: 1px solid #e2e8f0; background-color: #ffffff; padding: 10px; margin-bottom: 10px; border-radius: 5px; }");
        styleSheet.addRule(".similarity { color: #2ecc71; font-weight: bold; }");
        styleSheet.addRule("a { color: #4a6dff; text-decoration: none; }");
        resultsPane.setEditorKit(kit);

        resultsPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                handleNavigation(e.getDescription());
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsPane);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void performSearch() {
        String query = inputArea.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a sentence to search.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double threshold = (double) thresholdSpinner.getValue();
        
        // Show loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Run in background thread to avoid freezing UI
        new Thread(() -> {
            List<SentenceSearchResult> results = facade.performSimilaritySearch(query, threshold);
            
            // Update UI on EDT
            javax.swing.SwingUtilities.invokeLater(() -> {
                displayResults(results);
                setCursor(Cursor.getDefaultCursor());
            });
        }).start();
    }

    private void displayResults(List<SentenceSearchResult> results) {
        StringBuilder html = new StringBuilder("<html><body>");
        
        if (results.isEmpty()) {
            html.append("<div style='padding: 20px; text-align: center; color: #718096;'>No results found matching the criteria.</div>");
        } else {
            html.append("<div style='margin-bottom: 10px;'>Found <b>").append(results.size()).append("</b> matches:</div>");
            
            for (SentenceSearchResult result : results) {
                html.append("<div class='result-card'>");
                html.append("<div><b>Text:</b> ").append(result.getSentence().getText()).append("</div>");
                if (result.getSentence().getTextDiacritized() != null && !result.getSentence().getTextDiacritized().isEmpty()) {
                    html.append("<div style='color: #4a5568; font-size: 0.9em;'><i>").append(result.getSentence().getTextDiacritized()).append("</i></div>");
                }
                html.append("<div style='margin-top: 5px;'>");
                html.append("Similarity: <span class='similarity'>").append(String.format("%.2f", result.getSimilarityPercentage())).append("%</span>");
                html.append(" | <a href='/book/").append(getBookIdForChapter(result.getSentence().getChapterId()))
                    .append("/chapter/").append(result.getSentence().getChapterId())
                    .append("/sentence/").append(result.getSentence().getSentenceId())
                    .append("'>Go to Sentence</a>");
                html.append("</div>");
                html.append("</div>");
            }
        }
        
        html.append("</body></html>");
        resultsPane.setText(html.toString());
        resultsPane.setCaretPosition(0);
    }
    
    // Helper to get book ID from chapter ID (assuming we don't have it directly in Sentence)
    // Since Sentence only has chapterId, we might need to fetch the chapter to get bookId.
    // However, for navigation URL, we need it.
    // Let's assume we can get it via Facade or just pass 0 if not critical for lookup (depending on how we implement navigation).
    // Actually, let's fetch it properly.
    private int getBookIdForChapter(int chapterId) {
        // This is a bit inefficient doing it for every result, but acceptable for UI list.
        // Ideally Sentence should have bookId or we fetch it in bulk.
        // For now, let's try to get it from Facade if possible, or just use a placeholder if the navigation logic can handle it.
        // The requirement says: /book/{bookId}/chapter/{chapterId}/sentence/{sentenceId}
        // I'll implement a helper in mainUI or Facade to get BookID by ChapterID.
        // For now, I'll assume I can get it.
        // Wait, I don't have a direct method in Facade to get BookID by ChapterID.
        // I can use retrieveChapter(bookName, chapterId) but I don't have bookName.
        // I might need to add a method to Facade or just iterate books.
        // Let's check if Chapter object has bookId.
        return mainUI.getBookIdByChapterId(chapterId); 
    }

    private void handleNavigation(String url) {
        // URL format: /book/{bookId}/chapter/{chapterId}/sentence/{sentenceId}
        String[] parts = url.split("/");
        if (parts.length >= 7) {
            try {
                int bookId = Integer.parseInt(parts[2]);
                int chapterId = Integer.parseInt(parts[4]);
                int sentenceId = Integer.parseInt(parts[6]);
                
                mainUI.navigateToSentence(bookId, chapterId, sentenceId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
    public void setQuery(String text) {
        inputArea.setText(text);
    }
}
