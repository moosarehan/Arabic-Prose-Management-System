package PresentationLayerPL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.JTextPane;

import BuisnessLayerBL.IBusinessFacade;
import BuisnessLayerBL.ITokenOccurrenceBo;
import util.TextHighlighter;

/**
 * Panel that lets users browse sentences by selecting a token from a dropdown.
 * Uses the exact same token-occurrence search logic as the existing Search Tokens.
 */
public class TokenBrowsePanel extends JPanel {
    private final IBusinessFacade facade;
    private final ITokenOccurrenceBo tokenOccurrenceBo;
    private final JComboBox<String> tokenDropdown;
    private final JTextPane resultsPane;
    private final JLabel statusLabel;

    public TokenBrowsePanel(IBusinessFacade facade, ITokenOccurrenceBo tokenOccurrenceBo) {
        this.facade = facade;
        this.tokenOccurrenceBo = tokenOccurrenceBo;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(248, 249, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Browse Sentences by Token");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(45, 55, 72));

        JLabel subtitle = new JLabel("Select a token to list all sentences that contain it (exact match)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(113, 128, 150));

        JPanel titlePanel = new JPanel(new GridLayout(2,1));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        tokenDropdown = new JComboBox<>();
        tokenDropdown.setPreferredSize(new Dimension(320, 30));
        tokenDropdown.addActionListener(e -> {
            String token = (String) tokenDropdown.getSelectedItem();
            if (token != null && !token.isBlank()) {
                loadSentencesForToken(token);
            }
        });

        JButton refreshButton = new JButton("Refresh Tokens");
        refreshButton.addActionListener(e -> loadTokens());

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.add(new JLabel("Token:"));
        controls.add(tokenDropdown);
        controls.add(refreshButton);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titlePanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        resultsPane = new JTextPane();
        resultsPane.setContentType("text/html");
        resultsPane.setEditable(false);

        JScrollPane scroll = new JScrollPane(resultsPane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        statusLabel = new JLabel("Select a token to begin.");
        statusLabel.setForeground(new Color(113, 128, 150));

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadTokens();
    }

    private void loadTokens() {
        setEnabledRecursively(false);
        statusLabel.setText("Loading tokens...");
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return facade.getAllDistinctTokens();
            }

            @Override
            protected void done() {
                try {
                    List<String> tokens = get();
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                    for (String t : tokens) model.addElement(t);
                    tokenDropdown.setModel(model);
                    statusLabel.setText(tokens.isEmpty() ? "No tokens found in database." : "Select a token to view sentences.");
                    if (!tokens.isEmpty()) {
                        SwingUtilities.invokeLater(() -> tokenDropdown.setSelectedIndex(0));
                    } else {
                        resultsPane.setText("<html><body><p style='color:#717f96'>No tokens available.</p></body></html>");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load tokens: " + ex.getMessage());
                    resultsPane.setText("<html><body>Error loading tokens: " + ex.getMessage() + "</body></html>");
                } finally {
                    setEnabledRecursively(true);
                }
            }
        }.execute();
    }

    private void loadSentencesForToken(String token) {
        statusLabel.setText("Loading sentences for '" + token + "'...");
        resultsPane.setText("<html><body><p style='color:#717f96'>Loading...</p></body></html>");

        new SwingWorker<List<Map<String,String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return tokenOccurrenceBo.getSentencesByToken(token);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> results = get();
                    displayResults(results, token);
                    statusLabel.setText("Found " + results.size() + " occurrences");
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    resultsPane.setText("<html><body>Error: " + ex.getMessage() + "</body></html>");
                }
            }
        }.execute();
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
                String highlighted = TextHighlighter.highlightToken(row.get("sentence_text"), token);
                html.append("<li style='margin-bottom: 10px;'>")
                    .append("<b>").append(row.getOrDefault("chapter_name", "")).append("</b><br>")
                    .append(highlighted).append("</li>");
            }
            html.append("</ul>");
        }
        html.append("</body></html>");
        resultsPane.setText(html.toString());
        resultsPane.setCaretPosition(0);
    }

    private void setEnabledRecursively(boolean enabled) {
        tokenDropdown.setEnabled(enabled);
    }
}
