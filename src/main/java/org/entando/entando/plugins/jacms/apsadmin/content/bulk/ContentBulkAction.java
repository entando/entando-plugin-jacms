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
package org.entando.entando.plugins.jacms.apsadmin.content.bulk;

import java.util.Set;

import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.springframework.web.context.WebApplicationContext;

import com.agiletec.aps.util.ApsWebApplicationUtils;
import com.agiletec.apsadmin.system.BaseAction;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import java.util.Date;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands.BaseContentBulkCommand;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands.ContentBulkCommandContext;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands.DeleteContentBulkCommand;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands.InsertOnlineContentBulkCommand;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands.RemoveOnlineContentBulkCommand;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.report.DefaultBulkCommandReport;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.util.ContentBulkActionSummary;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.util.IContentBulkActionHelper;

public class ContentBulkAction extends BaseAction {

	private static final EntLogger _logger = EntLogFactory.getSanitizedLogger(ContentBulkAction.class);

	public String entry() {
		return this.checkAllowedContents() ? SUCCESS : "list";
	}

	public String applyOnline() {
		return this.apply(InsertOnlineContentBulkCommand.BEAN_NAME);
	}

	public String applyOffline() {
		return this.apply(RemoveOnlineContentBulkCommand.BEAN_NAME);
	}

	public String applyRemove() {
		return this.apply(DeleteContentBulkCommand.BEAN_NAME);
	}

	public String apply(String commandBeanName) {
		try {
			if (!this.checkAllowedContents()) {
				return "list";
			} else {
				BaseContentBulkCommand<ContentBulkCommandContext> command = this.initBulkCommand(commandBeanName);
				this.getSelectedIds().parallelStream().forEach(id -> {
					try {
						command.apply(id);
					} catch (Exception e) {
						_logger.error("Error executing " + command.getClass().getName() + " on content " + id);
					}
				});
				command.setEndingTime(new Date());
				this.setReport(command.getReport());
			}
		} catch (Throwable t) {
			_logger.error("Error occurred applying command {}", commandBeanName, t);
			return FAILURE;
		}
		return SUCCESS;
	}

	protected BaseContentBulkCommand<ContentBulkCommandContext> initBulkCommand(String commandBeanName) {
		WebApplicationContext applicationContext = ApsWebApplicationUtils.getWebApplicationContext(this.getRequest());
		BaseContentBulkCommand<ContentBulkCommandContext> command = (BaseContentBulkCommand<ContentBulkCommandContext>) applicationContext.getBean(commandBeanName);
		ContentBulkCommandContext context = new ContentBulkCommandContext(this.getSelectedIds(), this.getCurrentUser());
		command.init(context);
		return command;
	}

	public String viewResult() {
		return this.getReport() == null ? "expired" : SUCCESS;
	}

	public DefaultBulkCommandReport<String> getReport() {
		return report;
	}
	protected void setReport(DefaultBulkCommandReport<String> report) {
		this.report = report;
	}

	public ContentBulkActionSummary getSummary() {
		return this.getBulkActionHelper().getSummary(this.getSelectedIds());
	}

	protected boolean checkAllowedContents() {
		return this.getBulkActionHelper().checkAllowedContents(this.getSelectedIds(), this, this);
	}

	public Set<String> getSelectedIds() {
		return _selectedIds;
	}
	public void setSelectedIds(Set<String> selectedIds) {
		this._selectedIds = selectedIds;
	}

	protected IContentManager getContentManager() {
		return _contentManager;
	}
	public void setContentManager(IContentManager contentManager) {
		this._contentManager = contentManager;
	}

	protected IContentBulkActionHelper getBulkActionHelper() {
		return _bulkActionHelper;
	}
	public void setBulkActionHelper(IContentBulkActionHelper bulkActionHelper) {
		this._bulkActionHelper = bulkActionHelper;
	}

	private Set<String> _selectedIds;

	private IContentManager _contentManager;
	private IContentBulkActionHelper _bulkActionHelper;

	private DefaultBulkCommandReport<String> report;

}