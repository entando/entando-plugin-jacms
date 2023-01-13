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

import com.agiletec.aps.system.services.group.Group;
import com.agiletec.apsadmin.ApsAdminBaseTestCase;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.opensymphony.xwork2.Action;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.report.DefaultBulkCommandReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestContentBulkAction extends ApsAdminBaseTestCase {

    @Test
    void testApplyAddRemove_1() throws Throwable {
        String currentUser = "admin";
        int size = 8;
        List<String> contentList = null;
        try {
            contentList = this.addContents("ART1", size);
            String[] contentIds = contentList.toArray(new String[0]);
            String result = this.executeGroupAction(currentUser, "applyOnline", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
            ContentBulkAction action = (ContentBulkAction) this.getAction();
            this.checkItems(contentIds, action.getSelectedIds());
            for (int i = 0; i < contentList.size(); i++) {
                String newId = contentList.get(i);
                Assertions.assertNotNull(this._contentManager.loadContent(newId, true));
            }
            this.checkReport(size, size, size, 0, null, null);

            result = this.executeGroupAction(currentUser, "applyOffline", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
            for (int i = 0; i < contentList.size(); i++) {
                String newId = contentList.get(i);
                Assertions.assertNotNull(this._contentManager.loadContent(newId, false));
                Assertions.assertNull(this._contentManager.loadContent(newId, true));
            }
            this.checkReport(size, size, size, 0, null, null);

            result = this.executeGroupAction(currentUser, "applyRemove", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
            for (int i = 0; i < contentList.size(); i++) {
                String newId = contentList.get(i);
                Assertions.assertNull(this._contentManager.loadContent(newId, false));
            }
            this.checkReport(size, size, size, 0, null, null);

        } catch (Exception e) {
            this.deleteContents(contentList);
            throw e;
        }
    }

    @Test
    void applyOnlineContentShouldCheck() throws Throwable {
        String currentUser = "admin";
        int size = 8;
        List<String> contentList = null;
        try {
            contentList = this.addContents("ART1", size, Group.ADMINS_GROUP_NAME);
            String[] contentIds = contentList.toArray(new String[0]);
            String result = this.executeGroupAction(currentUser, "applyOnline", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
        } catch (Exception e) {
            this.deleteContents(contentList);
            throw e;
        }
        this.deleteContents(contentList);
    }

    @Test
    void applyOfflineContentShouldCheck() throws Throwable {
        String currentUser = "admin";
        int size = 8;
        List<String> contentList = null;
        try {
            contentList = this.addContents("ART1", size, Group.ADMINS_GROUP_NAME);
            String[] contentIds = contentList.toArray(new String[0]);
            String result = this.executeGroupAction(currentUser, "applyOnline", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);

            result = this.executeGroupAction(currentUser, "applyOffline", new String[]{"ART1"});
            Assertions.assertEquals(Action.SUCCESS, result);


        } catch (Exception e) {
            this.deleteContents(contentList);
            throw e;
        }
        this.deleteContents(contentList);
    }

    @Test
    void applyRemoveOnlineShouldNotRemove() throws Throwable {
        String currentUser = "admin";
        List<String> contentList = new ArrayList<>();
        try {
            List<String> validContents = this.addContents("ART1", 9);
            contentList.addAll(validContents);

            String[] contentIds = contentList.toArray(new String[0]);
            String result = this.executeGroupAction(currentUser, "applyOnline", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);

            result = this.executeGroupAction(currentUser, "applyRemove", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
            for (int i = 0; i < contentList.size(); i++) {
                String newId = contentList.get(i);
                Assertions.assertNotNull(this._contentManager.loadContent(newId, true));
            }

        } catch (Exception e) {
            this.deleteContents(contentList);
            throw e;
        }
        this.deleteContents(contentList);
    }


    @Test
    void testApplyAddFailure() throws Throwable {
        String currentUser = "admin";
        List<String> contentList = new ArrayList<>();
        try {
            List<String> validContents = this.addContents("ART1", 9);
            List<String> invalidContents = this.addEmptyContents("ART", 7);
            contentList.addAll(validContents);
            contentList.addAll(invalidContents);

            String[] contentIds = contentList.toArray(new String[0]);
            String result = this.executeGroupAction(currentUser, "applyOnline", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
            this.checkReport(16, 16, 9, 7, validContents, invalidContents);

            ContentBulkAction action = (ContentBulkAction) this.getAction();
            this.checkItems(contentIds, action.getSelectedIds());
            for (int i = 0; i < contentList.size(); i++) {
                String newId = contentList.get(i);
                if (validContents.contains(newId)) {
                    Assertions.assertNotNull(this._contentManager.loadContent(newId, true));
                } else {
                    Assertions.assertNull(this._contentManager.loadContent(newId, true));
                }
            }

            result = this.executeGroupAction(currentUser, "applyOffline", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
            for (int i = 0; i < contentList.size(); i++) {
                String newId = contentList.get(i);
                Assertions.assertNotNull(this._contentManager.loadContent(newId, false));
                Assertions.assertNull(this._contentManager.loadContent(newId, true));
            }
            this.checkReport(16, 16, 16, 0, validContents, null);

            result = this.executeGroupAction(currentUser, "applyRemove", contentIds);
            Assertions.assertEquals(Action.SUCCESS, result);
            for (int i = 0; i < contentList.size(); i++) {
                String newId = contentList.get(i);
                Assertions.assertNull(this._contentManager.loadContent(newId, false));
            }
            this.checkReport(16, 16, 16, 0, null, null);

        } catch (Exception e) {
            this.deleteContents(contentList);
            throw e;
        }
    }

    private void checkReport(int total, int applyTotal, int applySuccessed, int applyError, List<String> successId, List<String> errorIds) {
        DefaultBulkCommandReport<String> report = ((ContentBulkAction) this.getAction()).getReport();
        Assertions.assertEquals(total, report.getTotal());
        Assertions.assertEquals(applyTotal, report.getApplyTotal());
        Assertions.assertEquals(applySuccessed, report.getApplySuccesses());
        Assertions.assertEquals(applyError, report.getApplyErrors());
        if (null != successId) {
            for (int i = 0; i < successId.size(); i++) {
                String id = successId.get(i);
                Assertions.assertTrue(report.getSuccesses().contains(id));
            }
        }
        if (null != errorIds) {
            for (int i = 0; i < errorIds.size(); i++) {
                String id = errorIds.get(i);
                Assertions.assertTrue(report.getErrors().containsKey(id));
            }
        }
    }

    private List<String> addContents(String masterContentId, int size) throws EntException {
        return addContents(masterContentId, size, null);
    }

    private List<String> addContents(String masterContentId, int size, String group) throws EntException {
        List<String> contentIds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Content current = this._contentManager.loadContent(masterContentId, false);
            current.setId(null);
            if(StringUtils.isNotBlank(group)) {
                current.setMainGroup(group);
            }
            this._contentManager.addContent(current);
            contentIds.add(current.getId());
        }
        return contentIds;
    }

    private List<String> addEmptyContents(String typeCode, int size) throws EntException {
        List<String> contentIds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Content current = this._contentManager.createContentType(typeCode);
            current.setMainGroup(Group.FREE_GROUP_NAME);
            current.setDescription("Descr " + i);
            current.setId(null);
            this._contentManager.addContent(current);
            contentIds.add(current.getId());
        }
        return contentIds;
    }

    private void deleteContents(List<String> contentIds) throws EntException {
        for (String contentId : contentIds) {
            Content current = this._contentManager.loadContent(contentId, false);
            if (null != current) {
                this._contentManager.removeOnLineContent(current);
                this._contentManager.deleteContent(current);
            }
        }
    }

    private void checkItems(String[] expected, Collection<?> actual) {
        Assertions.assertEquals(expected.length, actual.size());
        for (Object current : expected) {
            Assertions.assertTrue(actual.contains(current));
        }
    }

    private String executeCommandAction(String currentUser, String actionName) throws Throwable {
        this.setUserOnSession(currentUser);
        this.initAction(NAMESPACE, actionName);
        return this.executeAction();
    }

    private String executeGroupAction(String currentUser, String actionName, String[] contentIds) throws Throwable {
        this.setUserOnSession(currentUser);
        this.initAction(NAMESPACE, actionName);
        this.addParameter("selectedIds", contentIds);
        return this.executeAction();
    }

    @BeforeEach
    private void init() {
        this._contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
    }

    private IContentManager _contentManager;

    private static final String NAMESPACE = "/do/jacms/Content/Bulk";

}