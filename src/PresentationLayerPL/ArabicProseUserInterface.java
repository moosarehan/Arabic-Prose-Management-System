
 package PresentationLayerPL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import BuisnessLayerBL.IBusinessFacade;
import ModelDTO.Author;
import ModelDTO.Book;
import ModelDTO.Chapter;
import ModelDTO.Root;
import ModelDTO.Sentence;
import ModelDTO.TokenData; // Add this import for Token DTO
import ModelDTO.TokenSegmentation;
import util.TextHighlighter;

public class ArabicProseUserInterface extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final Color PRIMARY_COLOR = new Color(74, 109, 255);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 252);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(45, 55, 72);
    private static final Color SECONDARY_TEXT = new Color(113, 128, 150);

    private final IBusinessFacade facade;

    private JTable bookTable;
    private DefaultTableModel bookTableModel;
    private JTable authorTable;
    private DefaultTableModel authorTableModel;
    private JTextPane resultsPane;
    private JTabbedPane tabbedPane;
    private SimilaritySearchPanel similaritySearchPanel;
    public ArabicProseUserInterface(IBusinessFacade facade) {
        this.facade = facade;
        initUI();
    }

    private void initUI() {
        setTitle("Arabic Prose Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        resultsPane = new JTextPane();
        resultsPane.setContentType("text/html");
        resultsPane.setEditable(false);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(248, 249, 252),
                        0, getHeight(), new Color(240, 242, 245));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header with icon and title
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);

        tabbedPane.add("Books Management", createBooksPanel());
        tabbedPane.add("Authors Management", createAuthorsPanel());
        similaritySearchPanel = new SimilaritySearchPanel(facade, this);
        tabbedPane.add("Similarity Search", similaritySearchPanel);
        tabbedPane.add("Index", new IndexPanel(facade, this));
        tabbedPane.add("Frequency Analysis", new FrequencyPanel(facade));
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel header = new JLabel("Arabic Prose Management System", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 32));
        header.setForeground(TEXT_COLOR);

        JLabel subtitle = new JLabel("Manage Books, Authors, Chapters and Sentences", SwingConstants.LEFT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(SECONDARY_TEXT);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(header);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);

        // Table setup
        bookTableModel = new DefaultTableModel(new Object[]{"Title", "Author", "Era"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(bookTableModel);
        styleTable(bookTable);
        refreshBookTable();

        // Double-click to open chapter dialog
        bookTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    String title = (String) bookTableModel.getValueAt(bookTable.getSelectedRow(), 0);
                    Book book = facade.retrieveBook(title);
                    if (book != null)
                        showChapterDialog(book, -1, -1);
                }
            }
        });

        JPanel tablePanel = createCardPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(createTableHeader("Books List", "Double-click on a book to manage its chapters"), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(createBookButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAuthorsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);

        authorTableModel = new DefaultTableModel(new Object[]{"Name", "Biography"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        authorTable = new JTable(authorTableModel);
        styleTable(authorTable);
        refreshAuthorTable();

        JPanel tablePanel = createCardPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(createTableHeader("Authors List", "Manage authors and their biographies"), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(authorTable), BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(createAuthorButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBookButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(BACKGROUND_COLOR);

        RoundedButton addBtn = new RoundedButton("Add Book", SUCCESS_COLOR);
        RoundedButton updateBtn = new RoundedButton("Update Book", WARNING_COLOR);
        RoundedButton deleteBtn = new RoundedButton("Delete Book", DANGER_COLOR);
        RoundedButton retrieveBtn = new RoundedButton("Retrieve Book", PRIMARY_COLOR);
        RoundedButton refreshBtn = new RoundedButton("Refresh", PRIMARY_COLOR);

        addBtn.addActionListener(e -> showAddBookDialog());
        updateBtn.addActionListener(e -> showUpdateBookDialog());
        deleteBtn.addActionListener(e -> deleteSelectedBook());
        retrieveBtn.addActionListener(e -> retrieveSelectedBook());
        refreshBtn.addActionListener(e -> refreshBookTable());

        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(retrieveBtn);
        panel.add(refreshBtn);
        return panel;
    }

    private JPanel createAuthorButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(BACKGROUND_COLOR);

        RoundedButton addBtn = new RoundedButton("Add Author", SUCCESS_COLOR);
        RoundedButton updateBtn = new RoundedButton("Update Author", WARNING_COLOR);
        RoundedButton deleteBtn = new RoundedButton("Delete Author", DANGER_COLOR);
        RoundedButton retrieveBtn = new RoundedButton("Retrieve Author", PRIMARY_COLOR);
        RoundedButton refreshBtn = new RoundedButton("Refresh", PRIMARY_COLOR);

        addBtn.addActionListener(e -> showAddAuthorDialog());
        updateBtn.addActionListener(e -> showUpdateAuthorDialog());
        deleteBtn.addActionListener(e -> deleteSelectedAuthor());
        retrieveBtn.addActionListener(e -> retrieveSelectedAuthor());
        refreshBtn.addActionListener(e -> refreshAuthorTable());

        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(retrieveBtn);
        panel.add(refreshBtn);
        return panel;
    }

    // BOOK CRUD OPERATIONS
    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(25);
        JTextField eraField = new JTextField(25);
        JComboBox<String> authorBox = new JComboBox<>();

        // Populate authors
        List<Author> authors = facade.retrieveAllAuthors();
        if (authors != null) {
            for (Author author : authors) {
                authorBox.addItem(author.getName());
            }
        }

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; dialog.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Era:"), gbc);
        gbc.gridx = 1; dialog.add(eraField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; dialog.add(authorBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        RoundedButton saveBtn = new RoundedButton("Save", SUCCESS_COLOR);
        RoundedButton cancelBtn = new RoundedButton("Cancel", DANGER_COLOR);

        saveBtn.addActionListener(e -> {
            if (!titleField.getText().trim().isEmpty() && authorBox.getSelectedItem() != null) {
                String authorName = (String) authorBox.getSelectedItem();
                boolean success = facade.addBook(titleField.getText().trim(), authorName,
                        eraField.getText().trim(), authorName);
                if (success) {
                    refreshBookTable();
                    dialog.dispose();
                    showSuccessMessage(this, "Book added successfully!");
                } else {
                    showErrorMessage(this, "Failed to add book!");
                }
            } else {
                showErrorMessage(this, "Please fill all required fields!");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showUpdateBookDialog() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(this, "Please select a book to update!");
            return;
        }

        String oldTitle = (String) bookTableModel.getValueAt(row, 0);
        Book selectedBook = facade.retrieveBook(oldTitle);
        if (selectedBook == null) {
            showErrorMessage(this, "Selected book not found!");
            return;
        }

        JDialog dialog = new JDialog(this, "Update Book", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(selectedBook.getTitle(), 25);
        JTextField eraField = new JTextField(selectedBook.getEra(), 25);
        JComboBox<String> authorBox = new JComboBox<>();

        // Populate authors
        List<Author> authors = facade.retrieveAllAuthors();
        if (authors != null) {
            for (Author author : authors) {
                authorBox.addItem(author.getName());
            }
            String currentAuthor = facade.getAuthorByID(selectedBook.getAuthorId());
            authorBox.setSelectedItem(currentAuthor);
        }

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; dialog.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Era:"), gbc);
        gbc.gridx = 1; dialog.add(eraField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; dialog.add(authorBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        RoundedButton saveBtn = new RoundedButton("Update", SUCCESS_COLOR);
        RoundedButton cancelBtn = new RoundedButton("Cancel", DANGER_COLOR);

        saveBtn.addActionListener(e -> {
            if (!titleField.getText().trim().isEmpty() && authorBox.getSelectedItem() != null) {
                String authorName = (String) authorBox.getSelectedItem();
                boolean success = facade.updateBook(oldTitle, titleField.getText().trim(),
                        authorName, eraField.getText().trim());
                if (success) {
                    refreshBookTable();
                    dialog.dispose();
                    showSuccessMessage(this, "Book updated successfully!");
                } else {
                    showErrorMessage(this, "Failed to update book!");
                }
            } else {
                showErrorMessage(this, "Please fill all required fields!");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(this, "Please select a book to delete!");
            return;
        }

        String bookTitle = (String) bookTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete book: '" + bookTitle + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = facade.deleteBook(bookTitle);
            if (success) {
                refreshBookTable();
                showSuccessMessage(this, "Book deleted successfully!");
            } else {
                showErrorMessage(this, "Failed to delete book!");
            }
        }
    }

    private void retrieveSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(this, "Please select a book to retrieve!");
            return;
        }

        String bookTitle = (String) bookTableModel.getValueAt(row, 0);
        Book book = facade.retrieveBook(bookTitle);
        if (book != null) {
            String authorName = facade.getAuthorByID(book.getAuthorId());
            String message = String.format(
                "Book Details:\n\nTitle: %s\nAuthor: %s\nEra: %s\nBook ID: %d",
                book.getTitle(), authorName, book.getEra(), book.getBookId()
            );
            showInfoMessage(this, message);
        } else {
            showErrorMessage(this, "Book not found!");
        }
    }

    // AUTHOR CRUD OPERATIONS
    private void showAddAuthorDialog() {
        JTextField nameField = new JTextField(20);
        JTextArea bioArea = new JTextArea(4, 20);
        JScrollPane bioScroll = new JScrollPane(bioArea);

        Object[] fields = {
            "Author Name:", nameField,
            "Biography:", bioScroll
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Add New Author",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opt == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            boolean success = facade.addAuthor(nameField.getText().trim(), bioArea.getText().trim());
            if (success) {
                refreshAuthorTable();
                showSuccessMessage(this, "Author added successfully!");
            } else {
                showErrorMessage(this, "Failed to add author!");
            }
        }
    }

    private void showUpdateAuthorDialog() {
        int row = authorTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(this, "Please select an author to update!");
            return;
        }

        String oldName = (String) authorTableModel.getValueAt(row, 0);
        Author author = facade.retrieveAuthor(oldName);
        if (author == null) {
            showErrorMessage(this, "Selected author not found!");
            return;
        }

        JTextField nameField = new JTextField(author.getName(), 20);
        JTextArea bioArea = new JTextArea(author.getBiography(), 4, 20);
        JScrollPane bioScroll = new JScrollPane(bioArea);

        Object[] fields = {
            "Author Name:", nameField,
            "Biography:", bioScroll
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Update Author",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opt == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            boolean success = facade.updateAuthor(oldName, nameField.getText().trim(), bioArea.getText().trim());
            if (success) {
                refreshAuthorTable();
                showSuccessMessage(this, "Author updated successfully!");
            } else {
                showErrorMessage(this, "Failed to update author!");
            }
        }
    }

    private void deleteSelectedAuthor() {
        int row = authorTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(this, "Please select an author to delete!");
            return;
        }

        String authorName = (String) authorTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete author: '" + authorName + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = facade.deleteAuthor(authorName);
            if (success) {
                refreshAuthorTable();
                showSuccessMessage(this, "Author deleted successfully!");
            } else {
                showErrorMessage(this, "Failed to delete author!");
            }
        }
    }

    private void retrieveSelectedAuthor() {
        int row = authorTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(this, "Please select an author to retrieve!");
            return;
        }

        String authorName = (String) authorTableModel.getValueAt(row, 0);
        Author author = facade.retrieveAuthor(authorName);
        if (author != null) {
            String message = String.format(
                "Author Details:\n\nName: %s\nBiography: %s\nAuthor ID: %d",
                author.getName(), author.getBiography(), author.getAuthorId()
            );
            showInfoMessage(this, message);
        } else {
            showErrorMessage(this, "Author not found!");
        }
    }

    // CHAPTER DIALOG
    private void showChapterDialog(Book book, int targetChapterId, int targetSentenceId) {
        JDialog dialog = new JDialog(this, "Chapters of: " + book.getTitle(), true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = createCardPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        DefaultTableModel chapterModel = new DefaultTableModel(new Object[]{"Chapter ID", "Chapter Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable chapterTable = new JTable(chapterModel);
        styleTable(chapterTable);
        refreshChapterTable(book, chapterModel);

        // Double-click to open sentence dialog
        chapterTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && chapterTable.getSelectedRow() != -1) {
                    int chapterId = (int) chapterModel.getValueAt(chapterTable.getSelectedRow(), 0);
                    Chapter chapter = facade.retrieveChapter(book.getTitle(), chapterId);
                    if (chapter != null)
                        showSentenceDialog(book, chapter, -1);
                }
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        JLabel headerLabel = new JLabel("Chapters in: " + book.getTitle());
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(chapterTable), BorderLayout.CENTER);

        // CRUD Buttons for Chapters
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_COLOR);

        RoundedButton addBtn = new RoundedButton("Add Chapter", SUCCESS_COLOR);
        RoundedButton updateBtn = new RoundedButton("Update Chapter", WARNING_COLOR);
        RoundedButton deleteBtn = new RoundedButton("Delete Chapter", DANGER_COLOR);
        RoundedButton importBtn = new RoundedButton("Import from File", PRIMARY_COLOR);
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);

        addBtn.addActionListener(e -> {
            JTextField chNameField = new JTextField(20);
            Object[] fields = {"Chapter Name:", chNameField};
            int opt = JOptionPane.showConfirmDialog(dialog, fields, "Add New Chapter",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt == JOptionPane.OK_OPTION && !chNameField.getText().trim().isEmpty()) {
                boolean success = facade.addChapter(book.getTitle(), chNameField.getText().trim());
                if (success) {
                    refreshChapterTable(book, chapterModel);
                    showSuccessMessage(dialog, "Chapter added successfully!");
                } else {
                    showErrorMessage(dialog, "Failed to add chapter!");
                }
            }
        });

        updateBtn.addActionListener(e -> {
            int row = chapterTable.getSelectedRow();
            if (row == -1) {
                showErrorMessage(dialog, "Please select a chapter first!");
                return;
            }
            int chapterId = (int) chapterModel.getValueAt(row, 0);
            String oldName = (String) chapterModel.getValueAt(row, 1);

            JTextField newNameField = new JTextField(oldName, 20);
            Object[] fields = {"New Chapter Name:", newNameField};
            int opt = JOptionPane.showConfirmDialog(dialog, fields, "Update Chapter",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt == JOptionPane.OK_OPTION && !newNameField.getText().trim().isEmpty()) {
                boolean success = facade.updateChapter(book.getTitle(), chapterId, newNameField.getText().trim());
                if (success) {
                    refreshChapterTable(book, chapterModel);
                    showSuccessMessage(dialog, "Chapter updated successfully!");
                } else {
                    showErrorMessage(dialog, "Failed to update chapter!");
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = chapterTable.getSelectedRow();
            if (row == -1) {
                showErrorMessage(dialog, "Please select a chapter to delete!");
                return;
            }
            int chapterId = (int) chapterModel.getValueAt(row, 0);
            String chapterName = (String) chapterModel.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete chapter: '" + chapterName + "'?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = facade.deleteChapter(book.getTitle(), chapterId);
                if (success) {
                    refreshChapterTable(book, chapterModel);
                    showSuccessMessage(dialog, "Chapter deleted successfully!");
                } else {
                    showErrorMessage(dialog, "Failed to delete chapter!");
                }
            }
        });

        importBtn.addActionListener(e -> showImportChapterDialog(book));
        closeBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(importBtn);
        btnPanel.add(closeBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        
        if (targetChapterId != -1) {
            for (int i = 0; i < chapterTable.getRowCount(); i++) {
                int id = (int) chapterModel.getValueAt(i, 0);
                if (id == targetChapterId) {
                    chapterTable.setRowSelectionInterval(i, i);
                    Chapter chapter = facade.retrieveChapter(book.getTitle(), targetChapterId);
                    if (chapter != null) {
                        showSentenceDialog(book, chapter, targetSentenceId);
                    }
                    break;
                }
            }
        }
        
        dialog.setVisible(true);
    }

    private void showSentenceDialog(Book book, Chapter chapter, int targetSentenceId) {
        JDialog dialog = new JDialog(this, "Sentences in: " + chapter.getChapterName(), true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = createCardPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        DefaultTableModel sentenceModel = new DefaultTableModel(
                new Object[]{"Sentence #", "Text", "Diacritized", "Translation", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable sentenceTable = new JTable(sentenceModel);
        styleTable(sentenceTable);

        // Refresh sentences for this chapter
        refreshSentenceTableForChapter(book, chapter, sentenceModel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        JLabel headerLabel = new JLabel("Sentences in Chapter: " + chapter.getChapterName() + " (Book: " + book.getTitle() + ")");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(sentenceTable), BorderLayout.CENTER);

        // CRUD Buttons for Sentences
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_COLOR);

        RoundedButton addBtn = new RoundedButton("Add Sentence", SUCCESS_COLOR);
        RoundedButton updateBtn = new RoundedButton("Update Sentence", WARNING_COLOR);
        RoundedButton deleteBtn = new RoundedButton("Delete Sentence", DANGER_COLOR);
        RoundedButton retrieveBtn = new RoundedButton("Retrieve Sentence", PRIMARY_COLOR);
        RoundedButton viewTokensBtn = new RoundedButton("View Tokens", PRIMARY_COLOR);
        RoundedButton viewLemmasBtn = new RoundedButton("View Lemmas", new Color(34, 139, 34));
        RoundedButton findSimilarBtn = new RoundedButton("Find Similar", new Color(74, 109, 255));
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);

        addBtn.addActionListener(e -> showAddSentenceDialog(book, chapter, sentenceModel, dialog));
        updateBtn.addActionListener(e -> showUpdateSentenceDialog(book, chapter, sentenceModel, dialog, sentenceTable));
        deleteBtn.addActionListener(e -> deleteSelectedSentence(book, chapter, sentenceModel, dialog, sentenceTable));
        retrieveBtn.addActionListener(e -> retrieveSelectedSentence(book, chapter, sentenceModel, dialog, sentenceTable));
        viewTokensBtn.addActionListener(e -> showTokensDialog(chapter, sentenceModel, dialog, sentenceTable));
        viewLemmasBtn.addActionListener(e -> showLemmasDialog(chapter, sentenceModel, dialog, sentenceTable));
        findSimilarBtn.addActionListener(e -> {
            int row = sentenceTable.getSelectedRow();
            if (row != -1) {
                String text = (String) sentenceModel.getValueAt(row, 1); // Column 1 is Text
                dialog.dispose();
                switchToSimilaritySearch(text);
            } else {
                showErrorMessage(dialog, "Please select a sentence first!");
            }
        });
        closeBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(retrieveBtn);
        btnPanel.add(viewTokensBtn);
        btnPanel.add(viewLemmasBtn);
        btnPanel.add(findSimilarBtn);
        btnPanel.add(closeBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        
        if (targetSentenceId != -1) {
             Sentence target = facade.retrieveSentenceById(targetSentenceId);
             if (target != null) {
                 int targetNum = target.getSentenceNumber();
                 for (int i = 0; i < sentenceTable.getRowCount(); i++) {
                     int num = (int) sentenceModel.getValueAt(i, 0);
                     if (num == targetNum) {
                         sentenceTable.setRowSelectionInterval(i, i);
                         sentenceTable.scrollRectToVisible(sentenceTable.getCellRect(i, 0, true));
                         break;
                     }
                 }
             }
        }
        
        dialog.setVisible(true);
    }

    private void showAddSentenceDialog(Book book, Chapter chapter, DefaultTableModel model, JDialog parent) {
        JDialog dialog = new JDialog(parent, "Add New Sentence", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField textField = new JTextField(25);
        JTextField diacritizedField = new JTextField(25);
        JTextField translationField = new JTextField(25);
        JTextArea notesArea = new JTextArea(3, 25);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Text:"), gbc);
        gbc.gridx = 1; dialog.add(textField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Diacritized:"), gbc);
        gbc.gridx = 1; dialog.add(diacritizedField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Translation:"), gbc);
        gbc.gridx = 1; dialog.add(translationField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; dialog.add(notesScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        RoundedButton saveBtn = new RoundedButton("Save", SUCCESS_COLOR);
        RoundedButton cancelBtn = new RoundedButton("Cancel", DANGER_COLOR);

        saveBtn.addActionListener(e -> {
            if (!textField.getText().trim().isEmpty()) {
                boolean success = facade.addSentence(
                    chapter.getChapterName(),
                    textField.getText().trim(),
                    diacritizedField.getText().trim(),
                    translationField.getText().trim(),
                    notesArea.getText().trim()
                );
                if (success) {
                    refreshSentenceTableForChapter(book, chapter, model);
                    dialog.dispose();
                    showSuccessMessage(parent, "Sentence added successfully!");
                } else {
                    showErrorMessage(parent, "Failed to add sentence!");
                }
            } else {
                showErrorMessage(parent, "Text is required!");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void showUpdateSentenceDialog(Book book, Chapter chapter, DefaultTableModel model, JDialog parent, JTable sentenceTable) {
        int row = sentenceTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(parent, "Please select a sentence to update!");
            return;
        }

        Object sentenceNumberObj = model.getValueAt(row, 0);
        if (!(sentenceNumberObj instanceof Integer)) {
            showErrorMessage(parent, "Invalid sentence number format!");
            return;
        }
        int sentenceNumber = (Integer) sentenceNumberObj;

        Sentence sentence = facade.retrieveSentence(chapter.getChapterName(), sentenceNumber);
        if (sentence == null) {
            showErrorMessage(parent, "Selected sentence not found!");
            return;
        }

        JDialog dialog = new JDialog(parent, "Update Sentence", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField textField = new JTextField(sentence.getText(), 25);
        JTextField diacritizedField = new JTextField(sentence.getTextDiacritized(), 25);
        JTextField translationField = new JTextField(sentence.getTranslation(), 25);
        JTextArea notesArea = new JTextArea(sentence.getNotes(), 3, 25);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Text:"), gbc);
        gbc.gridx = 1; dialog.add(textField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Diacritized:"), gbc);
        gbc.gridx = 1; dialog.add(diacritizedField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Translation:"), gbc);
        gbc.gridx = 1; dialog.add(translationField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; dialog.add(notesScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        RoundedButton saveBtn = new RoundedButton("Update", SUCCESS_COLOR);
        RoundedButton cancelBtn = new RoundedButton("Cancel", DANGER_COLOR);

        saveBtn.addActionListener(e -> {
            if (!textField.getText().trim().isEmpty()) {
                boolean success = facade.updateSentence(
                    chapter.getChapterName(),
                    sentenceNumber,
                    textField.getText().trim(),
                    diacritizedField.getText().trim(),
                    translationField.getText().trim(),
                    notesArea.getText().trim()
                );
                if (success) {
                    refreshSentenceTableForChapter(book, chapter, model);
                    dialog.dispose();
                    showSuccessMessage(parent, "Sentence updated successfully!");
                } else {
                    showErrorMessage(parent, "Failed to update sentence!");
                }
            } else {
                showErrorMessage(parent, "Text is required!");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void deleteSelectedSentence(Book book, Chapter chapter, DefaultTableModel model, JDialog parent, JTable sentenceTable) {
        int row = sentenceTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(parent, "Please select a sentence to delete!");
            return;
        }

        Object sentenceNumberObj = model.getValueAt(row, 0);
        if (!(sentenceNumberObj instanceof Integer)) {
            showErrorMessage(parent, "Invalid sentence number format!");
            return;
        }
        int sentenceNumber = (Integer) sentenceNumberObj;

        int confirm = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to delete sentence #" + sentenceNumber + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = facade.deleteSentence(chapter.getChapterName(), sentenceNumber);
            if (success) {
                refreshSentenceTableForChapter(book, chapter, model);
                showSuccessMessage(parent, "Sentence deleted successfully!");
            } else {
                showErrorMessage(parent, "Failed to delete sentence!");
            }
        }
    }

    private void retrieveSelectedSentence(Book book, Chapter chapter, DefaultTableModel model, JDialog parent, JTable sentenceTable) {
        int row = sentenceTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(parent, "Please select a sentence to retrieve!");
            return;
        }

        Object sentenceNumberObj = model.getValueAt(row, 0);
        if (!(sentenceNumberObj instanceof Integer)) {
            showErrorMessage(parent, "Invalid sentence number format!");
            return;
        }
        int sentenceNumber = (Integer) sentenceNumberObj;

        Sentence sentence = facade.retrieveSentence(chapter.getChapterName(), sentenceNumber);
        if (sentence != null) {
            String message = String.format(
                "Sentence Details:\n\nSentence #: %d\nText: %s\nDiacritized: %s\nTranslation: %s\nNotes: %s",
                sentence.getSentenceNumber(), sentence.getText(), sentence.getTextDiacritized(),
                sentence.getTranslation(), sentence.getNotes()
            );
            showInfoMessage(parent, message);
        } else {
            showErrorMessage(parent, "Sentence not found!");
        }
    }

    private void showTokensDialog(Chapter chapter, DefaultTableModel sentenceModel, JDialog parent, JTable sentenceTable) {
        int row = sentenceTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(parent, "Please select a sentence to view its tokens!");
            return;
        }

        Object sentenceNumberObj = sentenceModel.getValueAt(row, 0);
        if (!(sentenceNumberObj instanceof Integer)) {
            showErrorMessage(parent, "Invalid sentence number format!");
            return;
        }
        int sentenceNumber = (Integer) sentenceNumberObj;

        // Retrieve the sentence to get its sentence_id
        Sentence sentence = facade.retrieveSentence(chapter.getChapterName(), sentenceNumber);
        if (sentence == null) {
            showErrorMessage(parent, "Selected sentence not found!");
            return;
        }

        // Retrieve tokens for the sentence
        List<TokenData> tokens = facade.retrieveTokensBySentence(sentence.getSentenceId());
        if (tokens == null || tokens.isEmpty()) {
            showInfoMessage(parent, "No tokens found for this sentence!");
            return;
        }
        final List<TokenData> tokenList = new ArrayList<>(tokens);

        // Create a dialog to display tokens
        JDialog tokenDialog = new JDialog(parent, "Tokens for Sentence #" + sentenceNumber, true);
        tokenDialog.setSize(600, 400);
        tokenDialog.setLocationRelativeTo(parent);
        tokenDialog.setLayout(new BorderLayout(10, 10));

        JPanel tokenPanel = createCardPanel();
        tokenPanel.setLayout(new BorderLayout(10, 10));
        tokenPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create table for tokens
        DefaultTableModel tokenModel = new DefaultTableModel(
                new Object[]{"Token ID", "Token Text", "Position"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tokenTable = new JTable(tokenModel);
        styleTable(tokenTable);

        // Populate token table
        for (TokenData token : tokenList) {
            tokenModel.addRow(new Object[]{
                token.getTokenId(),
                token.getTokenText(),
                token.getPosition()
            });
        }

        // Add double-click listener to show segmentation
        tokenTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tokenTable.getSelectedRow() != -1) {
                    int selectedRow = tokenTable.getSelectedRow();
                    TokenData selectedToken = tokenList.get(selectedRow);
                    showTokenDetailsDialog(tokenDialog, selectedToken, chapter, sentenceNumber);
                }
            }
        });
        
        // Add right-click context menu for token actions
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem viewRootsItem = new JMenuItem("View Roots");
        JMenuItem viewSegmentationItem = new JMenuItem("View Segmentation");
        JMenuItem viewLemmaItem = new JMenuItem("View Lemma");
        contextMenu.add(viewRootsItem);
        contextMenu.add(viewSegmentationItem);
        contextMenu.add(viewLemmaItem);
        
        tokenTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && tokenTable.getSelectedRow() != -1) {
                    contextMenu.show(tokenTable, e.getX(), e.getY());
                }
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && tokenTable.getSelectedRow() != -1) {
                    contextMenu.show(tokenTable, e.getX(), e.getY());
                }
            }
        });
        
        viewRootsItem.addActionListener(e -> {
            int selectedRow = tokenTable.getSelectedRow();
            if (selectedRow != -1) {
                TokenData selectedToken = tokenList.get(selectedRow);
                showRootsDialog(tokenDialog, selectedToken.getTokenId(), selectedToken.getTokenText());
            }
        });
        
        viewSegmentationItem.addActionListener(e -> {
            int selectedRow = tokenTable.getSelectedRow();
            if (selectedRow != -1) {
                TokenData selectedToken = tokenList.get(selectedRow);
                showSegmentationDialog(tokenDialog, selectedToken.getTokenId(), selectedToken.getTokenText(), chapter, sentenceNumber);
            }
        });
        
        viewLemmaItem.addActionListener(e -> {
            int selectedRow = tokenTable.getSelectedRow();
            if (selectedRow != -1) {
                TokenData selectedToken = tokenList.get(selectedRow);
                showLemmaDetailDialog(tokenDialog, selectedToken);
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        JLabel headerLabel = new JLabel("Tokens for Sentence: " + sentence.getText());
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        tokenPanel.add(headerPanel, BorderLayout.NORTH);
        tokenPanel.add(new JScrollPane(tokenTable), BorderLayout.CENTER);

        // Button panel with Segment, Extract Roots, and Close buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_COLOR);
        RoundedButton segmentBtn = new RoundedButton("Segment All Tokens", PRIMARY_COLOR);
        RoundedButton extractRootsBtn = new RoundedButton("Extract Roots for All", WARNING_COLOR);
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);
        
        segmentBtn.addActionListener(e -> {
            boolean success = facade.segmentTokensForSentence(chapter.getChapterName(), sentenceNumber);
            if (success) {
                showSuccessMessage(tokenDialog, "All tokens segmented successfully!");
            } else {
                showErrorMessage(tokenDialog, "Failed to segment tokens!");
            }
        });
        
        extractRootsBtn.addActionListener(e -> {
            boolean success = facade.extractRootsForSentence(chapter.getChapterName(), sentenceNumber);
            if (success) {
                showSuccessMessage(tokenDialog, "Roots extracted for all tokens successfully!");
            } else {
                showErrorMessage(tokenDialog, "Failed to extract roots!");
            }
        });
        
        closeBtn.addActionListener(e -> tokenDialog.dispose());
        btnPanel.add(segmentBtn);
        btnPanel.add(extractRootsBtn);
        btnPanel.add(closeBtn);

        tokenPanel.add(btnPanel, BorderLayout.SOUTH);
        tokenDialog.add(tokenPanel);
        tokenDialog.setVisible(true);
    }
    
    private void showLemmasDialog(Chapter chapter, DefaultTableModel sentenceModel, JDialog parent, JTable sentenceTable) {
        int row = sentenceTable.getSelectedRow();
        if (row == -1) {
            showErrorMessage(parent, "Please select a sentence to view its lemmas!");
            return;
        }
        Object sentenceNumberObj = sentenceModel.getValueAt(row, 0);
        if (!(sentenceNumberObj instanceof Integer)) {
            showErrorMessage(parent, "Invalid sentence number format!");
            return;
        }
        int sentenceNumber = (Integer) sentenceNumberObj;

        Sentence sentence = facade.retrieveSentence(chapter.getChapterName(), sentenceNumber);
        if (sentence == null) {
            showErrorMessage(parent, "Selected sentence not found!");
            return;
        }

        List<TokenData> tokens = facade.retrieveTokensBySentence(sentence.getSentenceId());
        if (tokens == null || tokens.isEmpty()) {
            showInfoMessage(parent, "No tokens found for this sentence!");
            return;
        }

        JDialog lemmaDialog = new JDialog(parent, "Lemmas for Sentence #" + sentenceNumber, true);
        lemmaDialog.setSize(600, 400);
        lemmaDialog.setLocationRelativeTo(parent);
        lemmaDialog.setLayout(new BorderLayout(10, 10));

        JPanel lemmaPanel = createCardPanel();
        lemmaPanel.setLayout(new BorderLayout(10, 10));
        lemmaPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        DefaultTableModel lemmaModel = new DefaultTableModel(
                new Object[]{"Token Text", "Lemma", "Position"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable lemmaTable = new JTable(lemmaModel);
        styleTable(lemmaTable);

        for (TokenData token : tokens) {
            lemmaModel.addRow(new Object[]{
                    token.getTokenText(),
                    token.getLemma() == null || token.getLemma().isEmpty() ? "(not available)" : token.getLemma(),
                    token.getPosition()
            });
        }

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        JLabel headerLabel = new JLabel("Lemmas for Sentence: " + sentence.getText());
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        lemmaPanel.add(headerPanel, BorderLayout.NORTH);
        lemmaPanel.add(new JScrollPane(lemmaTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_COLOR);
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);
        closeBtn.addActionListener(e -> lemmaDialog.dispose());
        btnPanel.add(closeBtn);

        lemmaPanel.add(btnPanel, BorderLayout.SOUTH);
        lemmaDialog.add(lemmaPanel);
        lemmaDialog.setVisible(true);
    }
    
    private void showSegmentationDialog(JDialog parent, int tokenId, String tokenText, Chapter chapter, int sentenceNumber) {
        // Retrieve or create segmentation
        TokenSegmentation segmentation = facade.retrieveSegmentationByTokenId(tokenId);
        
        // If segmentation doesn't exist, create it
        if (segmentation == null) {
            boolean success = facade.segmentToken(tokenId, tokenText);
            if (success) {
                segmentation = facade.retrieveSegmentationByTokenId(tokenId);
            } else {
                showErrorMessage(parent, "Failed to segment token!");
                return;
            }
        }
        
        // Create segmentation dialog
        JDialog segDialog = new JDialog(parent, "Token Segmentation: " + tokenText, true);
        segDialog.setSize(500, 350);
        segDialog.setLocationRelativeTo(parent);
        segDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel segPanel = createCardPanel();
        segPanel.setLayout(new BorderLayout(10, 10));
        segPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        JLabel headerLabel = new JLabel("Token Segmentation");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Token info
        JPanel tokenInfoPanel = new JPanel(new GridBagLayout());
        tokenInfoPanel.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Token text
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel tokenLabel = new JLabel("Token:");
        tokenLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tokenInfoPanel.add(tokenLabel, gbc);
        gbc.gridx = 1;
        JLabel tokenValue = new JLabel(tokenText);
        tokenValue.setFont(new Font("Arial", Font.PLAIN, 18));
        tokenValue.setForeground(PRIMARY_COLOR);
        tokenInfoPanel.add(tokenValue, gbc);
        
        // Prefix
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel prefixLabel = new JLabel("Prefix:");
        prefixLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tokenInfoPanel.add(prefixLabel, gbc);
        gbc.gridx = 1;
        JLabel prefixValue = new JLabel(segmentation.getPrefix().isEmpty() ? "(none)" : segmentation.getPrefix());
        prefixValue.setFont(new Font("Arial", Font.PLAIN, 16));
        prefixValue.setForeground(segmentation.getPrefix().isEmpty() ? SECONDARY_TEXT : SUCCESS_COLOR);
        tokenInfoPanel.add(prefixValue, gbc);
        
        // Stem
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel stemLabel = new JLabel("Stem:");
        stemLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tokenInfoPanel.add(stemLabel, gbc);
        gbc.gridx = 1;
        JLabel stemValue = new JLabel(segmentation.getStem().isEmpty() ? "(none)" : segmentation.getStem());
        stemValue.setFont(new Font("Arial", Font.PLAIN, 16));
        stemValue.setForeground(segmentation.getStem().isEmpty() ? SECONDARY_TEXT : PRIMARY_COLOR);
        tokenInfoPanel.add(stemValue, gbc);
        
        // Suffix
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel suffixLabel = new JLabel("Suffix:");
        suffixLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tokenInfoPanel.add(suffixLabel, gbc);
        gbc.gridx = 1;
        JLabel suffixValue = new JLabel(segmentation.getSuffix().isEmpty() ? "(none)" : segmentation.getSuffix());
        suffixValue.setFont(new Font("Arial", Font.PLAIN, 16));
        suffixValue.setForeground(segmentation.getSuffix().isEmpty() ? SECONDARY_TEXT : WARNING_COLOR);
        tokenInfoPanel.add(suffixValue, gbc);
        
        // Visual representation
        JPanel visualPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        visualPanel.setBackground(CARD_COLOR);
        visualPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            "Visual Representation",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            TEXT_COLOR
        ));
        
        if (!segmentation.getPrefix().isEmpty()) {
            JLabel prefixVis = new JLabel(segmentation.getPrefix());
            prefixVis.setFont(new Font("Arial", Font.PLAIN, 16));
            prefixVis.setForeground(SUCCESS_COLOR);
            prefixVis.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            visualPanel.add(prefixVis);
        }
        
        JLabel stemVis = new JLabel(segmentation.getStem());
        stemVis.setFont(new Font("Arial", Font.BOLD, 18));
        stemVis.setForeground(PRIMARY_COLOR);
        stemVis.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        visualPanel.add(stemVis);
        
        if (!segmentation.getSuffix().isEmpty()) {
            JLabel suffixVis = new JLabel(segmentation.getSuffix());
            suffixVis.setFont(new Font("Arial", Font.PLAIN, 16));
            suffixVis.setForeground(WARNING_COLOR);
            suffixVis.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNING_COLOR, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            visualPanel.add(suffixVis);
        }
        
        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_COLOR);
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);
        closeBtn.addActionListener(e -> segDialog.dispose());
        btnPanel.add(closeBtn);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(CARD_COLOR);
        centerPanel.add(tokenInfoPanel, BorderLayout.CENTER);
        centerPanel.add(visualPanel, BorderLayout.SOUTH);
        
        segPanel.add(headerPanel, BorderLayout.NORTH);
        segPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel mainSegPanel = new JPanel(new BorderLayout(10, 10));
        mainSegPanel.setBackground(BACKGROUND_COLOR);
        mainSegPanel.add(segPanel, BorderLayout.CENTER);
        mainSegPanel.add(btnPanel, BorderLayout.SOUTH);
        
        segDialog.add(mainSegPanel);
        segDialog.setVisible(true);
    }
    

    private void displayResults(List<Map<String, String>> results, String token) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial; padding: 10px;'>");
        
        if (results.isEmpty()) {
            html.append("<p>No occurrences found for token: ").append(token).append("</p>");
        } else {
            html.append("<h3>Occurrences of '").append(token).append("' (").append(results.size()).append("):</h3>");
            html.append("<ul style='margin-top: 0;'>");
            
            String currentBook = "";
            String currentChapter = "";
            
            for (Map<String, String> row : results) {
                String bookTitle = row.get("book_title");
                String chapterName = row.get("chapter_name");
                String authorName = row.get("author_name");
                String sentenceText = row.get("sentence_text");
                
                String highlighted = TextHighlighter.highlightToken(sentenceText, token);
                
                // Show book header if it's a new book
                if (!bookTitle.equals(currentBook)) {
                    currentBook = bookTitle;
                    html.append("<li style='margin: 15px 0 5px 0; font-weight: bold; color: #2c3e50;'>")
                       .append(bookTitle).append(" (").append(authorName).append(")</li>");
                }
                
                // Show chapter header if it's a new chapter
                if (!chapterName.equals(currentChapter)) {
                    currentChapter = chapterName;
                    html.append("<li style='margin: 5px 0 5px 20px; font-style: italic; color: #34495e;'>")
                       .append(chapterName).append("</li>");
                }
                
                // Show the sentence
                html.append("<li style='margin: 5px 0 15px 40px;'>")
                   .append(highlighted).append("</li>");
            }
            
            html.append("</ul>");
        }
        html.append("</body></html>");
        
        resultsPane.setText(html.toString());
        resultsPane.setCaretPosition(0);
    }
 
    

    
    private void showTokenDetailsDialog(JDialog parent, TokenData tokenData, Chapter chapter, int sentenceNumber) {
        int tokenId = tokenData.getTokenId();
        String tokenText = tokenData.getTokenText();
        // Show a dialog with options to view segmentation, roots, or lemma
        JDialog detailsDialog = new JDialog(parent, "Token Details: " + tokenText, true);
        detailsDialog.setSize(400, 200);
        detailsDialog.setLocationRelativeTo(parent);
        detailsDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel detailsPanel = createCardPanel();
        detailsPanel.setLayout(new BorderLayout(10, 10));
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel tokenLabel = new JLabel("Token: " + tokenText);
        tokenLabel.setFont(new Font("Arial", Font.BOLD, 18));
        tokenLabel.setForeground(PRIMARY_COLOR);
        tokenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(CARD_COLOR);
        
        RoundedButton viewRootsBtn = new RoundedButton("View Roots", WARNING_COLOR);
        RoundedButton viewSegBtn = new RoundedButton("View Segmentation", PRIMARY_COLOR);
        RoundedButton viewLemmaBtn = new RoundedButton("View Lemma", new Color(34, 139, 34));
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);
        
        viewRootsBtn.addActionListener(e -> {
            detailsDialog.dispose();
            showRootsDialog(parent, tokenId, tokenText);
        });
        
        viewSegBtn.addActionListener(e -> {
            detailsDialog.dispose();
            showSegmentationDialog(parent, tokenId, tokenText, chapter, sentenceNumber);
        });
        
        viewLemmaBtn.addActionListener(e -> {
            detailsDialog.dispose();
            showLemmaDetailDialog(parent, tokenData);
        });
        
        closeBtn.addActionListener(e -> detailsDialog.dispose());
        
        buttonPanel.add(viewRootsBtn);
        buttonPanel.add(viewSegBtn);
        buttonPanel.add(viewLemmaBtn);
        buttonPanel.add(closeBtn);
        
        detailsPanel.add(tokenLabel, BorderLayout.NORTH);
        detailsPanel.add(buttonPanel, BorderLayout.CENTER);
        
        detailsDialog.add(detailsPanel);
        detailsDialog.setVisible(true);
    }
    
    private void showLemmaDetailDialog(JDialog parent, TokenData tokenData) {
        String lemma = tokenData.getLemma();
        JDialog lemmaDialog = new JDialog(parent, "Lemma Details", true);
        lemmaDialog.setSize(350, 200);
        lemmaDialog.setLocationRelativeTo(parent);
        lemmaDialog.setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = createCardPanel();
        contentPanel.setLayout(new GridLayout(3, 1, 5, 5));

        JLabel tokenLabel = new JLabel("Token: " + tokenData.getTokenText());
        tokenLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tokenLabel.setForeground(PRIMARY_COLOR);

        JLabel lemmaLabel = new JLabel("Lemma: " + (lemma == null || lemma.isEmpty() ? "(not available)" : lemma));
        lemmaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lemmaLabel.setForeground(TEXT_COLOR);

        JLabel noteLabel = new JLabel("Position: " + tokenData.getPosition());
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noteLabel.setForeground(SECONDARY_TEXT);

        contentPanel.add(tokenLabel);
        contentPanel.add(lemmaLabel);
        contentPanel.add(noteLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_COLOR);
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);
        closeBtn.addActionListener(e -> lemmaDialog.dispose());
        btnPanel.add(closeBtn);

        lemmaDialog.add(contentPanel, BorderLayout.CENTER);
        lemmaDialog.add(btnPanel, BorderLayout.SOUTH);
        lemmaDialog.setVisible(true);
    }
    
    private void showRootsDialog(JDialog parent, int tokenId, String tokenText) {
        // Retrieve existing roots
        List<Root> roots = facade.retrieveRootsByTokenId(tokenId);
        
        // If no roots exist, extract them
        if (roots == null || roots.isEmpty()) {
            int option = JOptionPane.showConfirmDialog(parent,
                "No roots found for this token. Would you like to extract roots now?",
                "Extract Roots",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                boolean success = facade.extractRootsForToken(tokenId, tokenText);
                if (success) {
                    roots = facade.retrieveRootsByTokenId(tokenId);
                } else {
                    showErrorMessage(parent, "Failed to extract roots for token: " + tokenText);
                    return;
                }
            } else {
                return;
            }
        }
        
        // Create roots dialog
        JDialog rootsDialog = new JDialog(parent, "Roots for Token: " + tokenText, true);
        rootsDialog.setSize(600, 450);
        rootsDialog.setLocationRelativeTo(parent);
        rootsDialog.setLayout(new BorderLayout(10, 10));
        
        JPanel rootsPanel = createCardPanel();
        rootsPanel.setLayout(new BorderLayout(10, 10));
        rootsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        JLabel headerLabel = new JLabel("Extracted Roots");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Token info
        JLabel tokenInfoLabel = new JLabel("Token: " + tokenText);
        tokenInfoLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        tokenInfoLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(tokenInfoLabel, BorderLayout.EAST);
        
        // Create table for roots
        DefaultTableModel rootsModel = new DefaultTableModel(
                new Object[]{"Root Text", "Pattern", "Confidence Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable rootsTable = new JTable(rootsModel);
        styleTable(rootsTable);
        
        // Populate roots table
        for (Root root : roots) {
            rootsModel.addRow(new Object[]{
                root.getRootText(),
                root.getPattern(),
                String.format("%.2f", root.getConfidenceScore())
            });
        }
        
        // Custom renderer for confidence score column
        rootsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    double confidence = Double.parseDouble(value.toString());
                    if (confidence >= 0.8) {
                        c.setForeground(SUCCESS_COLOR);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (confidence >= 0.5) {
                        c.setForeground(WARNING_COLOR);
                    } else {
                        c.setForeground(SECONDARY_TEXT);
                    }
                } catch (NumberFormatException e) {
                    // Keep default color
                }
                return c;
            }
        });
        
        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_COLOR);
        RoundedButton extractBtn = new RoundedButton("Re-extract Roots", PRIMARY_COLOR);
        RoundedButton closeBtn = new RoundedButton("Close", DANGER_COLOR);
        
        extractBtn.addActionListener(e -> {
            boolean success = facade.extractRootsForToken(tokenId, tokenText);
            if (success) {
                // Refresh roots
                rootsModel.setRowCount(0);
                List<Root> newRoots = facade.retrieveRootsByTokenId(tokenId);
                for (Root root : newRoots) {
                    rootsModel.addRow(new Object[]{
                        root.getRootText(),
                        root.getPattern(),
                        String.format("%.2f", root.getConfidenceScore())
                    });
                }
                showSuccessMessage(rootsDialog, "Roots re-extracted successfully!");
            } else {
                showErrorMessage(rootsDialog, "Failed to re-extract roots!");
            }
        });
        
        closeBtn.addActionListener(e -> rootsDialog.dispose());
        btnPanel.add(extractBtn);
        btnPanel.add(closeBtn);
        
        rootsPanel.add(headerPanel, BorderLayout.NORTH);
        rootsPanel.add(new JScrollPane(rootsTable), BorderLayout.CENTER);
        rootsPanel.add(btnPanel, BorderLayout.SOUTH);
        
        rootsDialog.add(rootsPanel);
        rootsDialog.setVisible(true);
    }

    // REFRESH METHODS
    private void refreshBookTable() {
        bookTableModel.setRowCount(0);
        List<Book> books = facade.retrieveAllBooks();
        if (books != null) {
            for (Book b : books) {
                String authorName = facade.getAuthorByID(b.getAuthorId());
                bookTableModel.addRow(new Object[]{b.getTitle(), authorName, b.getEra()});
            }
        }
    }

    private void refreshAuthorTable() {
        authorTableModel.setRowCount(0);
        List<Author> authors = facade.retrieveAllAuthors();
        if (authors != null) {
            for (Author a : authors) {
                authorTableModel.addRow(new Object[]{a.getName(), a.getBiography()});
            }
        }
    }

    private void refreshChapterTable(Book book, DefaultTableModel model) {
        model.setRowCount(0);
        List<Chapter> chapters = facade.retrieveChaptersByBook(book.getTitle());
        if (chapters != null && !chapters.isEmpty()) {
            for (Chapter c : chapters) {
                model.addRow(new Object[]{c.getChapterId(), c.getChapterName()});
            }
        } else {
            model.addRow(new Object[]{"No chapters", "added yet"});
        }
    }

    private void refreshSentenceTableForChapter(Book book, Chapter chapter, DefaultTableModel model) {
        model.setRowCount(0);
        List<Sentence> sentences = facade.retrieveSentencesByChapter(chapter.getChapterName());
        if (sentences != null && !sentences.isEmpty()) {
            for (Sentence s : sentences) {
                model.addRow(new Object[]{
                    s.getSentenceNumber(),
                    s.getText(),
                    s.getTextDiacritized(),
                    s.getTranslation(),
                    s.getNotes()
                });
            }
        } else {
            model.addRow(new Object[]{"No sentences", "added yet", "", "", ""});
        }
    }

    // UTILITY METHODS
    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    private JPanel createTableHeader(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(SECONDARY_TEXT);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CARD_COLOR);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(220, 237, 255));
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(new Color(226, 232, 240));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_COLOR);
        header.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
    }

    // MESSAGE DIALOGS
    private void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Import Chapter from File Dialog
    private void showImportChapterDialog(Book book) {
        JDialog dialog = new JDialog(this, "Import Chapter from File - " + book.getTitle(), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField chapterNameField = new JTextField(25);
        JTextField filePathField = new JTextField(25);
        JButton browseBtn = new JButton("Browse...");

        browseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Chapter Name:"), gbc);
        gbc.gridx = 1; dialog.add(chapterNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Text File:"), gbc);
        gbc.gridx = 1; dialog.add(filePathField, gbc);
        gbc.gridx = 2; dialog.add(browseBtn, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        RoundedButton importBtn = new RoundedButton("Import", SUCCESS_COLOR);
        RoundedButton cancelBtn = new RoundedButton("Cancel", DANGER_COLOR);

        importBtn.addActionListener(e -> {
            if (!chapterNameField.getText().trim().isEmpty() && !filePathField.getText().trim().isEmpty()) {
                boolean success = facade.importChapterFromFile(
                    book.getTitle(),
                    chapterNameField.getText().trim(),
                    filePathField.getText().trim()
                );
                if (success) {
                    dialog.dispose();
                    showSuccessMessage(this, "Chapter imported successfully from file!");
                    showChapterDialog(book, -1, -1);
                } else {
                    showErrorMessage(this, "Failed to import chapter from file!");
                }
            } else {
                showErrorMessage(this, "Please enter chapter name and select a file!");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(importBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ROUNDED BUTTON CLASS
    public static class RoundedButton extends JButton {
        private Color backgroundColor;
        private Color hoverColor;
        private Color pressedColor;

        public RoundedButton(String text, Color bgColor) {
            super(text);
            this.backgroundColor = bgColor;
            this.hoverColor = bgColor.brighter();
            this.pressedColor = bgColor.darker();

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(backgroundColor);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    setBackground(pressedColor);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    setBackground(hoverColor);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(backgroundColor);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
            g2.dispose();
        }
    }


    public int getBookIdByChapterId(int chapterId) {
        Chapter chapter = facade.retrieveChapter(chapterId);
        return (chapter != null) ? chapter.getBookId() : -1;
    }

    public void navigateToSentence(int bookId, int chapterId, int sentenceId) {
        List<Book> books = facade.retrieveAllBooks();
        Book targetBook = null;
        for (Book b : books) {
            if (b.getBookId() == bookId) {
                targetBook = b;
                break;
            }
        }

        if (targetBook != null) {
            showChapterDialog(targetBook, chapterId, sentenceId);
        } else {
            showErrorMessage(this, "Book not found for ID: " + bookId);
        }
    }


    
    public void switchToSimilaritySearch(String query) {
        if (tabbedPane != null && similaritySearchPanel != null) {
            tabbedPane.setSelectedComponent(similaritySearchPanel);
            similaritySearchPanel.setQuery(query);
        }
    }
} 


 