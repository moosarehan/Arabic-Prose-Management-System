// src/BuisnessLayerBL/LemmatizationBo.java
package BuisnessLayerBL;

import util.AlKhalilLemmatizerUtil;
import java.util.Map;
import util.LemmatizationUtil;

public class LemmatizationBo implements ILemmatizationBo {
    
    private final LemmatizationUtil lemmatizationUtil;

    public LemmatizationBo() {
        this.lemmatizationUtil = LemmatizationUtil.getInstance();
    }

    @Override
    public String lemmatizeToken(String tokenText) {
        return lemmatizationUtil.lemmatize(tokenText);
    }

}