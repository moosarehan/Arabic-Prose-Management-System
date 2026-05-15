
 package BuisnessLayerBL;

import ModelDTO.Chapter;
import ModelDTO.Sentence;

public interface IBusinessFacade extends IBookBo,IAuthorBo,ISentenceBo ,IChapterBo,ITokenBo,ISegmentationBo,IRootBo,ILemmatizationBo {

    Chapter retrieveChapter(int chapterId);
    ModelDTO.Book retrieveBook(int bookId);
    Sentence retrieveSentenceById(int sentenceId);
    java.util.List<java.util.Map<String, String>> getSentencesByToken(String tokenText);
    java.util.List<String> getLemmasByRoot(String rootText);

}