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
package com.agiletec.plugins.jacms.aps.system.services.contentpagemapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.agiletec.aps.BaseTestCase;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @version 1.0
 * @author M.Casari
 */
class ContentPageMapperManagerIntegrationTest extends BaseTestCase {
	
    @Test
    void testGetContentPageMapper() throws EntException {
		String codePage = _contentPageMapperManager.getPageCode("ART1");
		assertEquals(codePage, "homepage");
	}
	
    @Test
    void testReloadContentPageMapper() throws EntException{   
        _contentPageMapperManager.reloadContentPageMapper();
        String codePage = _contentPageMapperManager.getPageCode("ART1");
        assertEquals(codePage, "homepage");
    }
    
    @BeforeEach
    private void init() throws Exception {
    	try {
    		_contentPageMapperManager = (IContentPageMapperManager) this.getService(JacmsSystemConstants.CONTENT_PAGE_MAPPER_MANAGER);
    	} catch (Throwable t) {
            throw new Exception();
        }
    }
    
    private IContentPageMapperManager _contentPageMapperManager = null;
    
}
