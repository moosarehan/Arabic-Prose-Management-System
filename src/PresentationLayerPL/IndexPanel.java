package PresentationLayerPL;

import BuisnessLayerBL.IBusinessFacade;
import ModelDTO.TokenData;
import util.TextHighlighter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class IndexPanel extends JPanel {
    private final IBusinessFacade facade;
    private final ArabicProseUserInterface mainUI;

    private JList<String> itemList;
    private DefaultListModel<String> listModel;
    private JTextPane detailPane;
    private JLabel statusLabel;
    
    private enum Mode { TOKENS, LEMMAS, ROOTS }
    private Mode currentMode = Mode.TOKENS;

    public IndexPanel(IBusinessFacade facade, ArabicProseUserInterface mainUI) {
        this.facade = facade;
        this.mainUI = mainUI;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Control Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnTokens = new JButton("Index by Tokens");
        JButton btnLemmas = new JButton("Index by Lemmas");
        JButton btnRoots = new JButton("Index by Roots");

        btnTokens.addActionListener(e -> setMode(Mode.TOKENS));
        btnLemmas.addActionListener(e -> setMode(Mode.LEMMAS));
        btnRoots.addActionListener(e -> setMode(Mode.ROOTS));

        topPanel.add(btnTokens);
        topPanel.add(btnLemmas);
        topPanel.add(btnRoots);

        add(topPanel, BorderLayout.NORTH);

        // Split Pane: Left = List, Right = Details
        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onItemSelected(itemList.getSelectedValue());
            }
        });

        JScrollPane listScroll = new JScrollPane(itemList);
        listScroll.setPreferredSize(new Dimension(250, 0));

        detailPane = new JTextPane();
        detailPane.setContentType("text/html");
        detailPane.setEditable(false);
        detailPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                handleHyperlink(e.getDescription());
            }
        });
        JScrollPane detailScroll = new JScrollPane(detailPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, detailScroll);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Select a mode to begin.");
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setMode(Mode mode) {
        this.currentMode = mode;
        listModel.clear();
        detailPane.setText("");
        statusLabel.setText("Loading " + mode.toString().toLowerCase() + "...");

        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                List<String> results;
                switch (mode) {
                    case TOKENS: results = facade.getAllDistinctTokens(); break;
                    case LEMMAS: results = facade.getAllDistinctLemmas(); break;
                    case ROOTS: results = facade.getAllRoots(); break;
                    default: results = List.of();
                }
                // Ensure sorting (though DOAs usually sort, explicit sort is safer)
                Collections.sort(results);
                return results;
            }

            @Override
            protected void done() {
                try {
                    List<String> items = get();
                    listModel.addAll(items);
                    statusLabel.setText("Loaded " + items.size() + " unique " + mode.toString().toLowerCase() + ".");
                } catch (InterruptedException | ExecutionException e) {
                    statusLabel.setText("Error loading items: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void onItemSelected(String selectedItem) {
        if (selectedItem == null) return;
        
        statusLabel.setText("Loading details for: " + selectedItem);
        // Display generic loading message
        detailPane.setText("<html><body><h3>Loading details for " + selectedItem + "...</h3></body></html>");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return buildDetailHtml(selectedItem);
            }

            @Override
            protected void done() {
                try {
                    String html = get();
                    detailPane.setText(html);
                    detailPane.setCaretPosition(0);
                    statusLabel.setText("Details loaded for: " + selectedItem);
                } catch (Exception e) {
                    detailPane.setText("<html><body><h3>Error loading details</h3><p>" + e.getMessage() + "</p></body></html>");
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private String buildDetailHtml(String item) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI, sans-serif; padding:10px;'>");
        
        if (currentMode == Mode.TOKENS) {
            sb.append("<h2>Token: ").append(item).append("</h2>");
            // Direct sentences display
            sb.append(buildTokenSentencesHtml(item)); 
        } else if (currentMode == Mode.LEMMAS) {
            sb.append("<h2>Lemma: ").append(item).append("</h2>");
            appendLemmaOccurrences(sb, item);
        } else if (currentMode == Mode.ROOTS) {
            sb.append("<h2>Root: ").append(item).append("</h2>");
            appendRootOccurrences(sb, item);
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    // --- Helper Logic for Content Generation ---

    private void appendRootOccurrences(StringBuilder sb, String root) {
        // Root -> Distinct Lemmas
        List<String> lemmas = facade.getLemmasByRoot(root);
        if (lemmas.isEmpty()) {
            sb.append("<p>No lemmas found for this root.</p>");
            return;
        }
        sb.append("<h3>Lemmas for root '").append(root).append("':</h3><ul>");
        for (String lemma : lemmas) {
            // Link to show tokens for this lemma (switching context to Lemma-like view)
            sb.append("<li><a href='lemma://").append(lemma).append("'>")
              .append(lemma).append("</a></li>");
        }
        sb.append("</ul>");
    }

    private void appendLemmaOccurrences(StringBuilder sb, String lemma) {
        // Lemma -> Tokens
        List<TokenData> tokens = facade.getTokensByLemma(lemma);
        if (tokens.isEmpty()) {
            sb.append("<p>No tokens found for this lemma.</p>");
            return;
        }
        sb.append("<h3>Tokens for lemma '").append(lemma).append("':</h3><ul>");
        for (TokenData t : tokens) {
            // Link to show sentences for this token
            sb.append("<li><a href='token://").append(t.getTokenText()).append("'>")
              .append(t.getTokenText()).append("</a> (Sentence ID: ").append(t.getSentenceId()).append(")</li>");
        }
        sb.append("</ul>");
    }

    private String buildTokenSentencesHtml(String token) {
         StringBuilder sb = new StringBuilder();
         List<Map<String, String>> sentences = facade.getSentencesByToken(token);
         
         if (sentences.isEmpty()) {
             sb.append("<p>No sentences found for token '").append(token).append("'.</p>");
         } else {
             sb.append("<h3>Occurrences of '").append(token).append("':</h3>");
             sb.append("<ul>");
             for (Map<String, String> row : sentences) {
                 String text = row.get("sentence_text");
                 String bookTitle = row.get("book_title");
                 String chapterName = row.get("chapter_name");
                 String sentenceId = row.get("sentence_id");
                 String bookId = row.get("book_id");
                 String chapterId = row.get("chapter_id");
                 
                 // Required link format: /book/{bookId}/chapter/{chapterId}/sentence/{sentenceId}
                 String link = "/book/" + bookId + "/chapter/" + chapterId + "/sentence/" + sentenceId;
                 
                 sb.append("<li style='margin-bottom: 8px;'>");
                 sb.append("<b>").append(bookTitle).append(" / ").append(chapterName).append("</b><br>");
                 // Highlight token in sentence
                 sb.append(TextHighlighter.highlightToken(text, token));
                 sb.append(" <br><a href='").append(link).append("'>[Go to Sentence]</a>");
                 sb.append("</li>");
             }
             sb.append("</ul>");
         }
         return sb.toString();
    }

    // --- Hyperlink Handling ---

    private void handleHyperlink(String url) {
        if (url.startsWith("token://")) {
            String token = url.substring(8);
            showSentencesForToken(token);
        } else if (url.startsWith("lemma://")) {
            String lemma = url.substring(8);
            showTokensForLemma(lemma);
        } else if (url.startsWith("/book/")) {
             // /book/{bookId}/chapter/{chapterId}/sentence/{sentenceId}
             String[] parts = url.split("/");
             if (parts.length >= 7) {
                 try {
                     int bookId = Integer.parseInt(parts[2]);
                     int chapterId = Integer.parseInt(parts[4]);
                     int sentenceId = Integer.parseInt(parts[6]);
                     mainUI.navigateToSentence(bookId, chapterId, sentenceId);
                 } catch (NumberFormatException e) {
                     System.err.println("Invalid navigation ID: " + url);
                 }
             }
        }
    }
    
    private void showTokensForLemma(String lemma) {
        statusLabel.setText("Loading tokens for lemma: " + lemma);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body style='font-family:Segoe UI, sans-serif; padding:10px;'>");
                sb.append("<h2>Lemma: ").append(lemma).append("</h2>");
                appendLemmaOccurrences(sb, lemma);
                sb.append("</body></html>");
                return sb.toString();
            }
             @Override
            protected void done() {
                try {
                    String html = get();
                    detailPane.setText(html);
                    detailPane.setCaretPosition(0);
                    statusLabel.setText("Tokens loaded for lemma: " + lemma);
                } catch (Exception e) {
                    e.printStackTrace();
                    detailPane.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void showSentencesForToken(String token) {
        statusLabel.setText("Loading sentences for token: " + token);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body style='font-family:Segoe UI, sans-serif; padding:10px;'>");
                sb.append("<h2>Token: ").append(token).append("</h2>");
                sb.append(buildTokenSentencesHtml(token));
                sb.append("</body></html>");
                return sb.toString();
            }
             @Override
            protected void done() {
                try {
                    String html = get();
                    detailPane.setText(html);
                    detailPane.setCaretPosition(0);
                    statusLabel.setText("Sentences loaded for: " + token);
                } catch (Exception e) {
                    e.printStackTrace();
                    detailPane.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
