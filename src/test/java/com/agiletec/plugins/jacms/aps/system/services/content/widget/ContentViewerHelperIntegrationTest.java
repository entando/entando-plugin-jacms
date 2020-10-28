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

import org.entando.entando.aps.system.services.widgettype.IWidgetTypeManager;
import org.entando.entando.aps.system.services.widgettype.WidgetType;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.util.ApsProperties;

import com.agiletec.plugins.jacms.aps.system.services.Jdk11CompatibleDateFormatter;

/**
 * @author W.Ambu
 */
public class ContentViewerHelperIntegrationTest extends BaseTestCase {
    
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.init();
    }

    public void testGetRenderedContent_1() throws Throwable {
        try {
            String contentId = "ART1";
            String modelId = "3";
            String renderedContent = _helper.getRenderedContent(contentId, modelId, _requestContext);
            assertEquals(replaceNewLine(ART1_MODEL_3_IT_RENDER.trim()), replaceNewLine(renderedContent.trim()));
        } catch (Throwable t) {
            throw t;
        }
    }
    
    public void testGetRenderedContent_2() throws Throwable {
        this.testGetRenderedByModel("ART1", null, ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "", ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "   ", ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "default", ART1_MODEL_1_IT_RENDER);
        this.testGetRenderedByModel("ART1", "list", ART1_MODEL_11_IT_RENDER);
        this.testGetRenderedByModel("ART1", "  list   ", ART1_MODEL_11_IT_RENDER);
        this.testGetRenderedByModel("ART1", "3", ART1_MODEL_3_IT_RENDER);
    }
    
    public void testGetRenderedByModel(String contentId, String modelId, String expected) throws Throwable {
        this.configureCurrentWidget(contentId, modelId);
        String renderedContent = this._helper.getRenderedContent(null, null, _requestContext);
        assertEquals(replaceNewLine(expected.trim()), replaceNewLine(renderedContent.trim()));
    }

    public void testGetRenderedContentWithParams() throws Throwable {
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

    public void testGetRenderedContentNotApproved() throws Throwable {
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

    public void testGetRenderedContentNotPresent() throws Throwable {
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
        IWidgetTypeManager showletTypeMan
                = (IWidgetTypeManager) this.getService(SystemConstants.WIDGET_TYPE_MANAGER);
        WidgetType showletType = showletTypeMan.getWidgetType("content_viewer");
        widget.setType(showletType);
        ApsProperties properties = new ApsProperties();
        if (null != contentId) {
            properties.setProperty("contentId", contentId);
        }
        if (null != modelId) {
            properties.setProperty("modelId", modelId);
        }
        widget.setConfig(properties);
        this._requestContext.addExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET, widget);
    }

    private RequestContext _requestContext;
    private IContentViewerHelper _helper;

}
