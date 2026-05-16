
package DataAcessLayerDAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ModelDTO.Author;
import ModelDTO.Book;
import ModelDTO.Chapter;
import ModelDTO.Root;
import ModelDTO.Sentence;
import ModelDTO.TokenData;
import ModelDTO.TokenSegmentation;

public class DataAccessFacacde implements IDataFacade {
    private IAuthorDOA author;
    private IBookDOA book;
    private ISentenceDOA sentence;
    private IChapterDOA chapter;
    private ITokenDOA token;
    private ISegmentationDOA segmentation;
    private IRootDOA root;

    public DataAccessFacacde(IAuthorDOA author, IBookDOA book, ISentenceDOA sentence, IChapterDOA chapter, ITokenDOA token, ISegmentationDOA segmentation, IRootDOA root) {
        this.author = author;
        this.book = book;
        this.sentence = sentence;
        this.chapter = chapter;
        this.token = token;
        this.segmentation = segmentation;
        this.root = root;
    }

    // ---------------- Author methods ----------------
    @Override
    public boolean addAuthor(String name, String biography) {
        return author.addAuthor(name, biography);
    }

    @Override
    public boolean updateAuthor(String oldname, String newname, String biography) {
        return author.updateAuthor(oldname, newname, biography);
    }

    @Override
    public boolean deleteAuthor(String authorname) {
        return author.deleteAuthor(authorname);
    }

    @Override
    public Author retrieveAuthor(String keyword) {
        return author.retrieveAuthor(keyword);
    }

    @Override
    public int searchAuthor(String authorname) {
        return author.searchAuthor(authorname);
    }

    @Override
    public ArrayList<Author> retrieveAllAuthors() {
        return author.retrieveAllAuthors();
    }

    @Override
    public String getAuthorByID(int id) {
        return author.getAuthorByID(id);
    }

    // ---------------- Book methods ----------------
    @Override
    public ArrayList<Book> retrieveAllBooks() {
        return book.retrieveAllBooks();
    }

    @Override
    public int searchBook(String title) {
        return book.searchBook(title);
    }

    @Override
    public boolean addBook(String title, int authorId, String era, String authorname) {
        return book.addBook(title, authorId, era, authorname);
    }

    @Override
    public boolean updateBook(int bookId, int authorID, String newBookName, String era) {
        return book.updateBook(bookId, authorID, newBookName, era);
    }

    @Override
    public boolean deleteBook(int bookId) {
        return book.deleteBook(bookId);
    }

    @Override
    public Book retrieveBook(int id) {
        return book.retrieveBook(id);
    }

    // ---------------- Chapter methods ----------------
    @Override
    public boolean addChapter(int bookId, String chapterName) {
        return chapter.addChapter(bookId, chapterName);
    }

    @Override
    public boolean updateChapter(int chapterId, String newName) {
        return chapter.updateChapter(chapterId, newName);
    }

    @Override
    public boolean deleteChapter(int chapterId) {
        return chapter.deleteChapter(chapterId);
    }

    @Override
    public ModelDTO.Chapter retrieveChapter(int chapterId) {
        return chapter.retrieveChapter(chapterId);
    }

    @Override
    public java.util.List<ModelDTO.Chapter> retrieveChaptersByBook(int bookId) {
        return chapter.retrieveChaptersByBook(bookId);
    }

    @Override
    public int searchChapter(String name) {
        return chapter.searchChapter(name);
    }

    @Override
    public List<Integer> getChapterIdsByBook(int bookId) {
        return chapter.getChapterIdsByBook(bookId);
    }

    @Override
    public boolean importChapterFromFile(int bookId, String chapterName, String filePath) {
        return chapter.importChapterFromFile(bookId, chapterName, filePath);
    }

    @Override
    public Chapter getChapterById(int chapterId) {
        return chapter.getChapterById(chapterId);
    }

    @Override
    public String getChapterNameById(int chapterId) {
        return chapter.getChapterNameById(chapterId);
    }

    // ---------------- Sentence methods ----------------
    @Override
    public boolean addSentence(int chapterID, String text, String textDiacritized, String translation, String notes) {
        return sentence.addSentence(chapterID, text, textDiacritized, translation, notes);
    }

    @Override
    public boolean updateSentence(int chapterID, int sentenceNumber, String newText, String newDiacritized,
                                 String newTranslation, String newNotes) {
        return sentence.updateSentence(chapterID, sentenceNumber, newText, newDiacritized, newTranslation, newNotes);
    }

    @Override
    public boolean deleteSentence(int chapterID, int sentenceNumber) {
        return sentence.deleteSentence(chapterID, sentenceNumber);
    }

    @Override
    public Sentence retrieveSentence(int chapterID, int sentenceNumber) {
        return sentence.retrieveSentence(chapterID, sentenceNumber);
    }

    @Override
    public List<Sentence> retrieveSentencesByChapter(int chapterID) {
        return sentence.retrieveSentencesByChapter(chapterID); // Fixed: Correct method
    }

    @Override
    public List<Sentence> retrieveSentencesByBook(int bookId) {
        return sentence.retrieveSentencesByBook(bookId);
    }

    @Override
    public List<Sentence> retrieveAllSentences() {
        return sentence.retrieveAllSentences();
    }

    @Override
    public int getLastInsertedSentenceId() {
        return sentence.getLastInsertedSentenceId();
    }

    @Override
    public Sentence retrieveSentenceById(int sentenceId) {
        return sentence.retrieveSentenceById(sentenceId);
    }
    
    @Override
    public List<Sentence> searchSentencesByExactString(String phrase) {
        return sentence.searchSentencesByExactString(phrase);
    }

    // ---------------- Token methods ----------------
    @Override
    public boolean addTokensForSentence(int sentenceId, List<TokenData> tokens) {
        return token.addTokensForSentence(sentenceId, tokens);
    }

    @Override
    public boolean updateTokensForSentence(int sentenceId, List<TokenData> tokens) {
        return token.updateTokensForSentence(sentenceId, tokens);
    }

    @Override
    public List<TokenData> retrieveTokensForSentence(int sentenceId) {
        return token.retrieveTokensForSentence(sentenceId);
    }
    @Override
    public List<TokenData> retrieveTokensBySentence(int sentenceId) {
        return token.retrieveTokensBySentence(sentenceId);
    }
    
    @Override
    public List<TokenData> getTokensByRoot(String rootText) {
        return token.getTokensByRoot(rootText);
    }

    @Override
    public List<String> getAllDistinctTokens() {
        return token.getAllDistinctTokens();
    }
    
    @Override
    public List<String> getAllDistinctLemmas() {
        return token.getAllDistinctLemmas();
    }
    
    @Override
    public List<TokenData> getTokensByLemma(String lemma) {
        return token.getTokensByLemma(lemma);
    }

    @Override
    public List<String> getAllDistinctSegments() {
        return token.getAllDistinctSegments();
    }

    @Override
    public List<TokenData> getTokensBySegment(String segment) {
        return token.getTokensBySegment(segment);
    }

    // ---------------- Segmentation methods ---------------- 
    @Override
    public boolean addSegmentation(int tokenId, TokenSegmentation segmentation) {
        return this.segmentation.addSegmentation(tokenId, segmentation);
    }

    @Override
    public boolean addSegmentationsForTokens(List<TokenSegmentation> segmentations) {
        return this.segmentation.addSegmentationsForTokens(segmentations);
    }

    @Override
    public boolean updateSegmentation(int tokenId, TokenSegmentation segmentation) {
        return this.segmentation.updateSegmentation(tokenId, segmentation);
    }

    @Override
    public TokenSegmentation retrieveSegmentationByTokenId(int tokenId) {
        return this.segmentation.retrieveSegmentationByTokenId(tokenId);
    }

    @Override
    public List<TokenSegmentation> retrieveSegmentationsBySentenceId(int sentenceId) {
        return this.segmentation.retrieveSegmentationsBySentenceId(sentenceId);
    }

    @Override
    public boolean deleteSegmentation(int tokenId) {
        return this.segmentation.deleteSegmentation(tokenId);
    }

    // ---------------- Root methods ----------------
    @Override
    public boolean addRoot(Root root) {
        return this.root.addRoot(root);
    }

    @Override
    public boolean addRootsForToken(int tokenId, List<Root> roots) {
        return this.root.addRootsForToken(tokenId, roots);
    }

    @Override
    public boolean updateRoot(Root root) {
        return this.root.updateRoot(root);
    }

    @Override
    public List<Root> retrieveRootsByTokenId(int tokenId) {
        return this.root.retrieveRootsByTokenId(tokenId);
    }

    @Override
    public List<Root> retrieveRootsBySentenceId(int sentenceId) {
        return this.root.retrieveRootsBySentenceId(sentenceId);
    }

    @Override
    public boolean deleteRootsByTokenId(int tokenId) {
        return this.root.deleteRootsByTokenId(tokenId);
    }

    @Override
    public boolean deleteRoot(int rootId) {
        return this.root.deleteRoot(rootId);
    }
    
    @Override
    public List<String> getAllRoots() {
        return this.root.getAllRoots();
    }
    
 // In your DataFacade implementation class
    @Override
    public List<Map<String, String>> findSentencesByToken(String tokenText) {
        return new TokenDOA().findSentencesByToken(tokenText);
    }
    
    @Override
    public List<Map<String, String>> searchSentencesByRegex(String pattern) {
        return new SentenceDOA().searchSentencesByRegex(pattern);
    }

	@Override
	public List<String> getLemmasByRoot(String rootText) {
		return token.getLemmasByRoot(rootText);
	}

    // ---------------- Frequency Analysis Methods ----------------
    
    // Root Frequency
    @Override
    public java.util.Map<String, Integer> getRootFrequencyInChapter(int chapterId) {
        return root.getRootFrequencyInChapter(chapterId);
    }

    @Override
    public java.util.Map<String, Integer> getRootFrequencyInBook(int bookId) {
        return root.getRootFrequencyInBook(bookId);
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getRootFrequencyBreakdownByBook(int bookId) {
        return root.getRootFrequencyBreakdownByBook(bookId);
    }

    // Token & Lemma Frequency
    @Override
    public java.util.Map<String, Integer> getTokenFrequencyInChapter(int chapterId) {
        return token.getTokenFrequencyInChapter(chapterId);
    }

    @Override
    public java.util.Map<String, Integer> getLemmaFrequencyInChapter(int chapterId) {
        return token.getLemmaFrequencyInChapter(chapterId);
    }

    @Override
    public java.util.Map<String, Integer> getTokenFrequencyInBook(int bookId) {
        return token.getTokenFrequencyInBook(bookId);
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getTokenFrequencyBreakdownByBook(int bookId) {
        return token.getTokenFrequencyBreakdownByBook(bookId);
    }

    @Override
    public java.util.Map<String, Integer> getLemmaFrequencyInBook(int bookId) {
        return token.getLemmaFrequencyInBook(bookId);
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Integer>> getLemmaFrequencyBreakdownByBook(int bookId) {
        return token.getLemmaFrequencyBreakdownByBook(bookId);
    }
}