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
package com.agiletec.plugins.jacms.aps.system.services.dispenser;

import org.entando.entando.aps.system.services.cache.CacheInfoManager;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import org.entando.entando.aps.system.services.cache.ICacheInfoManager;

import static com.agiletec.plugins.jacms.aps.system.services.Jdk11CompatibleDateFormatter.formatMediumDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.agiletec.aps.system.services.user.IUserManager;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author W.Ambu - E.Santoboni
 */
class TestContentDispenser extends BaseTestCase {

    @Test
    void testGetRenderedContent_1() throws Throwable {
        RequestContext reqCtx = this.getRequestContext();

        ContentRenderizationInfo outputInfo = this._contentDispenser.getRenderizationInfo("ART1", 2, "en", reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART1_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
        this._contentDispenser.resolveLinks(outputInfo, reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART1.trim()), this.replaceNewLine(outputInfo.getRenderedContent().trim()));
        
        ContentRenderizationInfo outputInfoForUser = this._contentDispenser.getRenderizationInfo("ART1", 2, "en", this.getUserOnSession(), false);
        assertEquals(outputInfo.getCachedRenderedContent().trim(), outputInfoForUser.getCachedRenderedContent().trim());

        this.setUserOnSession("admin");
        outputInfo = this._contentDispenser.getRenderizationInfo("ART1", 2, "en", reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART1_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
        this._contentDispenser.resolveLinks(outputInfo, reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART1.trim()), this.replaceNewLine(outputInfo.getRenderedContent().trim()));
        
        outputInfoForUser = this._contentDispenser.getRenderizationInfo("ART1", 2, "en", this.getUserOnSession(), false);
        assertEquals(outputInfo.getCachedRenderedContent().trim(), outputInfoForUser.getCachedRenderedContent().trim());
        
        outputInfo = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", reqCtx);
        assertEquals(this.replaceNewLine(_attendedItART104_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
        
        outputInfoForUser = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", this.getUserOnSession(), false);
        assertEquals(outputInfo.getCachedRenderedContent().trim(), outputInfoForUser.getCachedRenderedContent().trim());
        
        this.setUserOnSession("editorCoach");
        outputInfo = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", reqCtx);
        assertEquals(this.replaceNewLine(_attendedItART104_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
        
        outputInfoForUser = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", this.getUserOnSession(), false);
        assertEquals(outputInfo.getCachedRenderedContent().trim(), outputInfoForUser.getCachedRenderedContent().trim());

        this.setUserOnSession("pageManagerCoach");
        outputInfo = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", reqCtx);
        assertEquals(this.replaceNewLine(_attendedItART104_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
        
        outputInfoForUser = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", this.getUserOnSession(), false);
        assertEquals(outputInfo.getCachedRenderedContent().trim(), outputInfoForUser.getCachedRenderedContent().trim());
    }
    
    private UserDetails getUserOnSession() {
        return (UserDetails) super.getRequestContext().getRequest().getSession().getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER);
    }

    @Test
    void testGetRenderedContent_2() throws Throwable {
        RequestContext reqCtx = this.getRequestContext();
        this.setUserOnSession("admin");

        ContentRenderizationInfo outputInfo = this._contentDispenser.getRenderizationInfo("ART120", 2, "it", reqCtx);
        assertEquals(this.replaceNewLine(_attendedItART120_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
        this._contentDispenser.resolveLinks(outputInfo, reqCtx);
        assertEquals(this.replaceNewLine(_attendedItART120.trim()), this.replaceNewLine(outputInfo.getRenderedContent().trim()));

        outputInfo = this._contentDispenser.getRenderizationInfo("ART120", 2, "en", reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART120_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
        this._contentDispenser.resolveLinks(outputInfo, reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART120.trim()), this.replaceNewLine(outputInfo.getRenderedContent().trim()));

        outputInfo = this._contentDispenser.getRenderizationInfo("ART121", 2, "it", reqCtx);
        assertEquals(this.replaceNewLine(_attendedItART121_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));

        outputInfo = this._contentDispenser.getRenderizationInfo("ART121", 2, "en", reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART121_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));

        outputInfo = this._contentDispenser.getRenderizationInfo("ART122", 2, "en", reqCtx);
        assertEquals(this.replaceNewLine(_attendedEnART122_cached.trim()), this.replaceNewLine(outputInfo.getCachedRenderedContent().trim()));
    }
    
    @Test
    void testGetRenderedContent_3_1() throws Throwable {
        this.executeTestGetRenderedContent_3(Boolean.FALSE, false);
        this.executeTestGetRenderedContent_3(Boolean.FALSE, true);
    }
    
    @Test
    void testGetRenderedContent_3_2() throws Throwable {
        this.executeTestGetRenderedContent_3(Boolean.TRUE, false);
        this.executeTestGetRenderedContent_3(Boolean.TRUE, true);
    }
    
    @Test
    void testGetRenderedContent_3_3() throws Throwable {
        this.executeTestGetRenderedContent_3(null, false);
    }
    
    protected void executeTestGetRenderedContent_3(Boolean cached, boolean useCurrentUser) throws Throwable {
        Content content = this._contentManager.loadContent("ART120", true);
        content.setId(null);
        try {
            RequestContext reqCtx = this.getRequestContext();
            this.setUserOnSession("admin");
            UserDetails currentUser = (UserDetails) reqCtx.getRequest().getSession().getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER);
            assertEquals("admin", currentUser.getUsername());
            this._contentManager.insertOnLineContent(content);
            String cacheKey = (useCurrentUser) ? 
                    BaseContentDispenser.getRenderizationInfoCacheKey(content.getId(), 2, "it", reqCtx) :
                    BaseContentDispenser.getRenderizationInfoCacheKey(content.getId(), 2, "it", currentUser);
            assertNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey));
            ContentRenderizationInfo outputInfo = null; 
            if (null == cached) {
                outputInfo = this._contentDispenser.getRenderizationInfo(content.getId(), 2, "it", reqCtx);
            } else {
                if (useCurrentUser) {
                    outputInfo = this._contentDispenser.getRenderizationInfo(content.getId(), 2, "it", currentUser, cached);
                } else {
                    outputInfo = this._contentDispenser.getRenderizationInfo(content.getId(), 2, "it", reqCtx, cached);
                }
            }
            assertNotNull(outputInfo);
            this.waitNotifyingThread();
            Object renderedInfoInCache = this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey);
            Object contentAuthInfoInCache = this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, JacmsSystemConstants.CONTENT_AUTH_INFO_CACHE_PREFIX + content.getId());
            if (cached != null && !cached) {
                assertNull(renderedInfoInCache);
                assertNull(contentAuthInfoInCache);
            } else {
                assertNotNull(renderedInfoInCache);
                assertNotNull(contentAuthInfoInCache);
            }
            this._contentManager.insertOnLineContent(content);
            this.waitNotifyingThread();
            assertNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey));
            assertNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, JacmsSystemConstants.CONTENT_AUTH_INFO_CACHE_PREFIX + content.getId()));
        } catch (Throwable t) {
            throw t;
        } finally {
            if (null != content.getId()) {
                this._contentManager.deleteContent(content);
            }
        }
    }
    
    @Test
    void testGetRenderedContent_4() throws Throwable {
        String contentId = "ART120";
        String contentShapeModel = "title (Text): testo=$content.Titolo.getText()";
        int modelId = 1972;
        try {
            this.addNewContentModel(modelId, contentShapeModel, "ART");
            RequestContext reqCtx = this.getRequestContext();
            this.setUserOnSession("admin");
            ContentRenderizationInfo outputInfo = this._contentDispenser.getRenderizationInfo(contentId, modelId, "en", reqCtx);
            assertEquals("title (Text): testo=Title of Administrator's Content", outputInfo.getCachedRenderedContent());

            ContentModel model = this._contentModelManager.getContentModel(modelId);
            String newContentShapeModel = "title: testo=$content.Titolo.getText()";
            model.setContentShape(newContentShapeModel);
            this._contentModelManager.updateContentModel(model);
            this.waitNotifyingThread();

            outputInfo = this._contentDispenser.getRenderizationInfo(contentId, modelId, "en", reqCtx);
            assertEquals("title: testo=Title of Administrator's Content", outputInfo.getCachedRenderedContent());
        } catch (Throwable t) {
            throw t;
        } finally {
            ContentModel model = this._contentModelManager.getContentModel(modelId);
            if (null != model) {
                this._contentModelManager.removeContentModel(model);
            }
        }
    }
    
    @Test
    void testGetRenderedContent_5() throws Throwable {
        Content content = this._contentManager.loadContent("ART1", true);
        content.setId(null);
        try {
            RequestContext reqCtx = this.getRequestContext();
            this.setUserOnSession("admin");
            IUserManager userManager = super.getApplicationContext().getBean(IUserManager.class);
            UserDetails currentUser = (UserDetails) reqCtx.getRequest().getSession().getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER);
            UserDetails guestUser = userManager.getGuestUser();
            assertEquals("admin", currentUser.getUsername());
            this._contentManager.insertOnLineContent(content);
            String cacheKey = BaseContentDispenser.getRenderizationInfoCacheKey(content.getId(), 2, "it", currentUser);
            String cacheKeyGuest = BaseContentDispenser.getRenderizationInfoCacheKey(content.getId(), 2, "it", guestUser);
            assertNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey));
            assertNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKeyGuest));
            Assertions.assertNotEquals(cacheKey, cacheKeyGuest);
            ContentRenderizationInfo outputInfo = this._contentDispenser.getRenderizationInfo(content.getId(), 2, "it", currentUser, true);
            ContentRenderizationInfo outputInfoGuest = this._contentDispenser.getRenderizationInfo(content.getId(), 2, "it", guestUser, true);
            assertNotNull(outputInfo);
            Assertions.assertEquals(outputInfo.getCachedRenderedContent(), outputInfoGuest.getCachedRenderedContent());
            this.waitNotifyingThread();
            assertNotNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey));
            assertNotNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKeyGuest));
            
            this._contentManager.insertOnLineContent(content);
            this.waitNotifyingThread();
            assertNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey));
            assertNull(this._cacheInfoManager.getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKeyGuest));
        } catch (Throwable t) {
            throw t;
        } finally {
            if (null != content.getId()) {
                this._contentManager.deleteContent(content);
            }
        }
    }
    
    public void addNewContentModel(int id, String shape, String contentTypeCode) throws Throwable {
        ContentModel model = new ContentModel();
        model.setContentType(contentTypeCode);
        model.setDescription("test");
        model.setId(id);
        model.setContentShape(shape);
        this._contentModelManager.addContentModel(model);
    }

    @Test
    void testGetUnauthorizedContent() throws Throwable {
        RequestContext reqCtx = this.getRequestContext();
        this.setUserOnSession(SystemConstants.GUEST_USER_NAME);
        ContentRenderizationInfo outputInfo = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", reqCtx);
        assertEquals("Current user 'guest' can't view this content", outputInfo.getCachedRenderedContent().trim());

        this.setUserOnSession("editorCustomers");
        outputInfo = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", reqCtx);
        assertEquals("Current user 'editorCustomers' can't view this content", outputInfo.getCachedRenderedContent().trim());

        this.setUserOnSession("supervisorCustomers");
        outputInfo = this._contentDispenser.getRenderizationInfo("ART104", 2, "it", reqCtx);
        assertEquals("Current user 'supervisorCustomers' can't view this content", outputInfo.getCachedRenderedContent().trim());
    }

    @Test
    void testGetRenderedContentWithWrongModel() throws Throwable {
        RequestContext reqCtx = this.getRequestContext();
        String output = _contentDispenser.getRenderedContent("ART1", 67, "en", reqCtx);
        assertEquals("Content model 67 undefined", output.trim());
    }
    
    @Test
    void testCspNoncePlaceholder() throws Throwable {
        String contentId = "ART120";
        String contentShapeModel = "CspNonce Test <script nonce=\"$content.nonce\">my script</script>";
        int modelId = 1948;
        try {
            this.addNewContentModel(modelId, contentShapeModel, "ART");
            RequestContext reqCtx = this.getRequestContext();
            this.setUserOnSession("admin");
            ContentRenderizationInfo outputInfo = this._contentDispenser.getRenderizationInfo(contentId, modelId, "en", reqCtx);
            assertEquals("CspNonce Test <script nonce=\"" + JacmsSystemConstants.CSP_NONCE_PLACEHOLDER + "\">my script</script>", outputInfo.getCachedRenderedContent());
        } catch (Throwable t) {
            throw t;
        } finally {
            ContentModel model = this._contentModelManager.getContentModel(modelId);
            if (null != model) {
                this._contentModelManager.removeContentModel(model);
            }
        }
    }
    
    private String replaceNewLine(String input) {
        input = input.replaceAll("\\n", "");
        input = input.replaceAll("\\r", "");
        return input;
    }
    
    @Test
    void testRenderCategories() throws Throwable {
        String contentId = "ART120";
        String contentShapeModel = "#foreach($contentCategory in $content.getCategories())<p>$contentCategory.title</p>#end";
        int modelId = 1955;
        try {
            this.addNewContentModel(modelId, contentShapeModel, "ART");
            RequestContext reqCtx = this.getRequestContext();
            this.setUserOnSession("admin");
            ContentRenderizationInfo outputInfo = this._contentDispenser.getRenderizationInfo(contentId, modelId, "en", reqCtx);
            assertEquals("<p>Category 2</p><p>Category 3</p>", outputInfo.getCachedRenderedContent());
        } catch (Throwable t) {
            throw t;
        } finally {
            ContentModel model = this._contentModelManager.getContentModel(modelId);
            if (null != model) {
                this._contentModelManager.removeContentModel(model);
            }
        }
    }
    
    @BeforeEach
    private void init() throws Exception {
        try {
            this._contentDispenser = (IContentDispenser) this.getService(JacmsSystemConstants.CONTENT_DISPENSER_MANAGER);
            this._contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
            this._contentModelManager = (IContentModelManager) this.getService(JacmsSystemConstants.CONTENT_MODEL_MANAGER);
            this._cacheInfoManager = (CacheInfoManager) this.getService(SystemConstants.CACHE_INFO_MANAGER);
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    private IContentDispenser _contentDispenser = null;
    private IContentManager _contentManager = null;
    private IContentModelManager _contentModelManager = null;
    private CacheInfoManager _cacheInfoManager;

    private String _attendedEnART1_cached
            = "ART1;\n"
            + "Pippo;\n"
            + "Paperino;\n"
            + "Pluto;\n"
            + "The title;\n"
            + "Spiderman,#!U;http://www.spiderman.org!#;\n"
            + "Image description,/Entando/resources/cms/images/lvback_d1.jpg;\n"
            + "Mar 10, 2004;";

    private String _attendedEnART1
            = "ART1;\n"
            + "Pippo;\n"
            + "Paperino;\n"
            + "Pluto;\n"
            + "The title;\n"
            + "Spiderman,http://www.spiderman.org;\n"
            + "Image description,/Entando/resources/cms/images/lvback_d1.jpg;\n"
            + "Mar 10, 2004;";

    private String _attendedItART104_cached
            = "ART104;\n"
            + "Walter;\n"
            + "Marco;\n"
            + "Eugenio;\n"
            + "William;\n"
            + "Titolo Contenuto 2 Coach;\n"
            + "Home Entando,#!U;http://www.entando.com!#;\n"
            + ",;\n"
            + formatMediumDate("4-gen-2007") + ";";

    private String _attendedItART104
            = "ART104;\n"
            + "Walter;\n"
            + "Marco;\n"
            + "Eugenio;\n"
            + "William;\n"
            + "Titolo Contenuto 2 Coach;\n"
            + "Home Entando,http://www.entando.com;\n"
            + ",;\n"
            + formatMediumDate("4-gen-2007") + ";";

    private String _attendedItART120_cached
            = "ART120;\n"
            + "Titolo Contenuto degli &quot;Amministratori&quot;;\n"
            + "Pagina Iniziale Entando Portal,#!U;http://www.entando.com!#;\n,;\n"
            + formatMediumDate("28-mar-2009") + ";";

    private String _attendedItART120
            = "ART120;\n"
            + "Titolo Contenuto degli &quot;Amministratori&quot;;\n"
            + "Pagina Iniziale Entando Portal,http://www.entando.com;\n,;\n"
            + formatMediumDate("28-mar-2009") + ";";

    private String _attendedEnART120_cached
            = "ART120;\n"
            + "Title of Administrator's Content;\n"
            + "jAPSPortal HomePage,#!U;http://www.entando.com!#;\n,;\n"
            + "Mar 28, 2009;";

    private String _attendedEnART120
            = "ART120;\n"
            + "Title of Administrator's Content;\n"
            + "jAPSPortal HomePage,http://www.entando.com;\n,;\n"
            + "Mar 28, 2009;";

    private String _attendedItART121_cached
            = "ART121;\n"
            + "Titolo Contenuto degli &quot;Amministratori&quot; 2;\n"
            + "Pagina Iniziale W3C,#!U;http://www.w3.org/!#;\n,;\n"
            + formatMediumDate("30-mar-2009") + ";";

    private String _attendedEnART121_cached
            = "ART121;\n"
            + "Title of Administrator's Content &lt;2&gt;;\n"
            + "World Wide Web Consortium - Web Standards,#!U;http://www.w3.org/!#;\n,;\n"
            + "Mar 30, 2009;";

    private String _attendedEnART122_cached
            = "ART122;\n"
            + "Titolo Contenuto degli &quot;Amministratori&quot; 3;\n,;\n,;\n;";

}
