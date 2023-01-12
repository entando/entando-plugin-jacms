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

import java.util.Collection;

import com.agiletec.aps.system.services.user.UserDetails;

public class ContentPropertyBulkCommandContext<P> extends ContentBulkCommandContext {

    public ContentPropertyBulkCommandContext(Collection<String> items, Collection<P> itemProperties, UserDetails currentUser) {
        super(items, currentUser);
        this.setItemProperties(itemProperties);
    }

    public Collection<P> getItemProperties() {
        return itemProperties;
    }
    protected void setItemProperties(Collection<P> itemProperties) {
        this.itemProperties = itemProperties;
    }

    private Collection<P> itemProperties;

}