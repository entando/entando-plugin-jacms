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
package com.agiletec.plugins.jacms.apsadmin.content.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.entando.entando.ent.exception.EntException;
import com.agiletec.apsadmin.ApsAdminBaseTestCase;
import com.agiletec.apsadmin.system.ApsAdminSystemConstants;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.apsadmin.content.AbstractContentAction;
import com.agiletec.plugins.jacms.apsadmin.content.ContentActionConstants;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author E.Santoboni
 */
public abstract class AbstractBaseTestContentAction extends ApsAdminBaseTestCase {

    protected String executeEdit(String contentId, String currentUserName) throws Throwable {
        this.initAction("/do/jacms/Content", "edit");
        this.setUserOnSession(currentUserName);
        this.addParameter("contentId", contentId);
        return this.executeAction();
    }

    protected String executeCreateNewVoid(String contentTypeCode, String contentDescription,
            String contentStatus, String contentMainGroup, String currentUserName) throws Throwable {
        this.initAction("/do/jacms/Content", "createNewVoid");
        this.setUserOnSession(currentUserName);
        this.addParameter("contentTypeCode", contentTypeCode);
        this.addParameter("contentDescription", contentDescription);
        this.addParameter("contentStatus", contentStatus);
        this.addParameter("contentMainGroup", contentMainGroup);
        String result = this.executeAction();

        String contentSessionMarker = this.extractSessionMarker(contentTypeCode, ApsAdminSystemConstants.ADD);
        assertNotNull(this.getContentOnEdit(contentSessionMarker));
        return result;
    }

    protected String extractSessionMarker(String param, int operation) throws EntException {
        Content content = null;
        if (operation == ApsAdminSystemConstants.ADD) {
            content = this._contentManager.createContentType(param);
        } else {
            content = this._contentManager.loadContent(param, false);
        }
        return AbstractContentAction.buildContentOnSessionMarker(content, operation);
    }

    protected void initContentAction(String namespace, String name, String contentOnSessionMarker) throws Exception {
        this.initAction(namespace, name);
        this.addParameter("contentOnSessionMarker", contentOnSessionMarker);
    }

    protected Content getContentOnEdit(String contentMarker) {
        return (Content) this.getRequest().getSession()
                .getAttribute(ContentActionConstants.SESSION_PARAM_NAME_CURRENT_CONTENT_PREXIX + contentMarker);
    }

    /**
     * Aggiunge una serie di contenuti ad uso dei test clonandoli da quelli
     * specificati.
     *
     * @param masterContentIds Gli identificativi dei contenuti da cui ricavare
     * i nuovi.
     * @param publish indica se publicare i contenuti.
     * @return Gli identificativi dei nuovi contenuti.
     * @throws Exception In caso di errore.
     */
    protected String[] addDraftContentsForTest(String[] masterContentIds, boolean publish) throws Exception {
        String[] newContentIds = new String[masterContentIds.length];
        for (int i = 0; i < masterContentIds.length; i++) {
            Content content = this.getContentManager().loadContent(masterContentIds[i], false);
            content.setId(null);
            this.getContentManager().saveContent(content);
            newContentIds[i] = content.getId();
            if (publish) {
                this.getContentManager().insertOnLineContent(content);
            }
        }
        for (int i = 0; i < newContentIds.length; i++) {
            Content content = this.getContentManager().loadContent(newContentIds[i], false);
            assertNotNull(content);
        }
        return newContentIds;
    }

    @BeforeEach
    protected void init() throws Exception {
        try {
            _contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    protected IContentManager getContentManager() {
        return this._contentManager;
    }

    private IContentManager _contentManager = null;

}
