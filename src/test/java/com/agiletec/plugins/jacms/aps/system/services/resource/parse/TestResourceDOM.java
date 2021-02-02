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
package com.agiletec.plugins.jacms.aps.system.services.resource.parse;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.agiletec.aps.BaseTestCase;
import org.entando.entando.ent.exception.EntException;
import org.junit.jupiter.api.Test;

/**
 * @version 1.0
 * @author M. Morini
 */
class TestResourceDOM extends BaseTestCase {
	
    @Test
    void testGetXMLDocument() throws EntException {  
		ResourceDOM resourceDom = new ResourceDOM();
        resourceDom.addCategory("tempcategory");
        int index = resourceDom.getXMLDocument().indexOf("tempcategory");
        assertTrue(index != -1);
    }
    
}
