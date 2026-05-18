package PresentationLayerPL;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import BuisnessLayerBL.AuthorBo;
import BuisnessLayerBL.BookBo;
import BuisnessLayerBL.BuisnessFacade;
import BuisnessLayerBL.ChapterBo;
import BuisnessLayerBL.IAuthorBo;
import BuisnessLayerBL.IBookBo;
import BuisnessLayerBL.IChapterBo;
import BuisnessLayerBL.IRootBo;
import BuisnessLayerBL.ILemmatizationBo;
import BuisnessLayerBL.ISegmentationBo;
import BuisnessLayerBL.ISentenceBo;
import BuisnessLayerBL.ITokenBo;
import BuisnessLayerBL.ITokenOccurrenceBo;
import BuisnessLayerBL.LemmatizationBo;
import BuisnessLayerBL.RootBo;
import BuisnessLayerBL.SegmentationBo;
import BuisnessLayerBL.SentenceBo;
import BuisnessLayerBL.TokenBo;
import BuisnessLayerBL.TokenOccurrenceBo;
import DataAcessLayerDAL.AuthorDOA;
import DataAcessLayerDAL.BookDOA;
import DataAcessLayerDAL.ChapterDOA;
import DataAcessLayerDAL.DataAccessFacacde;
import DataAcessLayerDAL.IAuthorDOA;
import DataAcessLayerDAL.IBookDOA;
import DataAcessLayerDAL.IChapterDOA;
import DataAcessLayerDAL.IRootDOA;
import DataAcessLayerDAL.ISegmentationDOA;
import DataAcessLayerDAL.ISentenceDOA;
import DataAcessLayerDAL.ITokenDOA;
import DataAcessLayerDAL.RootDOA;
import DataAcessLayerDAL.SegmentationDOA;
import DataAcessLayerDAL.SentenceDOA;
import DataAcessLayerDAL.TokenDOA;

public class main {
	
	
	
    public static void main(String[] args) throws SQLException {
        // Initialize DOAs with DBConnection
        IAuthorDOA authorDao = new AuthorDOA();
        IBookDOA bookDao = new BookDOA();
        IChapterDOA chapterDao = new ChapterDOA();
        ISentenceDOA sentenceDao = new SentenceDOA();
        ITokenDOA tokenDao = new TokenDOA();
        ISegmentationDOA segmentationDao = new SegmentationDOA();
        IRootDOA rootDao = new RootDOA();

        // Initialize DataAccessFacade with all DOAs
        DataAccessFacacde dataFacade = new DataAccessFacacde(authorDao, bookDao, sentenceDao, chapterDao, tokenDao, segmentationDao, rootDao);

        ITokenOccurrenceBo tokenOccurrenceBo = new TokenOccurrenceBo(dataFacade);
        // Initialize BOs
        IAuthorBo authorBo = new AuthorBo(dataFacade);
        IBookBo bookBo = new BookBo(dataFacade);
        IChapterBo chapterBo = new ChapterBo(dataFacade);
        ITokenBo tokenBo = new TokenBo(dataFacade);
        ILemmatizationBo lemmatizationBo = new LemmatizationBo();
        ISentenceBo sentenceBo = new SentenceBo(dataFacade, tokenBo, lemmatizationBo);
        ISegmentationBo segmentationBo = new SegmentationBo(dataFacade);
        IRootBo rootBo = new RootBo(dataFacade);

        // Initialize BusinessFacade
        BuisnessFacade facade = new BuisnessFacade(authorBo, bookBo, sentenceBo, chapterBo, tokenBo, segmentationBo, rootBo, lemmatizationBo);
    
        SwingUtilities.invokeLater(() -> {
            MainFrame browseFrame = new MainFrame(facade, tokenOccurrenceBo);
            browseFrame.setVisible(true);
            
            ArabicProseUserInterface ui = new ArabicProseUserInterface(facade);
            ui.setVisible(true);
        });
    }
} 
