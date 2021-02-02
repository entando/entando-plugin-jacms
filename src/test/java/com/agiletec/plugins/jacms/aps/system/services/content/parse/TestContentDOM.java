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
package com.agiletec.plugins.jacms.aps.system.services.content.parse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.agiletec.aps.BaseTestCase;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @version 1.0
 * @author M. Morini
 */
class TestContentDOM extends BaseTestCase {
	
	@Test
    void testGetXMLDocument() throws EntException {
    	Content contentTest = this._contentManager.createContentType("ART");
    	assertNotNull(contentTest);
    	contentTest.addGroup("tempGroupName");
    	String xml = contentTest.getXML();
		int index = xml.indexOf("tempGroupName");
		assertTrue((index != -1));
	}
    
    @BeforeEach
    private void init() throws Exception {
		try {
			_contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
		} catch (Throwable t) {
			throw new Exception(t);
		}
	}

	private IContentManager _contentManager = null;
    
}
