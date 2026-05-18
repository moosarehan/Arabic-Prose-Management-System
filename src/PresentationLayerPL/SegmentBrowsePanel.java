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

public class SegmentBrowsePanel extends JPanel {
    private final IBusinessFacade facade;
    private final ITokenOccurrenceBo tokenOccurrenceBo;
    private JComboBox<String> segmentDropdown;
    private JTextPane resultsPane;
    private JLabel statusLabel;

    public SegmentBrowsePanel(IBusinessFacade facade, ITokenOccurrenceBo tokenOccurrenceBo) {
        this.facade = facade;
        this.tokenOccurrenceBo = tokenOccurrenceBo;
        initUI();
        loadSegments();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(new EmptyBorder(8,8,8,8));

        segmentDropdown = new JComboBox<>();
        segmentDropdown.setPrototypeDisplayValue("................");
        segmentDropdown.addActionListener(this::onSegmentSelected);

        JButton refreshBtn = new JButton("Refresh Segments");
        refreshBtn.addActionListener(e -> loadSegments());

        top.add(new JLabel("Segment:"));
        top.add(segmentDropdown);
        top.add(refreshBtn);

        resultsPane = new JTextPane();
        resultsPane.setContentType("text/html");
        resultsPane.setEditable(false);

        statusLabel = new JLabel(" ");

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(resultsPane), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadSegments() {
        segmentDropdown.removeAllItems();
        statusLabel.setText("Loading segments...");
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return facade.getAllDistinctSegments();
            }

            @Override
            protected void done() {
                try {
                    List<String> segments = get();
                    for (String s : segments) segmentDropdown.addItem(s);
                    statusLabel.setText("Loaded " + segments.size() + " segments.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load segments: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void onSegmentSelected(ActionEvent ev) {
        Object sel = segmentDropdown.getSelectedItem();
        if (sel == null) return;
        String segment = sel.toString();
        statusLabel.setText("Loading tokens for segment '" + segment + "'...");

        SwingWorker<List<TokenData>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TokenData> doInBackground() throws Exception {
                return facade.getTokensBySegment(segment);
            }

            @Override
            protected void done() {
                try {
                    List<TokenData> tokens = get();
                    displayTokens(tokens, segment);
                    statusLabel.setText("Found " + tokens.size() + " tokens for segment '" + segment + "'.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load tokens: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayTokens(List<TokenData> tokens, String segment) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI, Arial; font-size:12px;'>");
        sb.append("<h3>Tokens for segment: ").append(TextHighlighter.escapeHtml(segment)).append("</h3>");
        if (tokens.isEmpty()) {
            sb.append("<p>No tokens found.</p>");
        } else {
            sb.append("<ul>");
            for (TokenData t : tokens) {
                sb.append("<li>");
                sb.append(TextHighlighter.escapeHtml(t.getTokenText()));
                sb.append(" &nbsp; <small>(sentenceId: ").append(t.getSentenceId()).append(")</small>");
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("</body></html>");
        resultsPane.setText(sb.toString());
        resultsPane.setCaretPosition(0);
    }
}
