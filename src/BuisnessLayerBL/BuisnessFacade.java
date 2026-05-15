package BuisnessLayerBL;
import java.util.ArrayList;
import java.util.List;

import ModelDTO.Author;
import ModelDTO.Book;
import ModelDTO.Chapter;
import ModelDTO.Sentence;
import ModelDTO.TokenData;
import ModelDTO.TokenSegmentation;
import ModelDTO.Root;

public class BuisnessFacade  implements IBusinessFacade{
     IAuthorBo author;
     IBookBo book;
     ISentenceBo sentence;
     private IChapterBo chapter;  // 
     private  ITokenBo token;
     private ISegmentationBo segmentation;
     private IRootBo root;
     private ILemmatizationBo lemmatization;
     
     public BuisnessFacade(IAuthorBo author, IBookBo book, ISentenceBo sentence, IChapterBo chapter, ITokenBo token, ISegmentationBo segmentation, IRootBo root, ILemmatizationBo lemmatization) {
         this.author = author;
         this.book = book;
         this.sentence = sentence;
         this.chapter = chapter;
         this.token = token;
         this.segmentation = segmentation;
         this.root = root;
         this.lemmatization = lemmatization;
     }
     
     public boolean addAuthor(String name,String biography )
     {
  	   return author.addAuthor(name, biography);
     }
	   public boolean updateAuthor(String oldname,String newname,String biography)
	   {
		   return author.updateAuthor(oldname, newname, biography);
	   }
	   public boolean deleteAuthor(String authorname)
	   {
		   return author.deleteAuthor(authorname);
	   }
	   public Author retrieveAuthor (String keyword)
	   {
		   return author.retrieveAuthor(keyword);
	   }
	   public int searchAuthor(String authorname)
	   {
		   return author.searchAuthor(authorname);
	   }
	   

	   @Override
	   public boolean addSentence(String chapterName, String text, String textDiacritized, String translation, String notes) {
		return sentence.addSentence(chapterName, text, textDiacritized, translation, notes);
	   }
	   @Override
	   public boolean updateSentence(String bookName, int sentenceNumber, String newText, String newDiacritized,
			String newTranslation, String newNotes) {
		return sentence.updateSentence(bookName, sentenceNumber, newText, newDiacritized, newTranslation, newNotes);
	   }
	   @Override
	   public boolean deleteSentence(String bookName, int sentenceNumber) {
		return sentence.deleteSentence(bookName, sentenceNumber);
	   }
	   @Override
	   public Sentence retrieveSentence(String bookName, int sentenceNumber) {
		return sentence.retrieveSentence(bookName, sentenceNumber);
	   }
	   @Override
	   public List<Sentence> retrieveSentencesByBook(String bookName) {
		return sentence.retrieveSentencesByBook(bookName);
	   }
	   @Override
	   public ArrayList<Author> retrieveAllAuthors() {
		return author.retrieveAllAuthors();
	   }
	   @Override
	   public ArrayList<Book> retrieveAllBooks() {
		return book.retrieveAllBooks();
	   }
	   @Override
	   public String getAuthorByID(int id) {
		return author.getAuthorByID(id);
	   }
	   @Override
	   public boolean addBook(String title, String authorName, String era, String authorname2) {
		return book.addBook(title, authorName, era, authorname2);
	   }
	   @Override
	   public boolean updateBook(String bookName, String newBookName, String authorName, String era) {
		return book.updateBook(bookName, newBookName, authorName, era);
	   }
	   @Override
	   public boolean deleteBook(String bookTitle) {
		return book.deleteBook(bookTitle);
	   }
	   @Override
	   public Book retrieveBook(String keyword) {
		return book.retrieveBook(keyword);
	   }
	   @Override
	   public int searchBook(String title) {
		return book.searchBook(title);
	   }
	// ---------------- CHAPTER METHODS ----------------
	    @Override
	    public boolean addChapter(String bookName, String chapterName) {
	    	System.out.println("adding chapterss");
	        return chapter.addChapter(bookName, chapterName);
	    }

	    @Override
	    public boolean updateChapter(String bookName, int chapterId, String newChapterName) {
	        return chapter.updateChapter(bookName, chapterId, newChapterName);
	    }

	    @Override
	    public boolean deleteChapter(String bookName, int chapterId) {
	        return chapter.deleteChapter(bookName, chapterId);
	    }

	    @Override
	    public Chapter retrieveChapter(String bookName, int chapterId) {
	        return chapter.retrieveChapter(bookName, chapterId);
	    }

	    @Override
	    public List<Chapter> retrieveChaptersByBook(String bookName) {
	        return chapter.retrieveChaptersByBook(bookName);
	    }
		@Override
		public List<Sentence> retrieveSentencesByChapter(String chapterName) {
			// TODO Auto-generated method stub
			return sentence.retrieveSentencesByChapter(chapterName);
		}
		@Override
		public boolean importChapterFromFile(String bookName, String chapterName, String filePath) {
			// TODO Auto-generated method stub
			return chapter.importChapterFromFile(bookName, chapterName, filePath) ;
		}
		
		@Override
	    public List<Sentence> retrieveAllSentences() {
	        return sentence.retrieveAllSentences();
	    }
	    
	    @Override
	    public String getChapterNameById(int chapterId) {
	        return chapter.getChapterNameById(chapterId);
	    }
	    
	    @Override
	    public int getLastInsertedSentenceId() {
	        return sentence.getLastInsertedSentenceId();
	    }
	    
	    @Override
	    public List<Sentence> searchSentencesByExactString(String phrase) {
	        return sentence.searchSentencesByExactString(phrase);
	    }
	    
	    @Override
	    public java.util.List<java.util.Map<String, String>> searchSentencesByRegex(String pattern) {
	        return sentence.searchSentencesByRegex(pattern);
	    }
	    
	    @Override
	    public boolean addTokensForSentence(String chapterName, int sentenceNumber, List<TokenData> tokens) {
	        return token.addTokensForSentence(chapterName, sentenceNumber, tokens);
	    }
	    
	    @Override
	    public boolean updateTokensForSentence(String chapterName, int sentenceNumber, List<TokenData> tokens) {
	        return token.updateTokensForSentence(chapterName, sentenceNumber, tokens);
	    }
	    
	    @Override
	    public List<TokenData> retrieveTokensForSentence(String chapterName, int sentenceNumber) {
	        return token.retrieveTokensForSentence(chapterName, sentenceNumber);
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
	    public boolean segmentToken(int tokenId, String tokenText) {
	        return segmentation.segmentToken(tokenId, tokenText);
	    }
	    
	    @Override
	    public boolean segmentTokensForSentence(String chapterName, int sentenceNumber) {
	        return segmentation.segmentTokensForSentence(chapterName, sentenceNumber);
	    }
	    
	    @Override
	    public TokenSegmentation retrieveSegmentationByTokenId(int tokenId) {
	        return segmentation.retrieveSegmentationByTokenId(tokenId);
	    }
	    
	    @Override
	    public List<TokenSegmentation> retrieveSegmentationsBySentence(String chapterName, int sentenceNumber) {
	        return segmentation.retrieveSegmentationsBySentence(chapterName, sentenceNumber);
	    }
	    
	    @Override
	    public boolean updateSegmentation(int tokenId, String prefix, String stem, String suffix) {
	        return segmentation.updateSegmentation(tokenId, prefix, stem, suffix);
	    }
	    
	    @Override
	    public boolean deleteSegmentation(int tokenId) {
	        return segmentation.deleteSegmentation(tokenId);
	    }
	    
	    // ---------------- Root extraction methods ----------------
	    @Override
	    public boolean extractRootsForToken(int tokenId, String tokenText) {
	        return root.extractRootsForToken(tokenId, tokenText);
	    }
	    
	    @Override
	    public boolean extractRootsForSentence(String chapterName, int sentenceNumber) {
	        return root.extractRootsForSentence(chapterName, sentenceNumber);
	    }
	    
	    @Override
	    public List<Root> retrieveRootsByTokenId(int tokenId) {
	        return root.retrieveRootsByTokenId(tokenId);
	    }
	    
	    @Override
	    public List<Root> retrieveRootsBySentence(String chapterName, int sentenceNumber) {
	        return root.retrieveRootsBySentence(chapterName, sentenceNumber);
	    }
	    
	    @Override
	    public boolean deleteRootsByTokenId(int tokenId) {
	        return root.deleteRootsByTokenId(tokenId);
	    }
	    
	    @Override
	    public boolean updateRoot(Root root) {
	        return this.root.updateRoot(root);
	    }
	    
	    @Override
	    public List<String> getAllRoots() {
	        return root.getAllRoots();
	    }
    
    @Override
    public String lemmatizeToken(String tokenText) {
        return lemmatization.lemmatizeToken(tokenText);
    }

    @Override
    public List<ModelDTO.SentenceSearchResult> performSimilaritySearch(String query, double thresholdPercentage) {
        return sentence.performSimilaritySearch(query, thresholdPercentage);
    }

    @Override
    public Chapter retrieveChapter(int chapterId) {
        return chapter.retrieveChapter(chapterId);
    }

    @Override
    public Book retrieveBook(int bookId) {
        return book.retrieveBook(bookId);
    }

    @Override
    public Sentence retrieveSentenceById(int sentenceId) {
        return sentence.retrieveSentenceById(sentenceId);
    }
    
    @Override
    public List<java.util.Map<String, String>> getSentencesByToken(String tokenText) {
        return token.getSentencesByToken(tokenText);
    }

    @Override
    public List<String> getLemmasByRoot(String rootText) {
        return token.getLemmasByRoot(rootText);
    }
    
    // ---------------- Frequency Analysis ----------------
    
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
/// ends  here 

