package PresentationLayerPL;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import BuisnessLayerBL.IBusinessFacade;
import ModelDTO.Book;
import ModelDTO.Chapter;
import PresentationLayerPL.ArabicProseUserInterface.RoundedButton;

public class FrequencyPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Colors matching Main UI
    private static final Color PRIMARY_COLOR = new Color(74, 109, 255);
    // Unused: private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(45, 55, 72);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 252);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);

    private IBusinessFacade facade;
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // State
    private Book selectedBook;
    private Chapter selectedChapter;
    private String currentMode; // "CHAPTER" or "BOOK"

    // Card Names
    private static final String HOME_VIEW = "HOME";
    private static final String BOOK_SELECTION_VIEW = "BOOK_SELECTION";
    private static final String CHAPTER_SELECTION_VIEW = "CHAPTER_SELECTION";
    private static final String OPTIONS_VIEW = "OPTIONS";
    private static final String RESULTS_VIEW = "RESULTS";

    public FrequencyPanel(IBusinessFacade facade) {
        this.facade = facade;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(BACKGROUND_COLOR);

        // Add cards
        mainContainer.add(createHomeView(), HOME_VIEW);
        mainContainer.add(createBookSelectionView(), BOOK_SELECTION_VIEW);
        mainContainer.add(createChapterSelectionView(), CHAPTER_SELECTION_VIEW);
        mainContainer.add(createOptionsView(), OPTIONS_VIEW);
        mainContainer.add(createResultsView(), RESULTS_VIEW);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createHomeView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("Frequency Analysis");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_COLOR);
        panel.add(title, gbc);

        gbc.gridy++;
        RoundedButton chapterLevelBtn = new RoundedButton("Frequency at Chapter Level", PRIMARY_COLOR);
        chapterLevelBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chapterLevelBtn.setPreferredSize(new java.awt.Dimension(300, 60));
        chapterLevelBtn.addActionListener(e -> startChapterLevelFlow());
        panel.add(chapterLevelBtn, gbc);

        gbc.gridy++;
        RoundedButton bookLevelBtn = new RoundedButton("Frequency at Book Level", SUCCESS_COLOR);
        bookLevelBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bookLevelBtn.setPreferredSize(new java.awt.Dimension(300, 60));
        bookLevelBtn.addActionListener(e -> startBookLevelFlow());
        panel.add(bookLevelBtn, gbc);

        return panel;
    }

    // --- SELECTION VIEWS ---

    private JTable bookTable;
    private DefaultTableModel bookTableModel;

    private JPanel createBookSelectionView() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Select a Book");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(header, BorderLayout.NORTH);

        bookTableModel = new DefaultTableModel(new Object[] { "Book ID", "Title", "Author", "Era" }, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        bookTable = new JTable(bookTableModel);
        styleTable(bookTable);
        
        // Double click to select
        bookTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    processBookSelection();
                }
            }
        });

        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(BACKGROUND_COLOR);
        RoundedButton nextBtn = new RoundedButton("Next", PRIMARY_COLOR);
        nextBtn.addActionListener(e -> processBookSelection());
        RoundedButton backBtn = new RoundedButton("Back", Color.GRAY);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, HOME_VIEW));

        btnPanel.add(backBtn);
        btnPanel.add(nextBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    // Helper to refresh book table
    private void refreshBookTable() {
        bookTableModel.setRowCount(0);
        List<Book> books = facade.retrieveAllBooks();
        for (Book b : books) {
            String authorName = facade.getAuthorByID(b.getAuthorId());
            bookTableModel.addRow(new Object[] { b.getBookId(), b.getTitle(), authorName, b.getEra() });
        }
    }

    private JTable chapterTable;
    private DefaultTableModel chapterTableModel;

    private JPanel createChapterSelectionView() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Select a Chapter");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(header, BorderLayout.NORTH);

        chapterTableModel = new DefaultTableModel(new Object[] { "Chapter ID", "Chapter Name" }, 0) {
           public boolean isCellEditable(int row, int col) { return false; } 
        };
        chapterTable = new JTable(chapterTableModel);
        styleTable(chapterTable);

        chapterTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && chapterTable.getSelectedRow() != -1) {
                    processChapterSelection();
                }
            }
        });

        panel.add(new JScrollPane(chapterTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(BACKGROUND_COLOR);
        RoundedButton nextBtn = new RoundedButton("Next", PRIMARY_COLOR);
        nextBtn.addActionListener(e -> processChapterSelection());
        RoundedButton backBtn = new RoundedButton("Back", Color.GRAY);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, BOOK_SELECTION_VIEW));

        btnPanel.add(backBtn);
        btnPanel.add(nextBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOptionsView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; 
        gbc.gridy = 0;
        
        JLabel header = new JLabel("Select Frequency Type");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(header, gbc);

        gbc.gridy++;
        RoundedButton tokenBtn = new RoundedButton("Frequency of Tokens", PRIMARY_COLOR);
        tokenBtn.setPreferredSize(new java.awt.Dimension(250, 50));
        tokenBtn.addActionListener(e -> calculateResults("TOKENS"));
        panel.add(tokenBtn, gbc);

        gbc.gridy++;
        RoundedButton lemmaBtn = new RoundedButton("Frequency of Lemmas", SUCCESS_COLOR);
        lemmaBtn.setPreferredSize(new java.awt.Dimension(250, 50));
        lemmaBtn.addActionListener(e -> calculateResults("LEMMAS"));
        panel.add(lemmaBtn, gbc);

        gbc.gridy++;
        RoundedButton rootBtn = new RoundedButton("Frequency of Roots", new Color(255, 165, 0)); // Orange
        rootBtn.setPreferredSize(new java.awt.Dimension(250, 50));
        rootBtn.addActionListener(e -> calculateResults("ROOTS"));
        panel.add(rootBtn, gbc);

        gbc.gridy++;
        RoundedButton backBtn = new RoundedButton("Back", Color.GRAY);
        backBtn.addActionListener(e -> goBackFromOptions());
        panel.add(backBtn, gbc);

        return panel;
    }
    
    // --- RESULTS VIEW ---
    private JTable resultsTable;
    private DefaultTableModel resultsModel;
    private JLabel resultsTitle;

    private JPanel createResultsView() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        resultsTitle = new JLabel("Analysis Results");
        resultsTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(resultsTitle, BorderLayout.NORTH);

        resultsModel = new DefaultTableModel();
        resultsTable = new JTable(resultsModel);
        styleTable(resultsTable);
        
        panel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        RoundedButton backBtn = new RoundedButton("Back to Options", Color.GRAY);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, OPTIONS_VIEW));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(BACKGROUND_COLOR);
        btnPanel.add(backBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- LOGIC ---

    private void startChapterLevelFlow() {
        currentMode = "CHAPTER";
        refreshBookTable();
        cardLayout.show(mainContainer, BOOK_SELECTION_VIEW);
    }

    private void startBookLevelFlow() {
        currentMode = "BOOK";
        refreshBookTable();
        cardLayout.show(mainContainer, BOOK_SELECTION_VIEW);
    }

    private void processBookSelection() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookId = (int) bookTableModel.getValueAt(row, 0);
        selectedBook = facade.retrieveBook(bookId);

        if (currentMode.equals("CHAPTER")) {
            // Load chapters
            chapterTableModel.setRowCount(0);
            List<Chapter> chapters = facade.retrieveChaptersByBook(selectedBook.getTitle()); // Expecting ByBook needs bookName? Or ID?
            // facade.retrieveChaptersByBook signature in Facade uses String bookName or int? 
            // In BusinessFacade: retrieveChaptersByBook(String bookName).
            // But DAL can do ID. Let's use name.
            
            if (chapters != null) {
                for (Chapter c : chapters) {
                    chapterTableModel.addRow(new Object[] { c.getChapterId(), c.getChapterName() });
                }
            }
            cardLayout.show(mainContainer, CHAPTER_SELECTION_VIEW);
        } else {
            // Book Mode -> Go to Options directly
            cardLayout.show(mainContainer, OPTIONS_VIEW);
        }
    }

    private void processChapterSelection() {
        int row = chapterTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a chapter.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int chapterId = (int) chapterTableModel.getValueAt(row, 0);
        selectedChapter = facade.retrieveChapter(chapterId);
        
        cardLayout.show(mainContainer, OPTIONS_VIEW);
    }

    private void goBackFromOptions() {
        if (currentMode.equals("CHAPTER")) {
            cardLayout.show(mainContainer, CHAPTER_SELECTION_VIEW);
        } else {
            cardLayout.show(mainContainer, BOOK_SELECTION_VIEW);
        }
    }

    private void calculateResults(String type) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            resultsModel.setRowCount(0);
            resultsModel.setColumnCount(0);

            if (currentMode.equals("CHAPTER")) {
                calculateChapterResults(type);
            } else {
                calculateBookResults(type);
            }
            
            cardLayout.show(mainContainer, RESULTS_VIEW);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void calculateChapterResults(String type) {
        int chapterId = selectedChapter.getChapterId();
        String title = "Frequency of " + type + " in Chapter: " + selectedChapter.getChapterName();
        resultsTitle.setText(title);

        resultsModel.addColumn(type.substring(0, 1) + type.substring(1).toLowerCase());
        resultsModel.addColumn("Frequency");

        Map<String, Integer> freqMap;
        List<String> allItems;

        switch (type) {
            case "TOKENS":
                freqMap = facade.getTokenFrequencyInChapter(chapterId);
                allItems = facade.getAllDistinctTokens();
                break;
            case "LEMMAS":
                freqMap = facade.getLemmaFrequencyInChapter(chapterId);
                allItems = facade.getAllDistinctLemmas();
                break;
            case "ROOTS":
                freqMap = facade.getRootFrequencyInChapter(chapterId);
                allItems = facade.getAllRoots();
                break;
            default:
                return;
        }

        // Merge logic
        for (String item : allItems) {
            if (item == null || item.trim().isEmpty()) continue;
            int count = freqMap.getOrDefault(item, 0);
            resultsModel.addRow(new Object[]{item, count});
        }
    }

    private void calculateBookResults(String type) {
        int bookId = selectedBook.getBookId();
        String title = "Frequency of " + type + " in Book: " + selectedBook.getTitle();
        resultsTitle.setText(title);

        resultsModel.addColumn(type.substring(0, 1) + type.substring(1).toLowerCase());
        resultsModel.addColumn("Total Frequency");
        resultsModel.addColumn("Chapter Breakdown");

        Map<String, Integer> totalMap;
        Map<String, Map<String, Integer>> breakdownMap;
        List<String> allItems;

        switch (type) {
            case "TOKENS":
                totalMap = facade.getTokenFrequencyInBook(bookId);
                breakdownMap = facade.getTokenFrequencyBreakdownByBook(bookId);
                allItems = facade.getAllDistinctTokens();
                break;
            case "LEMMAS":
                totalMap = facade.getLemmaFrequencyInBook(bookId);
                breakdownMap = facade.getLemmaFrequencyBreakdownByBook(bookId);
                allItems = facade.getAllDistinctLemmas();
                break;
            case "ROOTS":
                totalMap = facade.getRootFrequencyInBook(bookId);
                breakdownMap = facade.getRootFrequencyBreakdownByBook(bookId);
                allItems = facade.getAllRoots();
                break;
            default:
                return;
        }

        for (String item : allItems) {
            if (item == null || item.trim().isEmpty()) continue;
            int total = totalMap.getOrDefault(item, 0);
            
            StringBuilder breakdownStr = new StringBuilder();
            Map<String, Integer> chapters = breakdownMap.get(item);
            if (chapters != null) {
                // Sort by chapter name if possible, or just iterate
                TreeMap<String, Integer> sortedChapters = new TreeMap<>(chapters);
                for (Map.Entry<String, Integer> entry : sortedChapters.entrySet()) {
                    breakdownStr.append(entry.getKey()).append(": ").append(entry.getValue()).append(";  ");
                }
            } else {
                breakdownStr.append("-");
            }

            resultsModel.addRow(new Object[]{item, total, breakdownStr.toString()});
        }
        
        // Adjust column width for third column to be wider
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(400);
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(new Color(220, 237, 255));
    }
}
