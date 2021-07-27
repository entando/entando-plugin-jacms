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
package com.agiletec.plugins.jacms.apsadmin.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.agiletec.aps.system.SystemConstants;
import java.util.List;

import com.agiletec.aps.system.common.entity.model.EntitySearchFilter;
import com.agiletec.aps.system.common.entity.model.FieldError;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.group.IGroupManager;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.apsadmin.content.util.AbstractBaseTestContentAction;
import com.opensymphony.xwork2.Action;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author E.Santoboni
 */
class TestContentBulkAction extends AbstractBaseTestContentAction {
    
    private ILangManager langManager;
    private IGroupManager groupManager;

    @Override
    @BeforeEach
    protected void init() throws Exception {
        super.init();
        this.langManager = (ILangManager) super.getService(SystemConstants.LANGUAGE_MANAGER);
        this.groupManager = (IGroupManager) super.getService(SystemConstants.GROUP_MANAGER);
    }
    
    @Test
    void testBulkContent() throws Throwable {
        EntitySearchFilter typeFilter = new EntitySearchFilter(IContentManager.ENTITY_TYPE_CODE_FILTER_KEY, false, "ALL", false);
        EntitySearchFilter[] filters = {typeFilter};
        List<String> userGroups = new ArrayList<>();
        userGroups.add(Group.ADMINS_GROUP_NAME);
        
        List<String> contentIds = this.getContentManager().loadWorkContentsId(filters, userGroups);
        Assertions.assertEquals(1, contentIds.size());
        List<String> addedContents = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                Content content = this.getContentManager().loadContent("ALL4", false);
                content.setId(null);
                content.setDescription("__DESCR_TEST__ " + i);
                addedContents.add(this.getContentManager().addContent(content));
                List<FieldError> errors = content.validate(this.groupManager, this.langManager);
                assertEquals(0, errors.size());
            }
            contentIds = this.getContentManager().loadWorkContentsId(filters, userGroups);
            Assertions.assertEquals(5, contentIds.size());
            
            this.initAction("/do/jacms/Content/Bulk", "applyOnline");
            this.setUserOnSession("admin");
            this.addParameter("selectedIds", addedContents.toArray(new String[addedContents.size()]));
            String result = this.executeAction();
            assertEquals(Action.SUCCESS, result);
            contentIds = this.getContentManager().loadPublicContentsId(null, filters, userGroups);
            Assertions.assertEquals(5, contentIds.size());
        } catch (Throwable t) {
            throw t;
        } finally {
            for (int i = 0; i < addedContents.size(); i++) {
                String contentId = addedContents.get(i);
                Content content = this.getContentManager().loadContent(contentId, false);
                if (null != content) {
                    this.getContentManager().removeOnLineContent(content);
                    this.getContentManager().deleteContent(contentId);
                }
            }
        }
    }
    
}
