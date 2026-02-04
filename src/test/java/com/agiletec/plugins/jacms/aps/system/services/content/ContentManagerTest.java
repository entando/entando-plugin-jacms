/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.agiletec.plugins.jacms.aps.system.services.content;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.parse.IEntityTypeFactory;
import com.agiletec.aps.system.common.notify.INotifyManager;
import com.agiletec.aps.system.services.category.Category;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import com.agiletec.plugins.jacms.aps.system.services.content.parse.ContentDOM;
import com.agiletec.plugins.jacms.aps.system.services.content.parse.ContentTypeDOM;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.function.Supplier;
import org.entando.entando.ent.exception.EntException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;

@ExtendWith(MockitoExtension.class)
class ContentManagerTest {

    @Mock
    private IEntityTypeFactory entityTypeFactory;

    @Mock
    private ContentTypeDOM entityTypeDom;

    @Mock
    private ContentDOM entityDom;

    @Mock
    private ContentDAO contentDAO;

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private INotifyManager notifyManager;

    @Mock
    private Cache<String, Object> localCache;

    private String beanName = "jacmsContentManager";

    private String className = "com.agiletec.plugins.jacms.aps.system.services.content.model.Content";

    @InjectMocks
    private ContentManager contentManager;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.contentManager.setEntityClassName(className);
        this.contentManager.setConfigItemName(JacmsSystemConstants.CONFIG_ITEM_CONTENT_TYPES);
        this.contentManager.setBeanName(this.beanName);
    }

    @Test
    void testCreateContent() throws EntException {
        String typeCode = "ART";
        // @formatter:off
        when(entityTypeFactory.extractEntityType(
                                                 typeCode,
                                                 Content.class,
                                                 contentManager.getConfigItemName(),
                                                 this.entityTypeDom,
                                                 contentManager.getName(),
                                                 this.entityDom))
        .thenReturn(this.createFakeEntity(typeCode, "contentview", "1"));
        // @formatter:on
        Content content = this.contentManager.createContentType(typeCode);
        assertThat(content, is(not(nullValue())));
        assertThat(content.getViewPage(), is("contentview"));
        assertThat(content.getDefaultModel(), is("1"));
    }

    @Test
    void testGetXML() throws Throwable {
        String typeCode = "ART";
        // @formatter:off
        when(entityTypeFactory.extractEntityType(
                                                 typeCode,
                                                 Content.class,
                                                 contentManager.getConfigItemName(),
                                                 this.entityTypeDom,
                                                 contentManager.getName(),
                                                 this.entityDom))
        .thenReturn(this.createFakeEntity(typeCode, "contentview", "1"));
        // @formatter:on
        Content content = contentManager.createContentType(typeCode);
        ContentDOM contentDOM = new ContentDOM();
        contentDOM.setRootElementName("content");
        content.setEntityDOM(contentDOM);


        content.setId("ART1");
        content.setTypeCode("Articolo");
        content.setTypeDescription("Articolo");
        content.setDescription("descrizione");
        content.setStatus(Content.STATUS_DRAFT);
        content.setMainGroup("free");
        Category cat13 = new Category();
        cat13.setCode("13");
        content.addCategory(cat13);
        Category cat19 = new Category();
        cat19.setCode("19");
        content.addCategory(cat19);
        String xml = content.getXML();

        assertNotNull(xml);
        assertTrue(xml.indexOf("<content id=\"ART1\" typecode=\"Articolo\" typedescr=\"Articolo\">") != -1);
        assertTrue(xml.indexOf("<descr>descrizione</descr>") != -1);
        assertTrue(xml.indexOf("<status>" + Content.STATUS_DRAFT + "</status>") != -1);
        assertTrue(xml.indexOf("<category id=\"13\" />") != -1);
        assertTrue(xml.indexOf("<category id=\"19\" />") != -1);
    }

    @Test
    void testLoadAndCacheContentVO() throws EntException {
        String contentId = "ART1";
        ContentRecordVO vo = new ContentRecordVO();
        vo.setId(contentId);
        when(contentDAO.loadEntityRecord(contentId)).thenReturn(vo);
        
        ContentRecordVO result = contentManager.loadAndCacheContentVO(contentId);
        
        assertNotNull(result);
        assertThat(result.getId(), is(contentId));
    }

    @Test
    void testLoadAndCacheContentVO_Error() {
        String contentId = "ART1";
        when(contentDAO.loadEntityRecord(contentId)).thenThrow(new RuntimeException("DB Error"));
        
        assertThrows(EntException.class, () -> contentManager.loadAndCacheContentVO(contentId));
    }

    @Test
    void testLoadAndCacheContent_OnLine() throws Exception {
        String contentId = "ART1";
        String typeCode = "ART";
        String xmlOnLine = "<xml>online</xml>";
        
        ContentRecordVO vo = new ContentRecordVO();
        vo.setId(contentId);
        vo.setTypeCode(typeCode);
        vo.setXmlOnLine(xmlOnLine);
        vo.setOnLine(true);
        
        // Mocking createEntityFromXml indirectly by mocking its behavior in createContent
        ContentManager spyContentManager = spy(contentManager);
        Content content = new Content();
        content.setId(contentId);
        content.setTypeCode(typeCode);
        
        when(contentDAO.loadEntityRecord(contentId)).thenReturn(vo);
        org.mockito.Mockito.doReturn(content).when(spyContentManager).createContent(eq(vo), eq(true));
        
        Content result = spyContentManager.loadAndCacheContent(contentId, true);
        
        assertNotNull(result);
        assertThat(result.getId(), is(contentId));
    }

    @Test
    void testLoadAndCacheContent_Work() throws Exception {
        String contentId = "ART1";
        String typeCode = "ART";
        String xmlWork = "<xml>work</xml>";
        
        ContentRecordVO vo = new ContentRecordVO();
        vo.setId(contentId);
        vo.setTypeCode(typeCode);
        vo.setXmlWork(xmlWork);
        vo.setOnLine(false);
        
        ContentManager spyContentManager = spy(contentManager);
        Content content = new Content();
        content.setId(contentId);
        content.setTypeCode(typeCode);

        when(contentDAO.loadEntityRecord(contentId)).thenReturn(vo);
        org.mockito.Mockito.doReturn(content).when(spyContentManager).createContent(eq(vo), eq(false));

        Content result = spyContentManager.loadAndCacheContent(contentId, false);

        assertNotNull(result);
        assertThat(result.getId(), is(contentId));

        // again!
        result = spyContentManager.loadAndCacheContent(contentId, false);
        assertNotNull(result);
        assertThat(result.getId(), is(contentId));
    }

    @Test
    void testLoadAndCacheContent_NullVO() throws EntException {
        String contentId = "ART1";
        when(contentDAO.loadEntityRecord(contentId)).thenReturn(null);

        Content result = contentManager.loadAndCacheContent(contentId, true);
        lenient().when(localCache.get(eq(contentId), any())).thenReturn(null);

        assertNull(result);
    }

    @Test
    void testLoadAndCacheContentVO_CacheInteraction() throws EntException {
        String contentId = "ART1";
        ContentRecordVO vo = new ContentRecordVO();
        vo.setId(contentId);

        try (MockedStatic<IFContentLocalCache> mockedCache = mockStatic(IFContentLocalCache.class)) {
            mockedCache.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedCache.when(() -> IFContentLocalCache.instantiateLocalCache(
                    anyLong(),
                    anyLong(),
                    any(Boolean.class)
            )).thenCallRealMethod();
            mockedCache.when(() -> IFContentLocalCache.loadAndCacheContentVO(
                    anyString(),
                    any(Cache.class),
                    any(Supplier.class)
            )).thenCallRealMethod();

            // Mock the DAO to return the VO when loadEntityRecord is called
            when(contentDAO.loadEntityRecord(contentId)).thenReturn(vo);

            // Mock localCache.get to execute the mapping function (which calls the supplier/DAO)
            when(localCache.get(eq(contentId), any())).thenAnswer(invocation -> {
                java.util.function.Function<String, Object> mappingFunction = invocation.getArgument(1);
                return mappingFunction.apply(invocation.getArgument(0));
            });

            ContentRecordVO result = contentManager.loadAndCacheContentVO(contentId);

            assertNotNull(result);
            assertThat(result.getId(), is(contentId));
            // Verify that the cache was used
            verify(localCache, atLeastOnce()).get(eq(contentId), any());
        }
    }

    @Test
    void testEvict_CacheInteraction() {
        String contentId = "ART1";

        try (MockedStatic<IFContentLocalCache> mockedCache = mockStatic(IFContentLocalCache.class)) {
            mockedCache.when(IFContentLocalCache::checkEnabled).thenReturn(true);
            mockedCache.when(() -> IFContentLocalCache.evict(eq(contentId), any(Cache.class)))
                    .thenCallRealMethod();

            contentManager.evict(contentId);

            // Verifichiamo che localCache.invalidate sia stato chiamato
            verify(localCache).invalidate(contentId);
        }
    }


    private IApsEntity createFakeEntity(String typeCode, String viewPage, String defaultModel) {
        Content content = new Content();
        content.setTypeCode(typeCode);
        content.setViewPage(viewPage);
        content.setDefaultModel(defaultModel);
        return content;
    }
}
