package org.entando.entando.plugins.jacms.web.analysis;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.content.ContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentModelDto;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentTypeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.entando.entando.plugins.jacms.aps.system.services.ContentModelServiceImpl;
import org.entando.entando.plugins.jacms.aps.system.services.ContentTypeService;
import org.entando.entando.plugins.jacms.aps.system.services.content.ContentService;
import org.entando.entando.plugins.jacms.aps.system.services.resource.ResourcesService;
import org.entando.entando.web.AbstractControllerTest;
import org.entando.entando.web.utils.OAuth2TestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerCmsTest extends AbstractControllerTest {

    @Test
    void testRunAnalysis() throws Exception {
        String accessToken = mockAccessToken();

        // CONTENTS
        Mockito.lenient().doCallRealMethod().when(contentService).exists(Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doCallRealMethod().when(contentService).setContentManager(contentManager);
        contentService.setContentManager(contentManager);

        Mockito.lenient().doReturn(content).when(contentManager).loadContent("1", true);
        Mockito.lenient().doReturn(content).when(contentManager).loadContent("2", true);
        Mockito.lenient().doReturn(null).when(contentManager).loadContent("3", false);

        // CONTENT TYPE
        Mockito.lenient().doCallRealMethod().when(contentTypeService).exists(Mockito.anyString());
        Mockito.doReturn(true).when(contentTypeService).exists("1");
        Mockito.doReturn(true).when(contentTypeService).exists("2");
        Mockito.doReturn(false).when(contentTypeService).exists("3");

        // CONTENT MODELS
        Mockito.doReturn(true).when(contentModelService).exists("1");
        Mockito.doReturn(true).when(contentModelService).exists("2");
        Mockito.doReturn(false).when(contentModelService).exists("3");

        // ASSETS
        Mockito.doReturn(true).when(resourcesService).exists("1");
        Mockito.doReturn(true).when(resourcesService).exists("2");
        Mockito.doReturn(false).when(resourcesService).exists("3");

        // REQUEST
        Map<String, List<String>> request = ImmutableMap.of(
                "contents", ImmutableList.of("1", "2", "3"),
                "contentTypes", ImmutableList.of("1", "2", "3"),
                "contentTemplates", ImmutableList.of("1", "2", "3"),
                "assets", ImmutableList.of("1", "2", "3")
        );

        ResultActions result = mockMvc.perform(
                post("/analysis/cms/components/diff")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));

        result.andExpect(status().isOk());
        result.andDo(MockMvcResultHandlers.print());
        result.andExpect(content().contentType("application/json"));
        checkByComponentType(result, "contents");
        checkByComponentType(result, "contentTypes");
        checkByComponentType(result, "contentTemplates");
        checkByComponentType(result, "assets");
    }

    @Test
    void testRunAnalysisWrongObjectType() throws Exception {
        String accessToken = mockAccessToken();

        Map<String, List<String>> request = ImmutableMap.of(
                "nonexistent-object-type", ImmutableList.of("1", "2", "3")
        );

        ResultActions result = mockMvc.perform(
                post("/analysis/cms/components/diff")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));

        result.andExpect(status().is(400));
    }


    private void checkByComponentType(ResultActions result, String componentType) throws Exception {
        result.andExpect(jsonPath("$.payload." + componentType + ".1", Matchers.equalTo("DIFF")));
        result.andExpect(jsonPath("$.payload." + componentType + ".2", Matchers.equalTo("DIFF")));
        result.andExpect(jsonPath("$.payload." + componentType + ".3", Matchers.equalTo("NEW")));
    }

    private String mockAccessToken() {
        UserDetails user = new OAuth2TestUtils.UserBuilder("jack_bauer", "0x24").grantedToRoleAdmin().build();
        return mockOAuthInterceptor(user);
    }

    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private AnalysisControllerCms controller;

    @Mock
    Content content;
    @Mock
    ContentManager contentManager;
    @Spy
    ContentService contentService;
    @Mock
    ContentTypeService contentTypeService;
    @Mock
    ContentTypeDto contentType;
    @Mock
    ContentModelServiceImpl contentModelService;
    @Mock
    ContentModelDto contentModel;
    @Mock
    ResourcesService resourcesService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(entandoOauth2Interceptor)
                .setHandlerExceptionResolvers(createHandlerExceptionResolver())
                .build();
    }
    
}
