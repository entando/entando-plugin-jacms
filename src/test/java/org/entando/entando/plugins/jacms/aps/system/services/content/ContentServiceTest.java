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
package org.entando.entando.plugins.jacms.aps.system.services.content;

import static org.mockito.Mockito.when;

import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.common.entity.model.EntitySearchFilter;
import com.agiletec.aps.system.services.authorization.IAuthorizationManager;
import com.agiletec.aps.system.services.category.CategoryUtilizer;
import com.agiletec.aps.system.services.group.GroupUtilizer;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.PageUtilizer;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.content.ContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.ContentUtilizer;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.IContentAuthorizationHelper;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.PublicContentAuthorizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentDto;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.ContentRenderizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.IContentDispenser;
import com.agiletec.plugins.jacms.aps.system.services.searchengine.ICmsSearchEngineManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.entando.entando.aps.system.exception.ResourceNotFoundException;
import org.entando.entando.aps.system.exception.RestServerError;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.plugins.jacms.web.content.validator.RestContentListRequest;
import org.entando.entando.web.common.exceptions.ValidationGenericException;
import org.entando.entando.web.common.model.Filter;
import org.entando.entando.web.common.model.PagedMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

/**
 * @author E.Santoboni
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    public static final String FOUND_CONTENT_01 = "ART2";
    public static final String FOUND_CONTENT_02 = "ART5";
    public static final String FOUND_CONTENT_03 = "ART6";
    @Mock
    private ILangManager langManager;
    @Mock
    private ContentManager contentManager;
    @Mock
    private IContentModelManager contentModelManager;
    @Mock
    private IAuthorizationManager authorizationManager;
    @Mock
    private IContentAuthorizationHelper contentAuthorizationHelper;
    @Mock
    private IContentDispenser contentDispenser;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ICmsSearchEngineManager searchEngineManager;
    @InjectMocks
    private ContentService contentService;

    @BeforeEach
    public void setUp() throws Exception {
        ContentServiceUtilizer serviceUtilizer = Mockito.mock(ContentServiceUtilizer.class);
        Map<String, ContentServiceUtilizer> map = new HashMap<>();
        map.put("service", serviceUtilizer);
        Mockito.lenient().when(this.applicationContext.getBeansOfType(ContentServiceUtilizer.class)).thenReturn(map);
    }

    @Test
    public void getGroupUtilizer() throws Exception {
        List<String> contentsId = Arrays.asList("ART1", "ART2", "ART3"); // ART3 is unpublished
        when(((GroupUtilizer) this.contentManager).getGroupUtilizers(Mockito.anyString())).thenReturn(contentsId);
        when(this.contentManager.loadContent(AdditionalMatchers.or(Mockito.eq("ART1"), Mockito.eq("ART2")),
                Mockito.eq(true))).thenReturn(Mockito.mock(Content.class));
        List<ContentDto> dtos = this.contentService.getGroupUtilizer("groupName");
        Assertions.assertEquals(2, dtos.size());
        Mockito.verify(((GroupUtilizer<String>) this.contentManager), Mockito.times(1)).getGroupUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(3)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getGroupUtilizerWithError() throws Exception {
        when(((GroupUtilizer) this.contentManager).getGroupUtilizers(Mockito.anyString())).thenThrow(EntException.class);
        Assertions.assertThrows(RestServerError.class, () -> {
            List<ContentDto> dtos = this.contentService.getGroupUtilizer("groupName");
        });
        Mockito.verify(((GroupUtilizer) this.contentManager), Mockito.times(1)).getGroupUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(0)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getCategoryUtilizer() throws Exception {
        List<String> contentsId = Arrays.asList("ART11", "ART22", "ART33");
        when(((CategoryUtilizer) this.contentManager).getCategoryUtilizers(Mockito.anyString())).thenReturn(contentsId);
        when(this.contentManager.loadContent(Mockito.anyString(), Mockito.eq(true))).thenReturn(Mockito.mock(Content.class));
        List<ContentDto> dtos = this.contentService.getCategoryUtilizer("categoryCode");
        Assertions.assertEquals(3, dtos.size());
        Mockito.verify(((CategoryUtilizer) this.contentManager), Mockito.times(1)).getCategoryUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(3)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getCategoryUtilizerWithError() throws Exception {
        when(((CategoryUtilizer) this.contentManager).getCategoryUtilizers(Mockito.anyString())).thenThrow(EntException.class);
        Assertions.assertThrows(RestServerError.class, () -> {
            List<ContentDto> dtos = this.contentService.getCategoryUtilizer("categoryCode");
        });
        Mockito.verify(((CategoryUtilizer) this.contentManager), Mockito.times(1)).getCategoryUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(0)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getPageUtilizer() throws Exception {
        List<String> contentsId = Arrays.asList("ART1111", "ART2222", "ART333", "ART444", "ART5");
        when(((PageUtilizer) this.contentManager).getPageUtilizers(Mockito.anyString())).thenReturn(contentsId);
        when(this.contentManager.loadContent(Mockito.anyString(), Mockito.eq(true))).thenReturn(Mockito.mock(Content.class));
        List<ContentDto> dtos = this.contentService.getPageUtilizer("pageCode");
        Assertions.assertEquals(5, dtos.size());
        Mockito.verify(((PageUtilizer) this.contentManager), Mockito.times(1)).getPageUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(5)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getPageUtilizerWithError() throws Exception {
        when(((PageUtilizer) this.contentManager).getPageUtilizers(Mockito.anyString())).thenThrow(EntException.class);
        Assertions.assertThrows(RestServerError.class, () -> {
            List<ContentDto> dtos = this.contentService.getPageUtilizer("pageCode");
        });
        Mockito.verify(((PageUtilizer) this.contentManager), Mockito.times(1)).getPageUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(0)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getContentUtilizer() throws Exception {
        List<String> contentsId = Arrays.asList("ART1111", "ART2222", "ART333", "ART444", "ART5", "ART6");
        when(((ContentUtilizer) this.contentManager).getContentUtilizers(Mockito.anyString())).thenReturn(contentsId);
        when(this.contentManager.loadContent(Mockito.anyString(), Mockito.eq(true))).thenReturn(Mockito.mock(Content.class));
        List<ContentDto> dtos = this.contentService.getContentUtilizer("NEW456");
        Assertions.assertEquals(6, dtos.size());
        Mockito.verify(((ContentUtilizer) this.contentManager), Mockito.times(1)).getContentUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(6)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getContentUtilizerWithError() throws Exception {
        when(((ContentUtilizer) this.contentManager).getContentUtilizers(Mockito.anyString())).thenThrow(EntException.class);
        Assertions.assertThrows(RestServerError.class, () -> {
            List<ContentDto> dtos = this.contentService.getContentUtilizer("NEW456");
        });
        Mockito.verify(((ContentUtilizer) this.contentManager), Mockito.times(1)).getContentUtilizers(Mockito.anyString());
        Mockito.verify(this.contentManager, Mockito.times(0)).loadContent(Mockito.anyString(), Mockito.eq(true));
    }

    @Test
    public void getContentsWithHtml() throws Exception {
        RestContentListRequest requestList = this.createContentsRequest();
        requestList.setStatus(IContentService.STATUS_ONLINE);
        requestList.setPageSize(2);
        UserDetails user = Mockito.mock(UserDetails.class);
        when(this.langManager.getDefaultLang()).thenReturn(Mockito.mock(Lang.class));
        when(this.authorizationManager.getUserGroups(user)).thenReturn(new ArrayList<>());
        List<String> contentsId = Arrays.asList("ART1", "ART2", "ART3", "ART4", "ART5", "ART6");
        when((this.contentManager).loadPublicContentsId(Mockito.nullable(String[].class), Mockito.anyBoolean(),
                Mockito.nullable(EntitySearchFilter[].class), Mockito.any(List.class))).thenReturn(contentsId);
        when((this.contentDispenser).getRenderizationInfo(Mockito.nullable(String.class), Mockito.anyLong(),
                Mockito.nullable(String.class), Mockito.nullable(UserDetails.class), Mockito.anyBoolean())).thenReturn(Mockito.mock(ContentRenderizationInfo.class));
        this.createMockContent("ART");
        this.createMockContentModel("ART");
        PagedMetadata<ContentDto> metadata = this.contentService.getContents(requestList, user);
        Assertions.assertEquals(2, metadata.getBody().size());
        Mockito.verify(this.contentManager, Mockito.times(2)).loadContent(Mockito.anyString(), Mockito.eq(true));
        Mockito.verify(this.contentModelManager, Mockito.times(2)).getContentModel(10);
        Mockito.verify(this.contentDispenser, Mockito.times(2))
                .resolveLinks(Mockito.any(ContentRenderizationInfo.class), Mockito.nullable(RequestContext.class));
        Mockito.verifyZeroInteractions(this.searchEngineManager);
    }

    @Test
    public void getContentsWithoutHtml() throws Exception {
        UserDetails user = Mockito.mock(UserDetails.class);
        RestContentListRequest requestList = prepareGetContentTest(user);
        this.createMockContent("ART");
        PagedMetadata<ContentDto> metadata = this.contentService.getContents(requestList, user);
        Assertions.assertEquals(3, metadata.getBody().size());
        Mockito.verify(this.contentManager, Mockito.times(3)).loadContent(Mockito.anyString(), Mockito.eq(true));
        Mockito.verifyZeroInteractions(this.contentDispenser);
        Mockito.verifyZeroInteractions(this.contentModelManager);
    }

    @Test
    void testGetLinkableContentsViaOwnerGroup() throws Exception {
        UserDetails user = Mockito.mock(UserDetails.class);
        RestContentListRequest requestList = prepareGetContentTest(user);
        requestList.setForLinkingWithOwnerGroup("GROUP1");

        this.addMockedContent("ART2", "ART", "GROUP1", null);
        this.addMockedContent("ART5", "ART", "GROUP1", null);
        this.addMockedContent("ART6", "ART", "GROUP2", null);

        // test
        PagedMetadata<ContentDto> metadata = this.contentService.getContents(requestList, user);

        List<ContentDto> body = metadata.getBody();
        Assertions.assertEquals(2, body.size());
        Assertions.assertEquals("GROUP1", body.get(0).getMainGroup());
        Assertions.assertEquals("GROUP1", body.get(1).getMainGroup());
    }

    @Test
    void testGetLinkableContentsViaExtraGroup() throws Exception {
        UserDetails user = Mockito.mock(UserDetails.class);
        RestContentListRequest requestList = prepareGetContentTest(user);
        requestList.setForLinkingWithOwnerGroup("GROUP1");
        requestList.setForLinkingWithExtraGroups(Arrays.asList("GROUP2"));

        this.addMockedContent(FOUND_CONTENT_01, "ART", "GROUP1", "GROUP2");
        this.addMockedContent(FOUND_CONTENT_02, "ART", "GROUP1", null);
        this.addMockedContent(FOUND_CONTENT_03, "ART", "GROUP2", "GROUP1");

        // test
        PagedMetadata<ContentDto> metadata = this.contentService.getContents(requestList, user);

        List<ContentDto> body = metadata.getBody();
        Assertions.assertEquals(2, body.size());
        Assertions.assertEquals("GROUP1", body.get(0).getMainGroup());
        Assertions.assertEquals("GROUP2", body.get(1).getMainGroup());
    }

    @Test
    public void getContentsWithModelError_1() throws Exception {
        RestContentListRequest requestList = this.createContentsRequest();
        requestList.setStatus(IContentService.STATUS_ONLINE);
        requestList.setModel("34");
        UserDetails user = Mockito.mock(UserDetails.class);
        Mockito.lenient().when(this.langManager.getDefaultLang()).thenReturn(Mockito.mock(Lang.class));
        when(this.authorizationManager.getUserGroups(user)).thenReturn(new ArrayList<>());
        List<String> contentsId = Arrays.asList("ART1", "ART2", "ART3", "ART4", "ART5", "ART6");
        when((this.contentManager).loadPublicContentsId(Mockito.nullable(String[].class), Mockito.anyBoolean(),
                Mockito.nullable(EntitySearchFilter[].class), Mockito.any(List.class))).thenReturn(contentsId);
        this.createMockContent("ART");
        this.createMockContentModel("ART");
        when(this.contentModelManager.getContentModel(34)).thenReturn(null);
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            PagedMetadata<ContentDto> metadata = this.contentService.getContents(requestList, user);
        });
        Mockito.verify(this.contentManager, Mockito.times(1)).loadContent(Mockito.anyString(), Mockito.eq(true));
        Mockito.verify(this.contentModelManager, Mockito.times(1)).getContentModel(34);
        Mockito.verifyZeroInteractions(this.searchEngineManager);
        Mockito.verifyZeroInteractions(this.contentDispenser);
    }

    @Test
    public void getContentsWithModelError_2() throws Exception {
        RestContentListRequest requestList = this.createContentsRequest();
        requestList.setStatus(IContentService.STATUS_ONLINE);
        requestList.setModel("list");
        UserDetails user = Mockito.mock(UserDetails.class);
        Mockito.lenient().when(this.langManager.getDefaultLang()).thenReturn(Mockito.mock(Lang.class));
        when(this.authorizationManager.getUserGroups(user)).thenReturn(new ArrayList<>());
        List<String> contentsId = Arrays.asList("ART1", "ART2", "ART3", "ART4", "ART5", "ART6");
        when((this.contentManager).loadPublicContentsId(Mockito.nullable(String[].class), Mockito.anyBoolean(),
                Mockito.nullable(EntitySearchFilter[].class), Mockito.any(List.class))).thenReturn(contentsId);
        this.createMockContent("ART");
        this.createMockContentModel("NEW");
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            PagedMetadata<ContentDto> metadata = this.contentService.getContents(requestList, user);
        });
        Mockito.verify(this.contentManager, Mockito.times(1)).loadContent(Mockito.anyString(), Mockito.eq(true));
        Mockito.verify(this.contentModelManager, Mockito.times(1)).getContentModel(10);
        Mockito.verifyZeroInteractions(this.searchEngineManager);
        Mockito.verifyZeroInteractions(this.contentDispenser);
    }

    protected RestContentListRequest createContentsRequest() {
        RestContentListRequest requestList = new RestContentListRequest();
        Filter[] filters = new Filter[]{new Filter("attribute", "test", "eq")};
        requestList.setFilters(filters);
        requestList.setModel("list");
        requestList.setResolveLink(true);
        requestList.setStatus(IContentService.STATUS_DRAFT);
        return requestList;
    }

    protected void createMockContent(String typeCode) throws Exception {
        addMockedContent(null, typeCode, null, null);
    }

    protected void addMockedContent(String id, String typeCode,
            @Nullable String ownerGroup, @Nullable String extraGroup) throws Exception {
        Content mockContent = Mockito.mock(Content.class);
        when(mockContent.getListModel()).thenReturn("10");
        when(mockContent.getDefaultModel()).thenReturn("20");
        when(mockContent.getTypeCode()).thenReturn(typeCode);
        if (ownerGroup != null) {
            when(mockContent.getMainGroup()).thenReturn(ownerGroup);
        }
        if (extraGroup != null) {
            when(mockContent.getGroups()).thenReturn(new HashSet<>(Arrays.asList(extraGroup.split(","))));
        }
        if (id == null) {
            when(this.contentManager.loadContent(Mockito.anyString(), Mockito.eq(true))).thenReturn(mockContent);
        } else {
            when(this.contentManager.loadContent(Mockito.eq(id), Mockito.eq(true))).thenReturn(mockContent);
        }
    }

    protected void createMockContentModel(String typeCode) throws Exception {
        ContentModel mockContentModel = Mockito.mock(ContentModel.class);
        Mockito.lenient().when(mockContentModel.getContentType()).thenReturn(typeCode);
        Mockito.lenient().when(mockContentModel.getContentShape()).thenReturn("Content model");
        when(this.contentModelManager.getContentModel(Mockito.anyLong())).thenReturn(mockContentModel);
    }

    @Test
    public void getContent() throws Exception {
        UserDetails user = Mockito.mock(UserDetails.class);
        when(this.langManager.getDefaultLang()).thenReturn(Mockito.mock(Lang.class));
        when(this.authorizationManager.getUserGroups(user)).thenReturn(new ArrayList<>());
        PublicContentAuthorizationInfo pcai = Mockito.mock(PublicContentAuthorizationInfo.class);
        when(pcai.isUserAllowed(ArgumentMatchers.<String>anyList())).thenReturn(true);
        when(this.contentAuthorizationHelper.getAuthorizationInfo(Mockito.anyString())).thenReturn(pcai);
        Mockito.lenient().when((this.contentDispenser).getRenderizationInfo(Mockito.nullable(String.class), Mockito.anyLong(),
                Mockito.nullable(String.class), Mockito.nullable(RequestContext.class), Mockito.anyBoolean())).thenReturn(Mockito.mock(ContentRenderizationInfo.class));
        this.createMockContent("ART");
        this.createMockContentModel("ART");
        ContentDto dto = this.contentService.getContent("ART11", "list", IContentService.STATUS_ONLINE, null, false, user);
        Assertions.assertNotNull(dto);
        Mockito.verify(this.contentManager, Mockito.times(1)).loadContent(Mockito.anyString(), Mockito.eq(true));
        Mockito.verify(this.contentModelManager, Mockito.times(1)).getContentModel(10);
        Mockito.verify(this.contentDispenser, Mockito.times(0))
                .resolveLinks(Mockito.any(ContentRenderizationInfo.class), Mockito.nullable(RequestContext.class));
        Mockito.verifyZeroInteractions(this.searchEngineManager);
    }

    @Test
    public void getContentWithError_1() throws Exception {
        UserDetails user = Mockito.mock(UserDetails.class);
        Mockito.lenient().when(this.langManager.getDefaultLang()).thenReturn(Mockito.mock(Lang.class));
        when(this.authorizationManager.getUserGroups(user)).thenReturn(new ArrayList<>());
        PublicContentAuthorizationInfo pcai = Mockito.mock(PublicContentAuthorizationInfo.class);
        when(pcai.isUserAllowed(ArgumentMatchers.<String>anyList())).thenReturn(true);
        when(this.contentAuthorizationHelper.getAuthorizationInfo(Mockito.anyString())).thenReturn(pcai);
        when(this.contentManager.loadContent(Mockito.anyString(), Mockito.eq(true))).thenReturn(null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            ContentDto dto = this.contentService.getContent("ART11", "list", IContentService.STATUS_ONLINE, null, false, user);
        });
        Mockito.verify(this.contentManager, Mockito.times(1)).loadContent(Mockito.anyString(), Mockito.eq(true));
        Mockito.verifyZeroInteractions(this.contentModelManager);
        Mockito.verifyZeroInteractions(this.contentDispenser);
        Mockito.verifyZeroInteractions(this.searchEngineManager);
    }

    @Test
    public void getContentWithError_2() throws Exception {
        UserDetails user = Mockito.mock(UserDetails.class);
        when(this.contentAuthorizationHelper.getAuthorizationInfo(Mockito.anyString())).thenReturn(null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            ContentDto dto = this.contentService.getContent("ART11", "list", IContentService.STATUS_ONLINE, null, false, user);
        });
        Mockito.verifyZeroInteractions(this.contentModelManager);
        Mockito.verifyZeroInteractions(this.contentDispenser);
        Mockito.verifyZeroInteractions(this.contentManager);
        Mockito.verifyZeroInteractions(this.searchEngineManager);
        Mockito.verifyZeroInteractions(this.langManager);
    }

    private RestContentListRequest prepareGetContentTest(UserDetails user) throws EntException {
        RestContentListRequest requestList = this.createContentsRequest();
        requestList.setStatus(IContentService.STATUS_ONLINE);
        requestList.setModel(null);
        requestList.setPageSize(5);
        requestList.setText("text");
        when(this.langManager.getDefaultLang()).thenReturn(Mockito.mock(Lang.class));
        when(this.authorizationManager.getUserGroups(user)).thenReturn(new ArrayList<>());
        List<String> contentsId = new ArrayList<>(
                Arrays.asList("ART1", FOUND_CONTENT_01, "ART3", "ART4", FOUND_CONTENT_02, FOUND_CONTENT_03));
        when((this.contentManager).loadPublicContentsId(Mockito.nullable(String[].class), Mockito.anyBoolean(),
                Mockito.nullable(EntitySearchFilter[].class), Mockito.any(List.class))).thenReturn(contentsId);
        when(this.searchEngineManager.searchEntityId(Mockito.nullable(String.class),
                Mockito.eq("text"), Mockito.any())).thenReturn(
                Arrays.asList("ART7", FOUND_CONTENT_03, "ART8", "ART12", FOUND_CONTENT_01, FOUND_CONTENT_02));
        return requestList;
    }
    
}
