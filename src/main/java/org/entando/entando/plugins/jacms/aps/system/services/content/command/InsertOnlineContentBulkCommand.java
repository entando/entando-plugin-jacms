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
package org.entando.entando.plugins.jacms.aps.system.services.content.command;

import com.agiletec.aps.system.common.entity.model.FieldError;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.aps.system.services.group.IGroupManager;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import org.apache.commons.lang3.StringUtils;
import org.entando.entando.aps.system.common.command.constants.ApsCommandErrorCode;
import org.entando.entando.plugins.jacms.aps.system.services.content.command.common.BaseContentBulkCommand;
import org.entando.entando.plugins.jacms.aps.system.services.content.command.common.ContentBulkCommandContext;

import java.util.List;

public class InsertOnlineContentBulkCommand extends BaseContentBulkCommand<ContentBulkCommandContext> {

	public static final String BEAN_NAME = "jacmsInsertOnlineContentBulkCommand";
	public static final String COMMAND_NAME = "content.online";

	@Override
	public String getName() {
		return COMMAND_NAME;
	}

	@Override
	protected boolean apply(Content content) throws EntException {
		if (!this.validateContent(content)) {
			this.getTracer().traceError(content.getId(), ApsCommandErrorCode.NOT_APPLICABLE);
			return false;
		} else {
			this.getApplier().insertOnLineContent(content);
			this.getTracer().traceSuccess(content.getId());
			return true;
		}
	}

	protected boolean validateContent(Content content) throws EntException {
		boolean valid = this.validateDescription(content) && this.checkContentUtilizers(content);
		if (valid) {
			List<FieldError> errors = content.validate(this.getGroupManager(), this.getLangManager());
			valid = errors == null || errors.isEmpty();
		}
		return valid;
	}

	protected boolean validateDescription(Content content) {
		boolean valid = false;
		String descr = content.getDescription();
		int maxLength = 250;
		String regex = "([^\"])+";
    	if (StringUtils.isNotEmpty(descr) && descr.length() <= maxLength && descr.matches(regex)) {
    		valid = true;
		}
		return valid;
	}

	protected IGroupManager getGroupManager() {
		return _groupManager;
	}
	public void setGroupManager(IGroupManager groupManager) {
		this._groupManager = groupManager;
	}

    protected ILangManager getLangManager() {
        return langManager;
    }
    public void setLangManager(ILangManager langManager) {
        this.langManager = langManager;
    }
    
	private IGroupManager _groupManager;
	private ILangManager langManager;

}
