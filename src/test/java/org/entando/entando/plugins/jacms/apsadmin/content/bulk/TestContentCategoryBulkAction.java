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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.entando.entando.ent.exception.EntException;
import com.agiletec.aps.system.services.category.Category;
import com.agiletec.apsadmin.ApsAdminBaseTestCase;
import com.agiletec.apsadmin.system.ApsAdminSystemConstants;
import com.agiletec.apsadmin.system.BaseAction;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.opensymphony.xwork2.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestContentCategoryBulkAction extends ApsAdminBaseTestCase {

	@Test
	void testUserNotAllowed() throws Throwable {
		String[] contentIds = {"ART1", "RAH101", "EVN103"};
		String[] categoryCodes = {"cat1"};
		int strutsAction = ApsAdminSystemConstants.ADD;
		String currentUser = "pageManagerCoach";

		String result = this.executeEntry(currentUser, strutsAction, contentIds);
		assertEquals(BaseAction.USER_NOT_ALLOWED, result);

		result = this.executeJoinCategory(currentUser, strutsAction, contentIds, categoryCodes, "evento");
		assertEquals(BaseAction.USER_NOT_ALLOWED, result);

		result = this.executeDisjoinCategory(currentUser, strutsAction, contentIds, categoryCodes, "evento");
		assertEquals(BaseAction.USER_NOT_ALLOWED, result);

		result = this.executeCheckApply(currentUser, strutsAction, contentIds, categoryCodes);
		assertEquals(BaseAction.USER_NOT_ALLOWED, result);

		result = this.executeApply(currentUser, strutsAction, contentIds, categoryCodes);
		assertEquals(BaseAction.USER_NOT_ALLOWED, result);

		result = this.executeCheckResult(currentUser);
		assertEquals(BaseAction.USER_NOT_ALLOWED, result);
// FIXME
//		result = this.executeViewResult(currentUser);
//		assertEquals(BaseAction.USER_NOT_ALLOWED, result);
	}

	@Test
	void testEntryCheckApply() throws Throwable {
		String currentUser = "editorCustomers";
		String[] contentIds = new String[] {"ART1", "RAH101", "EVN103"};
		String[] categoryCodes = new String[] {"cat1", "evento"};

		String result = this.executeEntry(currentUser, ApsAdminSystemConstants.ADD, contentIds);
		assertEquals(Action.SUCCESS, result);
		this.checkItems(contentIds, ((ContentCategoryBulkAction) this.getAction()).getSelectedIds());

		result = this.executeEntry(currentUser, ApsAdminSystemConstants.DELETE, contentIds);
		assertEquals(Action.SUCCESS, result);
		this.checkItems(contentIds, ((ContentCategoryBulkAction) this.getAction()).getSelectedIds());

		result = this.executeCheckApply(currentUser, ApsAdminSystemConstants.ADD, contentIds, categoryCodes);
		assertEquals(Action.SUCCESS, result);
		this.checkItems(contentIds, ((ContentCategoryBulkAction) this.getAction()).getSelectedIds());
		this.checkItems(categoryCodes, ((ContentCategoryBulkAction) this.getAction()).getCategoryCodes());
	}

	@Test
	void applyShouldManageContentNotFoundOrContentNotAuth() throws Throwable {
		String currentUser = "editorCustomers";
		String[] contentIds = new String[] {"ART1_NOT_FOUND","EVN193"};
		String[] categoryCodes = new String[] {"cat1", "evento"};

		String result = this.executeApply(currentUser, ApsAdminSystemConstants.ADD, contentIds, categoryCodes);
		assertEquals(Action.SUCCESS, result);

	}
	@Test
	void testJoinDisjoin() throws Throwable {
		String[] contentIds = new String[] {"ART1", "RAH101", "EVN103"};
		String[] categoryCodes = new String[] {"cat1", "evento"};
		String username = "editorCustomers";

		this.executeJoinCategory(username, ApsAdminSystemConstants.ADD, contentIds, null, "general_cat1");
		Collection<String> foundCategoryCodes = ((ContentCategoryBulkAction) this.getAction()).getCategoryCodes();
		this.checkItems(new String[] {"general_cat1"}, foundCategoryCodes);

		this.executeJoinCategory(username, ApsAdminSystemConstants.DELETE, contentIds, categoryCodes, "evento");
		foundCategoryCodes = ((ContentCategoryBulkAction) this.getAction()).getCategoryCodes();
		this.checkItems(new String[] {"cat1", "evento"}, foundCategoryCodes);

		this.executeJoinCategory(username, ApsAdminSystemConstants.DELETE, contentIds, categoryCodes, "general_cat1");
		foundCategoryCodes = ((ContentCategoryBulkAction) this.getAction()).getCategoryCodes();
		this.checkItems(new String[] {"cat1", "evento", "general_cat1"}, foundCategoryCodes);

		this.executeDisjoinCategory(username, ApsAdminSystemConstants.ADD, contentIds, null, "evento");
		foundCategoryCodes = ((ContentCategoryBulkAction) this.getAction()).getCategoryCodes();
		this.checkItems(new String[] {}, foundCategoryCodes);

		this.executeDisjoinCategory(username, ApsAdminSystemConstants.DELETE, contentIds, categoryCodes, "evento");
		foundCategoryCodes = ((ContentCategoryBulkAction) this.getAction()).getCategoryCodes();
		this.checkItems(new String[] {"cat1"}, foundCategoryCodes);

		this.executeDisjoinCategory(username, ApsAdminSystemConstants.ADD, contentIds, categoryCodes, "general_cat1");
		foundCategoryCodes = ((ContentCategoryBulkAction) this.getAction()).getCategoryCodes();
		this.checkItems(new String[] {"cat1", "evento"}, foundCategoryCodes);
	}

	@Test
	void testApplyAddRemove() throws Throwable {
		String currentUser = "mainEditor";
		String[] categoryCodes = new String[] {"cat1", "evento"};
		int size = 8;
		List<String> contentList = this.addContents("ART1", size);
		try {
			String[] contentIds = contentList.toArray(new String[0]);
			String result = this.executeApply(currentUser, ApsAdminSystemConstants.ADD, contentIds, categoryCodes);
			assertEquals(Action.SUCCESS, result);
			ContentCategoryBulkAction action = (ContentCategoryBulkAction) this.getAction();
			this.checkItems(contentIds, action.getSelectedIds());
			this.checkItems(categoryCodes, action.getCategoryCodes());

//			result = this.executeCheckResult(currentUser);
//			assertEquals(Action.SUCCESS, result);
//			result = this.executeViewResult(currentUser);
//			assertEquals(Action.SUCCESS, result);
			this.checkContentCategories(contentIds, categoryCodes, true, false);
			result = this.executeApply(currentUser, ApsAdminSystemConstants.DELETE, contentIds, categoryCodes);
			this.checkContentCategories(contentIds, categoryCodes, false, false);
		} finally {
			this.deleteContents(contentList);
		}
	}

	private void checkContentCategories(String[] contentIds, String[] categoryCodes, boolean expectedWork, boolean expectedOnline) throws EntException {
		for (String contentId : contentIds) {
			Content current = this._contentManager.loadContent(contentId, false);
			Collection<String> contentCategories = this.extractCategoryCodes(current.getCategories());
			for (String categoryCode : categoryCodes) {
				assertEquals(expectedWork, contentCategories.contains(categoryCode));
			}
			if (current.isOnLine()) {
				current = this._contentManager.loadContent(contentId, true);
				contentCategories = this.extractCategoryCodes(current.getCategories());
				for (String categoryCode : categoryCodes) {
					assertEquals(expectedOnline, contentCategories.contains(categoryCode));
				}
			}
		}
	}

	private Collection<String> extractCategoryCodes(Collection<Category> categories) {
		Set<String> categoryCodes = new HashSet<>();
		if (categories != null) {
			for (Category category : categories) {
				categoryCodes.add(category.getCode());
			}
		}
		return categoryCodes;
	}

	private List<String> addContents(String masterContentId, int size) throws EntException {
		List<String> contentIds = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			Content current = this._contentManager.loadContent(masterContentId, false);
			current.setId(null);
			this._contentManager.addContent(current);
			contentIds.add(current.getId());
			if (i % 2 == 0) {
				this._contentManager.insertOnLineContent(current);
			}
		}
		return contentIds;
	}

	private void deleteContents(List<String> contentIds) throws EntException {
		for (String contentId : contentIds) {
			Content current = this._contentManager.loadContent(contentId, false);
			this._contentManager.deleteContent(current);
		}
	}

	private void checkItems(String[] expected, Collection<?> actual) {
		assertEquals(expected.length, actual.size());
		for (Object current : expected) {
			assertTrue(actual.contains(current));
		}
	}

	private String executeEntry(String currentUser, int strutsAction, String[] contentIds) throws Throwable {
		return this.executeCategoryAction(currentUser, "entry", strutsAction, contentIds, null, null);
	}

	private String executeJoinCategory(String currentUser, int strutsAction, String[] contentIds, String[] categoryCodes, String categoryCode) throws Throwable {
		return this.executeCategoryAction(currentUser, "join", strutsAction, contentIds, categoryCodes, categoryCode);
	}

	private String executeDisjoinCategory(String currentUser, int strutsAction, String[] contentIds, String[] categoryCodes, String categoryCode) throws Throwable {
		return this.executeCategoryAction(currentUser, "disjoin", strutsAction, contentIds, categoryCodes, categoryCode);
	}

	private String executeCheckApply(String currentUser, int strutsAction, String[] contentIds, String[] categoryCodes) throws Throwable {
		return this.executeCategoryAction(currentUser, "checkApply", strutsAction, contentIds, categoryCodes, null);
	}

	private String executeApply(String currentUser, int strutsAction, String[] contentIds, String[] categoryCodes) throws Throwable {
		return this.executeCategoryAction(currentUser, "apply", strutsAction, contentIds, categoryCodes, null);
	}

	private String executeCheckResult(String currentUser) throws Throwable {
		return this.executeCommandAction(currentUser, "checkResult");
	}

	private String executeViewResult(String currentUser) throws Throwable {
		return this.executeCommandAction(currentUser, "viewResult");
	}

	private String executeCommandAction(String currentUser, String actionName) throws Throwable {
		this.setUserOnSession(currentUser);
		this.initAction(NAMESPACE, actionName);
		return this.executeAction();
	}

	private String executeCategoryAction(String currentUser, String actionName, int strutsAction, String[] contentIds, String[] categoryCodes, String categoryCode) throws Throwable {
		this.setUserOnSession(currentUser);
		this.initAction(NAMESPACE, actionName);
		this.addParameter("strutsAction", strutsAction);
		this.addParameter("selectedIds", contentIds);
		this.addParameter("categoryCodes", categoryCodes);
		this.addParameter("categoryCode", categoryCode);
		return this.executeAction();
	}

	@BeforeEach
	private void init() {
		this._contentManager = (IContentManager) this.getApplicationContext().getBean(JacmsSystemConstants.CONTENT_MANAGER);
		//this._bulkCommandManager = (IBulkCommandManager) this.getApplicationContext().getBean(SystemConstants.BULK_COMMAND_MANAGER);
	}

	private IContentManager _contentManager;
	//private IBulkCommandManager _bulkCommandManager;

	private static final String NAMESPACE = "/do/jacms/Content/Category";

}