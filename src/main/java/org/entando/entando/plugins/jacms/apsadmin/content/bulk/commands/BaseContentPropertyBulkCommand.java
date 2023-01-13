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

import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import java.util.Collection;
import org.entando.entando.ent.exception.EntException;

public abstract class BaseContentPropertyBulkCommand<P> extends BaseContentBulkCommand<ContentPropertyBulkCommandContext<P>> {

    protected void markErrorNotValid(String key) {
        this.getErrors().put(key, ApsCommandErrorCode.PARAMS_NOT_VALID);
    }

    public Collection<P> getItemProperties() {
        return this.getContext().getItemProperties();
    }

    protected boolean isItemPropertiesEmpty() {
        Collection<P> items = this.getContext().getItemProperties();
        return items == null || items.isEmpty() ;
    }

    protected boolean apply(Content content) throws EntException {
        Collection<P> items = this.getItemProperties();
        if (isItemPropertiesEmpty()) {
            markErrorNotApplicable(content.getId());
            return false;
        } else {
            for (P item :items) {
                manageValidItem(content, item);
            }
            this.getApplier().saveContent(content);
        }
        return true;
    }

    protected abstract void manageValidItem(Content content, P item);
}