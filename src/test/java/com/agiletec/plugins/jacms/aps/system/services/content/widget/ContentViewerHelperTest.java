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

import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.tags.util.HeadInfoContainer;
import com.agiletec.aps.util.ApsProperties;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.IContentAuthorizationHelper;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.PublicContentAuthorizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.ContentRenderizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.IContentDispenser;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentViewerHelperTest {

    @Mock
    private IContentModelManager contentModelManager;

    @Mock
    private IContentManager contentManager;

    @Mock
    private IContentDispenser contentDispenser;

    @Mock
    private IContentAuthorizationHelper contentAuthorizationHelper;

    @Mock
    private RequestContext reqCtx;

    @InjectMocks
    private ContentViewerHelper contentViewerHelper;

    @BeforeEach
    public void setUp() throws Exception {
        Lang currentLang = new Lang();
        currentLang.setCode("en");
        currentLang.setDescr("English");
        IPage currentPage = Mockito.mock(IPage.class);
        Mockito.lenient().when(currentPage.isUseExtraTitles()).thenReturn(true);
        Mockito.lenient().when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_PAGE)).thenReturn(currentPage);
        Mockito.lenient().when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_LANG)).thenReturn(currentLang);

        ContentRenderizationInfo renderizationInfo = Mockito.mock(ContentRenderizationInfo.class);
        Mockito.lenient().when(this.contentDispenser.getRenderizationInfo(Mockito.anyString(),
                Mockito.anyLong(), Mockito.anyString(), Mockito.any(RequestContext.class), Mockito.anyBoolean())).thenReturn(renderizationInfo);
        Mockito.lenient().when(renderizationInfo.getCachedRenderedContent()).thenReturn("Cached Rendered Content");
        Mockito.lenient().when(renderizationInfo.getRenderedContent()).thenReturn("Final Rendered Content");
    }
    
    @Test
    void testGetRenderedContent() throws Exception {
        HeadInfoContainer hic = Mockito.mock(HeadInfoContainer.class);
        Mockito.lenient().when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_HEAD_INFO_CONTAINER)).thenReturn(hic);
        ContentModel model = Mockito.mock(ContentModel.class);
        when(this.contentModelManager.getContentModel(Mockito.anyLong())).thenReturn(model);
        when(model.getStylesheet()).thenReturn("..css style..");
        Mockito.lenient().when(model.getContentShape()).thenReturn("Body of content model");
        String renderedContent = this.contentViewerHelper.getRenderedContent("ART123", "11", reqCtx);
        Assertions.assertNotNull(renderedContent);
        Assertions.assertEquals("Final Rendered Content", renderedContent);
        Mockito.verify(hic, Mockito.times(1)).addInfo("CSS", "..css style..");
        Mockito.verify(contentDispenser, Mockito.times(1)).getRenderizationInfo("ART123", 11, "en", reqCtx, true);
        Mockito.verify(contentDispenser, Mockito.times(1)).resolveLinks(Mockito.any(ContentRenderizationInfo.class), Mockito.any(RequestContext.class));
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_LANG);
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET);
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_HEAD_INFO_CONTAINER);
        Mockito.verifyZeroInteractions(contentManager);
    }

    @Test
    void testGetRenderedContentWithListModel() throws Exception {
        HeadInfoContainer hic = Mockito.mock(HeadInfoContainer.class);
        Mockito.lenient().when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_HEAD_INFO_CONTAINER)).thenReturn(hic);
        ContentModel model = Mockito.mock(ContentModel.class);
        when(this.contentModelManager.getContentModel(Mockito.anyLong())).thenReturn(model);
        when(this.contentManager.getListModel(Mockito.anyString())).thenReturn("34");
        when(model.getStylesheet()).thenReturn("..other css style..");
        Mockito.lenient().when(model.getContentShape()).thenReturn("Body of content model");
        String renderedContent = this.contentViewerHelper.getRenderedContent("ART123", "list", reqCtx);
        Assertions.assertNotNull(renderedContent);
        Assertions.assertEquals("Final Rendered Content", renderedContent);
        Mockito.verify(hic, Mockito.times(1)).addInfo("CSS", "..other css style..");
        Mockito.verify(contentDispenser, Mockito.times(1)).getRenderizationInfo("ART123", 34, "en", reqCtx, true);
        Mockito.verify(contentDispenser, Mockito.times(1)).resolveLinks(Mockito.any(ContentRenderizationInfo.class), Mockito.any(RequestContext.class));
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_LANG);
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET);
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_HEAD_INFO_CONTAINER);
        Mockito.verify(contentManager, Mockito.times(1)).getListModel("ART123");
    }
    
    @Test
    void testGetRenderedContentWithError() throws Exception {
        HeadInfoContainer hic = Mockito.mock(HeadInfoContainer.class);
        Mockito.lenient().when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_HEAD_INFO_CONTAINER)).thenReturn(hic);
        Mockito.doThrow(RuntimeException.class).when(this.contentDispenser).resolveLinks(Mockito.any(ContentRenderizationInfo.class), Mockito.any(RequestContext.class));
        Assertions.assertThrows(EntException.class, () -> {
            this.contentViewerHelper.getRenderedContent("NEW123", "10", reqCtx);
        });
        Mockito.verify(hic, Mockito.times(0)).addInfo(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_LANG);
        Mockito.verify(contentDispenser, Mockito.times(1)).getRenderizationInfo("NEW123", 10, "en", reqCtx, true);
        Mockito.verify(contentDispenser, Mockito.times(1)).resolveLinks(Mockito.any(ContentRenderizationInfo.class), Mockito.any(RequestContext.class));
        Mockito.verify(reqCtx, Mockito.times(0)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_FRAME);
        Mockito.verifyZeroInteractions(contentManager);
    }
    
    @Test
    void getRenderizationInfo() throws Exception {
        ContentModel model = Mockito.mock(ContentModel.class);
        when(this.contentModelManager.getContentModel(Mockito.anyLong())).thenReturn(model);
        when(this.contentManager.getDefaultModel(Mockito.anyString())).thenReturn("68");
        when(model.getStylesheet()).thenReturn(null);
        Mockito.lenient().when(model.getContentShape()).thenReturn("Body of content model");
        when(this.contentDispenser.getRenderizationInfo(Mockito.anyString(),
                Mockito.anyLong(), Mockito.anyString(), Mockito.any(RequestContext.class), Mockito.anyBoolean())).thenReturn(null);
        String renderedContent = this.contentViewerHelper.getRenderedContent("ART124", "default", reqCtx);
        Assertions.assertNotNull(renderedContent);
        Assertions.assertTrue(StringUtils.isBlank(renderedContent));
        Mockito.verify(contentDispenser, Mockito.times(1)).getRenderizationInfo("ART124", 68, "en", reqCtx, true);
        Mockito.verify(contentDispenser, Mockito.times(0)).resolveLinks(Mockito.any(ContentRenderizationInfo.class), Mockito.any(RequestContext.class));
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_LANG);
        Mockito.verify(reqCtx, Mockito.times(1)).getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET);
        Mockito.verify(reqCtx, Mockito.times(0)).getExtraParam(SystemConstants.EXTRAPAR_HEAD_INFO_CONTAINER);
        Mockito.verify(contentManager, Mockito.times(1)).getDefaultModel("ART124");
    }

    @Test
    void getAuthorizationInfo() throws Exception {
        PublicContentAuthorizationInfo pcaiMock = Mockito.mock(PublicContentAuthorizationInfo.class);
        when(this.contentAuthorizationHelper.getAuthorizationInfo("EVN100", true)).thenReturn(pcaiMock);
        Widget currentWidget = new Widget();
        ApsProperties properties = new ApsProperties();
        properties.setProperty("contentId", "EVN100");
        currentWidget.setConfig(properties);
        when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET)).thenReturn(currentWidget);
        PublicContentAuthorizationInfo pcai = contentViewerHelper.getAuthorizationInfo(null, reqCtx);
        Assertions.assertNotNull(pcai);
        Mockito.verify(reqCtx, Mockito.times(0)).getRequest();
    }

    @Test
    void getNullAuthorizationInfo() throws Exception {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getParameter(SystemConstants.K_CONTENT_ID_PARAM)).thenReturn(null);
        when(this.reqCtx.getRequest()).thenReturn(mockRequest);
        Widget currentWidget = new Widget();
        ApsProperties properties = new ApsProperties();
        properties.setProperty("wrongParam", "xyz");
        currentWidget.setConfig(properties);
        when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET)).thenReturn(currentWidget);
        PublicContentAuthorizationInfo pcai = contentViewerHelper.getAuthorizationInfo(null, reqCtx);
        Assertions.assertNull(pcai);
        Mockito.verify(reqCtx, Mockito.times(1)).getRequest();
        Mockito.verify(contentAuthorizationHelper, Mockito.times(0)).getAuthorizationInfo(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    void getAuthorizationInfoWithError() throws Exception {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getParameter(SystemConstants.K_CONTENT_ID_PARAM)).thenReturn("ART123");
        when(this.reqCtx.getRequest()).thenReturn(mockRequest);
        Widget currentWidget = new Widget();
        currentWidget.setConfig(new ApsProperties());
        when(this.reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET)).thenReturn(currentWidget);
        Mockito.doThrow(RuntimeException.class).when(this.contentAuthorizationHelper).getAuthorizationInfo("ART123", true);
        Assertions.assertThrows(EntException.class, () -> {
            this.contentViewerHelper.getAuthorizationInfo(null, reqCtx);
        });
        Mockito.verify(reqCtx, Mockito.times(1)).getRequest();
    }

    @Test
    void getRenderizationInfo_ModelIdParamNotNumeric_ShouldReturnNull() throws Exception {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(this.reqCtx.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getParameter("modelId")).thenReturn("foo");

        ContentRenderizationInfo renderizationInfo = contentViewerHelper.getRenderizationInfo("ART123", null, false, reqCtx);

        Assertions.assertNull(renderizationInfo);
    }
}
