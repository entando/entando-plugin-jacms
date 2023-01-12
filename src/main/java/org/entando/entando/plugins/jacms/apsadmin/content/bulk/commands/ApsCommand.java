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

import java.util.Date;

/**
 * Class that applies a command on one or multiple items.
 *
 * @author E.Mezzano
 */
public interface ApsCommand<C extends BulkCommandContext> {

    /**
     * Returns the conventional name of the given command.
     * @return The conventional name of the given command.
     */
    public String getName();

    /**
     * Init the context of the command.
     * @param context The context of the command.
     */
    public void init(C context);

    /**
     * Returns the instant of the end of the command.
     * @return The instant of the end of the command.
     */
    public Date getEndingTime();

    public void setEndingTime(Date date);

}