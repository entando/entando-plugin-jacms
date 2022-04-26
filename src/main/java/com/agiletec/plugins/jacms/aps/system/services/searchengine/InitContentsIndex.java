package com.agiletec.plugins.jacms.aps.system.services.searchengine;

import com.agiletec.aps.system.common.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;

public class InitContentsIndex extends AbstractService {

@Autowired
    private SearchEngineManager searchEngineManager;
    @Override
    public void init() throws Exception {
        searchEngineManager.startReloadContentsReferences();
    }
}

