package PresentationLayerPL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import BuisnessLayerBL.IBusinessFacade;
import ModelDTO.TokenData;

/**
 * Panel that lets users browse tokens grouped by their extracted root.
 */
public class RootBrowsePanel extends JPanel {
    private final IBusinessFacade facade;
    private final JComboBox<String> rootsComboBox;
    private final JPanel tokensContainer;
    private final JLabel statusLabel;

    public RootBrowsePanel(IBusinessFacade facade) {
        this.facade = facade;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(248, 249, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Browse Tokens by Root");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(45, 55, 72));

        JLabel subtitle = new JLabel("Select a root to list all tokens mapped to it");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(113, 128, 150));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        rootsComboBox = new JComboBox<>();
        rootsComboBox.setPreferredSize(new Dimension(250, 32));
        rootsComboBox.addActionListener(e -> {
            String root = (String) rootsComboBox.getSelectedItem();
            if (root != null && !root.isBlank()) {
                loadTokensForRoot(root);
            }
        });

        JButton refreshButton = new JButton("Refresh Roots");
        refreshButton.addActionListener(e -> loadRoots());

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.add(new JLabel("Root:"));
        controls.add(rootsComboBox);
        controls.add(refreshButton);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titlePanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        tokensContainer = new JPanel();
        tokensContainer.setOpaque(false);
        tokensContainer.setLayout(new GridLayout(0, 1, 10, 10));

        JScrollPane scrollPane = new JScrollPane(tokensContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        statusLabel = new JLabel("Select a root to begin.");
        statusLabel.setForeground(new Color(113, 128, 150));

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadRoots();
    }

    private void loadRoots() {
        setControlsEnabled(false);
        statusLabel.setText("Loading roots...");
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return facade.getAllRoots();
            }

            @Override
            protected void done() {
                try {
                    List<String> roots = get();
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                    for (String root : roots) {
                        model.addElement(root);
                    }
                    rootsComboBox.setModel(model);
                    statusLabel.setText(roots.isEmpty() ? "No roots found in database." : "Select a root to view tokens.");
                    if (!roots.isEmpty()) {
                        SwingUtilities.invokeLater(() -> rootsComboBox.setSelectedIndex(0));
                    } else {
                        tokensContainer.removeAll();
                        tokensContainer.add(createPlaceholder("Start by extracting roots for tokens."));
                        tokensContainer.revalidate();
                        tokensContainer.repaint();
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load roots: " + ex.getMessage());
                } finally {
                    setControlsEnabled(true);
                }
            }
        }.execute();
    }

    private void setControlsEnabled(boolean enabled) {
        rootsComboBox.setEnabled(enabled);
    }

    private void loadTokensForRoot(String root) {
        statusLabel.setText("Loading tokens for root '" + root + "'...");
        tokensContainer.removeAll();
        tokensContainer.add(createPlaceholder("Loading..."));
        tokensContainer.revalidate();
        tokensContainer.repaint();

        new SwingWorker<List<TokenData>, Void>() {
            @Override
            protected List<TokenData> doInBackground() {
                return facade.getTokensByRoot(root);
            }

            @Override
            protected void done() {
                try {
                    List<TokenData> tokens = get();
                    tokensContainer.removeAll();
                    if (tokens.isEmpty()) {
                        tokensContainer.add(createPlaceholder("No tokens currently mapped to root '" + root + "'."));
                        statusLabel.setText("No tokens found for selected root.");
                    } else {
                        for (TokenData token : tokens) {
                            tokensContainer.add(new TokenCardPanel(token));
                        }
                        statusLabel.setText("Showing " + tokens.size() + " tokens.");
                    }
                } catch (Exception ex) {
                    tokensContainer.removeAll();
                    tokensContainer.add(createPlaceholder("Error loading tokens: " + ex.getMessage()));
                    statusLabel.setText("Failed to load tokens for root.");
                } finally {
                    tokensContainer.revalidate();
                    tokensContainer.repaint();
                }
            }
        }.execute();
    }

    private JLabel createPlaceholder(String text) {
        JLabel placeholder = new JLabel(text, JLabel.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        placeholder.setForeground(new Color(113, 128, 150));
        placeholder.setBorder(BorderFactory.createDashedBorder(new Color(226, 232, 240)));
        placeholder.setPreferredSize(new Dimension(200, 80));
        return placeholder;
    }
}

