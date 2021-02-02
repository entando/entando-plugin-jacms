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
package com.agiletec.plugins.jacms.aps.system.services.content.widget;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.entando.entando.aps.system.services.widgettype.IWidgetTypeManager;
import org.entando.entando.aps.system.services.widgettype.WidgetType;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.util.ApsProperties;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;

import com.agiletec.plugins.jacms.aps.system.services.Jdk11CompatibleDateFormatter;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.ContentRenderizationInfo;
import org.entando.entando.ent.exception.EntException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author W.Ambu
 */
class ContentViewerHelperIntegrationTest extends BaseTestCase {
    
    private static final String ART1_MODEL_1_IT_RENDER = "<h1 class=\"titolo\">Il titolo</h1>"
            + "<p>Data: " + Jdk11CompatibleDateFormatter.formatLongDate("10-mar-2004") + "</p>"
            + "<img class=\"left\" src=\"/Entando/resources/cms/images/lvback_d2.jpg\" alt=\"Image description\" />"
            + "<h2 class=\"titolo\">Autori:</h2>"
            + "<ul title=\"Authors\">"
            + "	<li>Pippo;</li>"
            + "	<li>Paperino;</li>"
            + "	<li>Pluto;</li>"
            + "</ul>"
            + "<h2 class=\"titolo\">Link:</h2>"
            + "<p><li><a href=\"http://www.spiderman.org\">Spiderman</a></li></p>";

    private static final String ART1_MODEL_3_IT_RENDER = "------ RENDERING CONTENUTO: id = ART1; ---------\n"
            + "ATTRIBUTI:\n"
            + "  - AUTORI (Monolist-Monotext):\n"
            + "         testo=Pippo;\n"
            + "         testo=Paperino;\n"
            + "         testo=Pluto;\n"
            + "  - TITOLO (Text): testo=Il titolo;\n"
            + "  - VEDI ANCHE (Link): testo=Spiderman, dest=http://www.spiderman.org;\n"
            + "  - FOTO (Image): testo=Image description, src(1)=/Entando/resources/cms/images/lvback_d1.jpg;\n"
            + "  - DATA (Date): data_media = " + Jdk11CompatibleDateFormatter.formatMediumDate("10-mar-2004") + ";\n"
            + "------ END ------";

    private static final String ART1_MODEL_11_IT_RENDER = "<h1 class=\"titolo\">Il titolo</h1>"
            + "<a href=\"http://www.entando.com/Entando/it/homepage.page\">Details...</a>"
            + "Benvenuto Name Surname (admin - Name.Surname)";

    @Test
    void testGetRenderedContent_1() throws Throwable {
        try {
            String contentId = "ART1";
            String modelId = "3";
            String renderedContent = _helper.getRenderedContent(contentId, modelId, _requestContext);
            assertEquals(replaceNewLine(ART1_MODEL_3_IT_RENDER.trim()), replaceNewLine(renderedContent.trim()));
        } catch (Throwable t) {
            throw t;
        }
    }
    
    @Test
    void testGetRenderedContent_2() throws Throwable {
        this.testGetRenderedByModel("ART1", null, ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "", ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "   ", ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "default", ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "list", ART1_MODEL_11_IT_RENDER);
        this.testGetRenderedByModel("ART1", "  list   ", ART1_MODEL_11_IT_RENDER);
        this.testGetRenderedByModel("ART1", "3", ART1_MODEL_3_IT_RENDER);
    }
    
    @Test
    void testGetRenderedContent_4() throws Throwable {
        this.executeGetRenderedContent_4(true, 3, "ART1", ART1_MODEL_1_IT_RENDER, true);
        this.executeGetRenderedContent_4(false, 3, "ART1", ART1_MODEL_1_IT_RENDER, false);
        this.executeGetRenderedContent_4(true, 4, "ART1", ART1_MODEL_1_IT_RENDER, false);
        this.executeGetRenderedContent_4(true, 3, null, "", false);
    }
    
    private void executeGetRenderedContent_4(boolean useExtraTitle, int frame, 
            String contentId, String expected, boolean nullExtraParam) throws Throwable {
        this._requestContext.removeExtraParam(SystemConstants.EXTRAPAR_EXTRA_PAGE_TITLES); //clean
        ((MockHttpServletRequest) this._requestContext.getRequest()).removeParameter(SystemConstants.K_CONTENT_ID_PARAM); //clean
        IPage page = this.pageManager.getOnlineRoot();
        page.getMetadata().setUseExtraTitles(useExtraTitle);
        this._requestContext.addExtraParam(SystemConstants.EXTRAPAR_CURRENT_PAGE, page);
        this._requestContext.addExtraParam(SystemConstants.EXTRAPAR_CURRENT_FRAME, frame);
        if (null != contentId) {
            ((MockHttpServletRequest) this._requestContext.getRequest()).setParameter(SystemConstants.K_CONTENT_ID_PARAM, contentId);
        }
        String renderedContent = this._helper.getRenderedContent(null, null, true, _requestContext);
        assertEquals(replaceNewLine(expected.trim()), replaceNewLine(renderedContent.trim()));
        assertEquals(nullExtraParam, null != this._requestContext.getExtraParam(SystemConstants.EXTRAPAR_EXTRA_PAGE_TITLES));
    }
    
    void testGetRenderedByModel(String contentId, String modelId, String expected) throws Throwable {
        this.configureCurrentWidget(contentId, modelId);
        String renderedContent = this._helper.getRenderedContent(null, null, _requestContext);
        assertEquals(replaceNewLine(expected.trim()), replaceNewLine(renderedContent.trim()));
    }

    @Test
    void testGetRenderedContentWithParams() throws Throwable {
        try {
            String contentId = "ART1";
            String modelId = "11";
            String renderedContent = _helper.getRenderedContent(contentId, modelId, _requestContext);
            assertEquals(replaceNewLine(ART1_MODEL_11_IT_RENDER.trim()), replaceNewLine(renderedContent.trim()));
        } catch (Throwable t) {
            throw t;
        }
    }
    
    private String replaceNewLine(String input) {
        input = input.replaceAll("\\n", "");
        input = input.replaceAll("\\r", "");
        return input;
    }
    
    @Test
    void testGetRenderedContentNotApproved() throws Throwable {
        try {
            String contentId = "ART2";
            String modelId = "3";
            String renderedContent = _helper.getRenderedContent(
                    contentId, modelId, _requestContext);
            assertEquals("", renderedContent);
        } catch (Throwable t) {
            throw t;
        }
    }

    @Test
    void testGetRenderedContentNotPresent() throws Throwable {
        try {
            String contentId = "ART3";
            String modelId = "3";
            String renderedContent = _helper.getRenderedContent(
                    contentId, modelId, _requestContext);
            assertEquals("", renderedContent);
        } catch (Throwable t) {
            throw t;
        }
    }
    
    @Test
    void testConvertCspNoncePlaceholder() throws Exception {
        String contentId = "ART120";
        String contentShapeModel = "CspNonce Test <script nonce=\"$content.nonce\">my script</script>";
        int modelId = 1948;
        this.getRequestContext().addExtraParam(SystemConstants.EXTRAPAR_CSP_NONCE_TOKEN, "csp_token_value");
        try {
            this.addNewContentModel(modelId, contentShapeModel, "ART");
            super.setUserOnSession("admin");
            RequestContext reqCtx = this.getRequestContext();
            ContentRenderizationInfo outputInfo = this._helper.getRenderizationInfo(contentId, String.valueOf(modelId), true, reqCtx);
            assertEquals("CspNonce Test <script nonce=\"csp_token_value\">my script</script>", outputInfo.getRenderedContent());
        } catch (Exception t) {
            throw t;
        } finally {
            ContentModel model = this._contentModelManager.getContentModel(modelId);
            if (null != model) {
                this._contentModelManager.removeContentModel(model);
            }
        }
    }
    
    private void addNewContentModel(int id, String shape, String contentTypeCode) throws EntException {
        ContentModel model = new ContentModel();
        model.setContentType(contentTypeCode);
        model.setDescription("test");
        model.setId(id);
        model.setContentShape(shape);
        this._contentModelManager.addContentModel(model);
    }
    
    @BeforeEach
    private void init() throws Exception {
        try {
            this._requestContext = this.getRequestContext();
            Lang lang = new Lang();
            lang.setCode("it");
            lang.setDescr("italiano");
            this._requestContext.addExtraParam(SystemConstants.EXTRAPAR_CURRENT_LANG, lang);
            this.configureCurrentWidget(null, null);
            this._helper = (IContentViewerHelper) this.getApplicationContext().getBean("jacmsContentViewerHelper");
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }
    
    private void configureCurrentWidget(String contentId, String modelId) {
        Widget widget = new Widget();
        IWidgetTypeManager widgetTypeManager
                = (IWidgetTypeManager) this.getService(SystemConstants.WIDGET_TYPE_MANAGER);
        WidgetType widgetType = widgetTypeManager.getWidgetType("content_viewer");
        widget.setType(widgetType);
        ApsProperties properties = new ApsProperties();
        if (null != contentId) {
            properties.setProperty("contentId", contentId);
        }
        if (null != modelId) {
            properties.setProperty("modelId", modelId);
        }
        if (!properties.isEmpty()) {
            widget.setConfig(properties);
        }
        this._requestContext.addExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET, widget);
        this.pageManager = (IPageManager) this.getService(SystemConstants.PAGE_MANAGER);
        this._contentModelManager = (IContentModelManager) this.getService(JacmsSystemConstants.CONTENT_MODEL_MANAGER);
    }

    private RequestContext _requestContext;
    private IPageManager pageManager;
    private IContentViewerHelper _helper;
    private IContentModelManager _contentModelManager = null;

}
