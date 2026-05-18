package PresentationLayerPL;

import BuisnessLayerBL.IBusinessFacade;
import BuisnessLayerBL.ITokenOccurrenceBo;
import ModelDTO.TokenData;
import util.TextHighlighter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class LemmaBrowsePanel extends JPanel {
    private final IBusinessFacade facade;
    private final ITokenOccurrenceBo tokenOccurrenceBo;
    private JComboBox<String> lemmaDropdown;
    private JTextPane resultsPane;
    private JLabel statusLabel;

    public LemmaBrowsePanel(IBusinessFacade facade, ITokenOccurrenceBo tokenOccurrenceBo) {
        this.facade = facade;
        this.tokenOccurrenceBo = tokenOccurrenceBo;
        initUI();
        loadLemmas();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(new EmptyBorder(8,8,8,8));

        lemmaDropdown = new JComboBox<>();
        lemmaDropdown.setPrototypeDisplayValue("................");
        lemmaDropdown.addActionListener(this::onLemmaSelected);

        JButton refreshBtn = new JButton("Refresh Lemmas");
        refreshBtn.addActionListener(e -> loadLemmas());

        top.add(new JLabel("Lemma:"));
        top.add(lemmaDropdown);
        top.add(refreshBtn);

        resultsPane = new JTextPane();
        resultsPane.setContentType("text/html");
        resultsPane.setEditable(false);

        statusLabel = new JLabel(" ");

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(resultsPane), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadLemmas() {
        lemmaDropdown.removeAllItems();
        statusLabel.setText("Loading lemmas...");
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return facade.getAllDistinctLemmas();
            }

            @Override
            protected void done() {
                try {
                    List<String> lemmas = get();
                    for (String l : lemmas) lemmaDropdown.addItem(l);
                    statusLabel.setText("Loaded " + lemmas.size() + " lemmas.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load lemmas: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void onLemmaSelected(ActionEvent ev) {
        Object sel = lemmaDropdown.getSelectedItem();
        if (sel == null) return;
        String lemma = sel.toString();
        statusLabel.setText("Loading tokens for lemma '" + lemma + "'...");

        SwingWorker<List<TokenData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TokenData> doInBackground() throws Exception {
                return facade.getTokensByLemma(lemma);
            }

            @Override
            protected void done() {
                try {
                    List<TokenData> tokens = get();
                    displayTokens(tokens, lemma);
                    statusLabel.setText("Found " + tokens.size() + " tokens for lemma '" + lemma + "'.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load tokens: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayTokens(List<TokenData> tokens, String lemma) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI, Arial; font-size:12px;'>");
        for (TokenData t : tokens) {
            sb.append("<div style='padding:6px;border-bottom:1px solid #ddd;'>");
            sb.append("<b>Token:</b> ").append(TextHighlighter.escapeHtml(t.getTokenText()));
            sb.append(" &nbsp; <b>Sentence ID:</b> ").append(t.getSentenceId());
            sb.append(" &nbsp; <b>Position:</b> ").append(t.getPosition());
            if (t.getLemma() != null) sb.append(" &nbsp; <b>Lemma:</b> ").append(TextHighlighter.escapeHtml(t.getLemma()));
            sb.append("</div>");
        }
        sb.append("</body></html>");
        resultsPane.setText(sb.toString());
        resultsPane.setCaretPosition(0);
    }
}
