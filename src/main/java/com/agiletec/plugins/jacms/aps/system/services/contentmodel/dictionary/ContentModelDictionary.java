package com.agiletec.plugins.jacms.aps.system.services.contentmodel.dictionary;

import java.util.List;
import java.util.Properties;

import com.agiletec.aps.system.common.entity.model.IApsEntity;
import org.entando.entando.aps.system.services.dataobjectmodel.dictionary.DataModelDictionary;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;


public class ContentModelDictionary extends DataModelDictionary {


    private final EntLogger logger = EntLogFactory.getSanitizedLogger(getClass());

    private static final String KEY_ROOT = "$content";

    @Override
    public String getEntityRootName() {
        return KEY_ROOT;
    }

    public ContentModelDictionary(List<String> contentConfig, List<String> i18nConfig, List<String> infoConfig, List<String> commonConfig, Properties publicAttributeMethods, IApsEntity prototype) {
        super(contentConfig, i18nConfig, infoConfig, commonConfig, publicAttributeMethods, prototype);
    }


}
