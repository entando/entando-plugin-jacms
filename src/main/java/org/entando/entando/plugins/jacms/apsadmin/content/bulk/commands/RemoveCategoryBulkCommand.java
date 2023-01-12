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
import com.agiletec.aps.system.services.category.Category;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;

import java.util.Collection;

public class RemoveCategoryBulkCommand extends BaseContentPropertyBulkCommand<Category> {

    public static final String BEAN_NAME = "jacmsRemoveCategoryBulkCommand";
    public static final String COMMAND_NAME = "content.category.rem";

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    protected boolean apply(Content content) throws EntException {
        Collection<Category> categories = this.getItemProperties();
        if (null == categories || categories.isEmpty()) {
            this.getErrors().put(content.getId(), ApsCommandErrorCode.PARAMS_NOT_VALID);
            return false;
        } else {
            for (Category category : categories) {
                if (null != category && !category.getCode().equals(category.getParentCode())) {
                    content.removeCategory(category);
                }
            }
            this.getApplier().saveContent(content);
        }
        return true;
    }

}