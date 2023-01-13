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
package org.entando.entando.plugins.jacms.apsadmin.content.bulk.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.agiletec.aps.system.services.category.Category;
import com.agiletec.apsadmin.ApsAdminBaseTestCase;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import java.util.ArrayList;
import java.util.List;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.ContentCategoryBulkAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestContentBulkActionHelper extends ApsAdminBaseTestCase {

    private static final String NAMESPACE = "/do/jacms/Content/Category";
    @Test
    void checkAllowedContentsShouldReturnFalseForEmptyorNullListOfContentIds() throws Throwable {
        this.initAction(NAMESPACE, "entry");
        ContentCategoryBulkAction validator = (ContentCategoryBulkAction)this.getAction();

        ContentBulkActionHelper actionHelper = new ContentBulkActionHelper();
        boolean isAllowed = actionHelper.checkAllowedContents(null, validator, validator);
        assertEquals(false, isAllowed);

        isAllowed = actionHelper.checkAllowedContents(new ArrayList<>(), validator, validator);
        assertEquals(false, isAllowed);

    }

    @Test
    void checkCategoriesShouldReturnFalseForEmptyOrNullListOfCategoryCodes() throws Throwable {
        this.initAction(NAMESPACE, "entry");
        ContentCategoryBulkAction validator = (ContentCategoryBulkAction)this.getAction();

        ContentBulkActionHelper actionHelper = new ContentBulkActionHelper();
        boolean isAllowed = actionHelper.checkCategories(null, validator, validator);
        assertEquals(false, isAllowed);

        isAllowed = actionHelper.checkCategories(new ArrayList<>(), validator, validator);
        assertEquals(false, isAllowed);

    }

    @Test
    void getCategoriesToManageShouldReturnNullForEmptyOrNullListOfCategoryCodes() throws Throwable {
        this.initAction(NAMESPACE, "entry");
        ContentCategoryBulkAction validator = (ContentCategoryBulkAction)this.getAction();

        ContentBulkActionHelper actionHelper = new ContentBulkActionHelper();
        List<Category> categories = actionHelper.getCategoriesToManage(null, validator, validator);
        assertNull(categories);

        categories = actionHelper.getCategoriesToManage(new ArrayList<>(), validator, validator);
        assertNull(categories);

    }

    @BeforeEach
    private void init() {
        this._contentManager = (IContentManager) this.getApplicationContext().getBean(JacmsSystemConstants.CONTENT_MANAGER);
    }

    private IContentManager _contentManager;

}