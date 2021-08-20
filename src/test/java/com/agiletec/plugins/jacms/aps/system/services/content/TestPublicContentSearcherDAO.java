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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.agiletec.aps.system.services.group.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.agiletec.aps.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author E.Santoboni
 */
class TestPublicContentSearcherDAO extends BaseTestCase {
	
	@Test
    void testLoadContentsId_1() throws Throwable {
    	List<String> list = null;
		try {
			list = _contentSearcherDao.loadContentsId("ART", null, false, null, Arrays.asList("free"));
		} catch (Throwable t) {
			throw t;
		}
		assertEquals(4, list.size());
		assertFalse(list.contains("ART179"));//contenuto non on-line
		assertTrue(list.contains("ART180"));
		assertTrue(list.contains("ART187"));
		assertTrue(list.contains("ART121"));//contenuto administrator abilitato ai free
		assertFalse(list.contains("ART102"));//contenuto di gruppo customers
	}
    
    @Test
    void testLoadContentsId_2() throws Throwable {
		List<String> list = null;
		try {
			List<String> groups = new ArrayList<>();
			groups.add("customers");
			groups.add("free");
			list = _contentSearcherDao.loadContentsId("ART", null, false, null, groups);
		} catch (Throwable t) {
			throw t;
		}
		assertEquals(8, list.size());
		assertFalse(list.contains("ART179"));//contenuto non on-line
		assertTrue(list.contains("ART111"));
		assertTrue(list.contains("ART180"));
		assertTrue(list.contains("ART187"));
		assertTrue(list.contains("ART121"));//contenuto administrator abilitato ai free
		assertTrue(list.contains("ART122"));//contenuto administrator abilitato ai customers
		assertTrue(list.contains("ART102"));
		assertTrue(list.contains("ART112"));
	}
	
    @BeforeEach
	private void init() throws Exception {
		try {
			_contentSearcherDao = new PublicContentSearcherDAO();
			DataSource dataSource = (DataSource) this.getApplicationContext().getBean("portDataSource");
			((PublicContentSearcherDAO) _contentSearcherDao).setDataSource(dataSource);
		} catch (Throwable t) {
			throw new Exception(t);
		}
	}
    
	private PublicContentSearcherDAO _contentSearcherDao;
    
}
