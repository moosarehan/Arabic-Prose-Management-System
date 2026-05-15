// In BuisnessLayerBL/ITokenOccurrenceBo.java
package BuisnessLayerBL;

import java.util.List;
import java.util.Map;

public interface ITokenOccurrenceBo {
    List<Map<String, String>> getSentencesByToken(String tokenText);
}