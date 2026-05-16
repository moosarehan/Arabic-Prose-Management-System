
 package DataAcessLayerDAL;

import java.util.List;
import java.util.Map;

public interface IDataFacade extends IAuthorDOA, IBookDOA, ISentenceDOA, IChapterDOA, ITokenDOA, ISegmentationDOA, IRootDOA {
	// In DataAcessLayerDAL/IDataFacade.java, add:
	List<Map<String, String>> findSentencesByToken(String tokenText);
	
	/**
	 * Regex-based search on the sentences table using database REGEXP.
	 */
	List<Map<String, String>> searchSentencesByRegex(String pattern);
}
