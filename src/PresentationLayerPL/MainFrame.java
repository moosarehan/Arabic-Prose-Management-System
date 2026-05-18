package PresentationLayerPL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import BuisnessLayerBL.IBusinessFacade;
import BuisnessLayerBL.ITokenOccurrenceBo;

/**
 * Lightweight navigation frame that hosts different browsing panels.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public MainFrame(IBusinessFacade facade, ITokenOccurrenceBo tokenOccurrenceBo) {
        super("Arabic Token Browser");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        rootPanel.setBackground(new Color(248, 249, 252));

        JLabel header = new JLabel("Token Exploration Workspace", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(new Color(45, 55, 72));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse Tokens (Search)", new TokenOccurrencePanel(tokenOccurrenceBo, facade));
            tabs.addTab("Browse by Tokens", new TokenBrowsePanel(facade, tokenOccurrenceBo));
            tabs.addTab("Browse by Lemmas", new LemmaBrowsePanel(facade, tokenOccurrenceBo));
            tabs.addTab("Browse by Segments", new SegmentBrowsePanel(facade, tokenOccurrenceBo));
        tabs.addTab("Browse by Root", new RootBrowsePanel(facade));

        rootPanel.add(header, BorderLayout.NORTH);
        rootPanel.add(tabs, BorderLayout.CENTER);
        add(rootPanel);
    }
}

