package PresentationLayerPL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ModelDTO.TokenData;

/**
 * Simple reusable card component to render token level details in browse views.
 */
public class TokenCardPanel extends JPanel {
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color PRIMARY_COLOR = new Color(74, 109, 255);
    private static final Color TEXT_COLOR = new Color(45, 55, 72);
    private static final Color SECONDARY_TEXT = new Color(113, 128, 150);

    public TokenCardPanel(TokenData tokenData) {
        setLayout(new BorderLayout(8, 4));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel tokenLabel = new JLabel(tokenData.getTokenText(), SwingConstants.LEFT);
        tokenLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tokenLabel.setForeground(PRIMARY_COLOR);

        JLabel lemmaLabel = new JLabel("Lemma: " + tokenData.getLemma());
        lemmaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lemmaLabel.setForeground(TEXT_COLOR);

        JLabel metadataLabel = new JLabel(
            String.format("Sentence ID: %d    Position: %d", tokenData.getSentenceId(), tokenData.getPosition())
        );
        metadataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        metadataLabel.setForeground(SECONDARY_TEXT);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        infoPanel.setOpaque(false);
        infoPanel.add(lemmaLabel);
        infoPanel.add(metadataLabel);

        add(tokenLabel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
    }
}

