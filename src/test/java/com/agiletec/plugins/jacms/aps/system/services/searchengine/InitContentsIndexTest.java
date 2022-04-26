package com.agiletec.plugins.jacms.aps.system.services.searchengine;

import com.agiletec.aps.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class InitContentsIndexTest extends BaseTestCase {
    @Mock
    private SearchEngineManager searchEngineManager;

    @InjectMocks
    private InitContentsIndex initContentsIndex;

    @BeforeEach
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitMethod() throws Exception {
        initContentsIndex.init();
        Mockito.verify(this.searchEngineManager, Mockito.times(1)).startReloadContentsReferences();
    }
}