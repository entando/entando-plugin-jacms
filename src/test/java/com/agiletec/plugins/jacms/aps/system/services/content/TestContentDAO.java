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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.sql.DataSource;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.aps.system.common.entity.model.attribute.MonoTextAttribute;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test del Data Access Object per gli oggetti contenuto (Content).
 * @version 1.0
 * @author M.Morini - S.Didaci - E.Santoboni
 */
class TestContentDAO extends BaseTestCase {
	
	@Test
    void testDeleteAddContent() throws Throwable {
    	try {
			Content mockContent = this.getMockContent();
			this.deleteContent(mockContent);
			this.addContent(mockContent);
		} catch (Throwable e) {
			throw e;
		}
    }
	
	private void deleteContent(Content content) throws EntException {
		this._contentDao.deleteEntity(content.getId());
		ContentRecordVO contentRecord = (ContentRecordVO) this._contentDao.loadEntityRecord(content.getId());
		assertNull(contentRecord);
	}
	
	private void addContent(Content mockContent) throws EntException {
		_contentDao.addEntity(mockContent);
		ContentRecordVO contentRecord = (ContentRecordVO) this._contentDao.loadEntityRecord(mockContent.getId());
		assertEquals(mockContent.getDescription(), contentRecord.getDescr());
		assertEquals(mockContent.getStatus(), contentRecord.getStatus());
		assertFalse(contentRecord.isOnLine());
	}
	
	@Test
    void testGetAllContentIds() throws Throwable {
		List<String> contentIds1 = this._contentDao.getAllEntityId();
		List<String> contentIds2 = this._contentManager.searchId(null);
		assertEquals(contentIds1.size(), contentIds2.size());
		for (int i = 0; i < contentIds1.size(); i++) {
			String contentId = contentIds1.get(i);
			assertTrue(contentIds2.contains(contentId));
		}
	}
	
	@Test
    void testInsertRemoveOnlineContent() throws Throwable {
    	try {
			Content mockContent = this.getMockContent();
			this.insertOnLineContent(mockContent);
			this.getAllContentsOnLine(mockContent);
	    	this.removeOnLineContent(mockContent);
		} catch (Throwable e) {
			throw e;
		}
    }
	
	private void insertOnLineContent(Content mockContent) throws EntException {
		this._contentDao.insertOnLineContent(mockContent);
		ContentRecordVO contentRecord = (ContentRecordVO) this._contentDao.loadEntityRecord(mockContent.getId());
		assertTrue(contentRecord.isOnLine());
	}
	
	private void getAllContentsOnLine(Content mockContent) throws EntException {  
		List<String> list = this._contentDao.getAllEntityId();
        assertTrue(list.contains(mockContent.getId()));
    }
    
	private void removeOnLineContent(Content content) throws EntException {
		this._contentDao.removeOnLineContent(content);
		ContentRecordVO contentRecord = (ContentRecordVO) this._contentDao.loadEntityRecord(content.getId());
		assertFalse(contentRecord.isOnLine());
	}
	
	@Test
    void testUpdateContent() throws Throwable {
    	try {
			Content mockContent = this.getMockContent();
			mockContent.setDescription("New Description");
			mockContent.setStatus(Content.STATUS_READY);
			this.updateContent(mockContent);
		} catch (Throwable t) {
			throw t;
		}
    }
	
	@Test
    void testGetPageUtilizers() throws Throwable {
		List<String> contentIds = _contentDao.getPageUtilizers("pagina_11");
		assertNotNull(contentIds);
		assertEquals(2, contentIds.size());
		String contentId = contentIds.get(0);
		assertEquals("EVN193", contentId);
    }
	
	@Test
    void testGetContentUtilizers() throws Throwable {
		List<String> contentIds = _contentDao.getContentUtilizers("ART1");
		assertNotNull(contentIds);
		assertEquals(2, contentIds.size());
		String contentId = contentIds.get(0);
		assertEquals("EVN193", contentId);
		contentId = contentIds.get(1);
		assertEquals("EVN194", contentId);
    }
	
	@Test
    void testGetGroupUtilizers() throws Throwable {
		List<String> contentIds = _contentDao.getGroupUtilizers("customers");
		assertNotNull(contentIds);
		assertEquals(5, contentIds.size());
		assertTrue(contentIds.contains("ART102"));
		assertTrue(contentIds.contains("ART111"));
		assertTrue(contentIds.contains("ART122"));
		assertTrue(contentIds.contains("RAH101"));
		assertTrue(contentIds.contains("ART112"));
    }
	
	@Test
    void testGetResourceUtilizers() throws Throwable {
		List<String> contentIds = _contentDao.getResourceUtilizers("44");
		assertNotNull(contentIds);
		assertEquals(3, contentIds.size());
		assertTrue(contentIds.contains("ART1"));
		assertTrue(contentIds.contains("ART180"));
		assertTrue(contentIds.contains("ALL4"));
    }
	
	private void updateContent(Content mockContent) throws EntException {
		this._contentDao.updateEntity(mockContent);
		ContentRecordVO contentRecord = (ContentRecordVO) this._contentDao.loadEntityRecord(mockContent.getId());
		assertEquals(mockContent.getDescription(), contentRecord.getDescription());
		assertEquals(mockContent.getStatus(), contentRecord.getStatus());
		assertFalse(contentRecord.isOnLine());
	}
	
	private Content getMockContent() { 
        Content content = this._contentManager.createContentType("ART");
        
        content.setId("temp");
        content.setMainGroup(Group.FREE_GROUP_NAME);
        
        content.addGroup("firstGroup");
        content.addGroup("secondGroup");
        content.addGroup("thirdGroup");
        
    	AttributeInterface attribute = new MonoTextAttribute();
    	attribute.setName("temp");
    	attribute.setDefaultLangCode("it");
    	attribute.setRenderingLang("it");
    	attribute.setSearchable(true);
    	attribute.setType("Monotext");
    	content.addAttribute(attribute);	
    	content.setDefaultLang("it");
    	content.setDefaultModel("content_viewer");
    	content.setDescription("temp");
    	content.setListModel("Monolist");
    	content.setRenderingLang("it");
    	content.setStatus("Bozza");
    	content.setTypeCode("ART");
    	content.setTypeDescription("Articolo rassegna stampa");
    	return content;
    }
    
    @AfterEach
    private void dispose() throws Exception {
		Content mockContent = this.getMockContent();
		try {
			this._contentDao.deleteEntity(mockContent.getId());
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}
    
    @BeforeEach
    private void init() throws Exception {
		this._contentDao = new ContentDAO();
		try {
			this._contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
			Content mockContent = this.getMockContent();
			DataSource dataSource = (DataSource) this.getApplicationContext().getBean("portDataSource");
			this._contentDao.setDataSource(dataSource);
			ILangManager langManager = (ILangManager) this.getService(SystemConstants.LANGUAGE_MANAGER);
			this._contentDao.setLangManager(langManager);
			this._contentDao.addEntity(mockContent);
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}
    
    private ContentDAO _contentDao;
    
    private IContentManager _contentManager;
	
}
