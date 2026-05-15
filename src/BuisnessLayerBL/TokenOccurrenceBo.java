// In BuisnessLayerBL/TokenOccurrenceBo.java
package BuisnessLayerBL;

import DataAcessLayerDAL.IDataFacade;
import java.util.List;
import java.util.Map;

public class TokenOccurrenceBo implements ITokenOccurrenceBo {
    private final IDataFacade dataFacade;

    public TokenOccurrenceBo(IDataFacade dataFacade) {
        this.dataFacade = dataFacade;
    }

    @Override
    public List<Map<String, String>> getSentencesByToken(String tokenText) {
        if (tokenText == null || tokenText.trim().isEmpty()) {
            return List.of();
        }
        return dataFacade.findSentencesByToken(tokenText.trim());
    }
}