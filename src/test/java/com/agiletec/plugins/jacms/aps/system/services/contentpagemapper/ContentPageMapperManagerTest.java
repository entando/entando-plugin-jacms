/*
 * Copyright 2018-Present Entando S.r.l. (http://www.entando.com) All rights reserved.
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

import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.aps.system.services.page.events.PageChangedEvent;
import com.agiletec.plugins.jacms.aps.system.services.contentpagemapper.cache.ContentMapperCacheWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author E.Santoboni
 */
@ExtendWith(MockitoExtension.class)
class ContentPageMapperManagerTest {

	@Mock
	private IPageManager pageManager;

	@Mock
	private ContentMapperCacheWrapper cacheWrapper;

	@InjectMocks
	private ContentPageMapperManager pageMapperManager;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testInit() throws Throwable {
		pageMapperManager.init();
		Mockito.verify(cacheWrapper, Mockito.times(1)).initCache(pageManager);
	}

	@Test
	void testReload() throws Throwable {
		pageMapperManager.reloadContentPageMapper();
		Mockito.verify(cacheWrapper, Mockito.times(1)).initCache(pageManager);
	}

	@Test
	void testGetPage() throws Throwable {
		Mockito.when(cacheWrapper.getPageCode(Mockito.anyString())).thenReturn("pageCode");
		String pageCode = this.pageMapperManager.getPageCode("ART1");
		Assertions.assertNotNull(pageCode);
		Assertions.assertEquals("pageCode", pageCode);
	}

	@Test
	void testUpdate() throws Throwable {
		pageMapperManager.updateFromPageChanged(Mockito.any(PageChangedEvent.class));
		Mockito.verify(cacheWrapper, Mockito.times(1)).initCache(pageManager);
	}

}
