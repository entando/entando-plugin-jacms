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
package org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands;

import org.entando.entando.ent.exception.EntException;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import org.entando.entando.plugins.jacms.aps.system.services.content.helper.IContentHelper;

import java.util.List;
import java.util.Map;

public class RemoveOnlineContentBulkCommand extends BaseContentBulkCommand<ContentBulkCommandContext> {

    public static final String BEAN_NAME = "jacmsRemoveOnlineContentBulkCommand";
    public static final String COMMAND_NAME = "content.offline";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    protected boolean apply(Content content) throws EntException {
        if (!this.validateContent(content)) {
            markErrorNotApplicable(content.getId());
            return false;
        } else {
            this.getApplier().removeOnLineContent(content);
            return true;
        }
    }

    protected boolean validateContent(Content content) throws EntException {
        Map<String, List<?>> references = this.getContentHelper().getReferencingObjects(content, this.getContentUtilizers());
        return references == null || references.isEmpty();
    }

    protected IContentHelper getContentHelper() {
        return contentHelper;
    }
    public void setContentHelper(IContentHelper contentHelper) {
        this.contentHelper = contentHelper;
    }

    private IContentHelper contentHelper;

}