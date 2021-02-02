package org.entando.entando.plugins.jacms.web.contentmodel;

import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentModelDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.web.AbstractControllerIntegrationTest;
import org.entando.entando.web.MockMvcHelper;
import org.entando.entando.web.analysis.AnalysisControllerDiffAnalysisEngineTestsStubs;
import org.entando.entando.web.utils.OAuth2TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContentModelControllerIntegrationTest extends AbstractControllerIntegrationTest {
    private final EntLogger logger = EntLogFactory.getSanitizedLogger(getClass());

    private static final String BASE_URI = "/plugins/cms/contentmodels";

    @Autowired
    private IContentModelManager contentModelManager;

    private MockMvcHelper mockMvcHelper;
    private String accessToken;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        mockMvcHelper = new MockMvcHelper(mockMvc);
        UserDetails user = new OAuth2TestUtils.UserBuilder("jack_bauer", "0x24").grantedToRoleAdmin().build();
        this.accessToken = mockOAuthInterceptor(user);
    }

    @Test
    void testGetContentModelsSortId() throws Exception {

        UserDetails user = new OAuth2TestUtils.UserBuilder("jack_bauer", "0x24").grantedToRoleAdmin().build();
        ResultActions result = mockMvc
                .perform(get(BASE_URI)
                        .param("sort", "id")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload[0].id", is(1)));

        result = mockMvc
                .perform(get(BASE_URI)
                        .param("direction", FieldSearchFilter.DESC_ORDER)
                        .param("sort", "id")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload[0].id", is(13)));

    }

    @Test
    void testGetContentModelDefaultSorting() throws Exception {
        ResultActions result = mockMvc
                .perform(get(BASE_URI)
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.metaData.pageSize", is(100)));
        result.andExpect(jsonPath("$.metaData.sort", is("id")));
        result.andExpect(jsonPath("$.metaData.page", is(1)));
    }

    @Test
    void testGetContentModelsSortByDescr() throws Exception {

        ResultActions result = mockMvc
                .perform(get(BASE_URI)
                        .param("direction", FieldSearchFilter.ASC_ORDER)
                        .param("sort", "descr")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload[0].descr", is("List Model")));

        result = mockMvc
                .perform(get(BASE_URI)
                        .param("direction", FieldSearchFilter.DESC_ORDER)
                        .param("sort", "descr")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload[0].descr", is("scheda di un articolo")));

    }

    @Test
    void testGetContentModelsWithFilters() throws Exception {

        ResultActions result = mockMvc
                .perform(get(BASE_URI)
                        .param("direction", FieldSearchFilter.ASC_ORDER)
                        .param("sort", "descr")
                        .param("filters[0].attribute", "contentType")
                        .param("filters[0].value", "ART")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload.length()", is(4)));

        result = mockMvc
                .perform(get(BASE_URI)
                        .param("direction", FieldSearchFilter.ASC_ORDER)
                        .param("sort", "descr")
                        .param("filters[0].attribute", "contentType")
                        .param("filters[0].value", "ART")
                        .param("filters[1].attribute", "descr")
                        .param("filters[1].value", "MoDeL")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload.length()", is(2)));
    }

    @Test
    void testGetContentModelOk() throws Exception {

        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/{modelId}", "1")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload.id", is(1)));

    }

    @Test
    void testGetContentModelKo() throws Exception {

        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/{modelId}", "0")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isNotFound());
        result.andExpect(jsonPath("$.errors[0].code", is("1")));
    }

    @Test
    void testGetContentModelDictionary() throws Exception {

        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/dictionary")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());

    }

    @Test
    void testGetContentModelDictionaryWithTypeCode() throws Exception {

        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/dictionary")
                        .param("typeCode", "EVN")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload").isNotEmpty());
        result.andExpect(jsonPath("$.payload.$content").isNotEmpty());
    }

    @Test
    void testGetContentModelDictionaryValidTypeCodeInvalid() throws Exception {

        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/dictionary")
                        .param("typeCode", "LOL")
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isNotFound());
        result.andExpect(jsonPath("$.errors[0].code", is("6")));
    }

    @Test
    void testCrudContentModel() throws Exception {
        long modelId = 2001;
        try {
            String payload = null;

            ContentModelDto request = new ContentModelDto();
            request.setId(modelId);
            request.setContentType("ART");
            request.setDescr("testCrudContentModel");
            request.setContentShape("testCrudContentModel");

            payload = mapper.writeValueAsString(request);

            ResultActions result = mockMvc
                    .perform(post(BASE_URI)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isOk());
            ContentModel contentModelAdded = this.contentModelManager.getContentModel(modelId);
            assertThat(contentModelAdded.getId(), is(request.getId()));
            assertThat(contentModelAdded.getContentType(), is(request.getContentType()));
            assertThat(contentModelAdded.getDescription(), is(request.getDescr()));
            assertThat(contentModelAdded.getContentShape(), is(request.getContentShape()));

            //----------------------------------------------
            request.setId(modelId);
            request.setContentType("ART");
            request.setDescr("testCrudContentModel".toUpperCase());
            request.setContentShape("testCrudContentModel".toUpperCase());
            request.setStylesheet("Stylesheet".toUpperCase());

            payload = mapper.writeValueAsString(request);

            result = mockMvc
                    .perform(put(BASE_URI + "/{id}", modelId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isOk());
            contentModelAdded = this.contentModelManager.getContentModel(modelId);
            assertThat(contentModelAdded.getId(), is(request.getId()));
            assertThat(contentModelAdded.getContentType(), is(request.getContentType()));
            assertThat(contentModelAdded.getDescription(), is(request.getDescr()));
            assertThat(contentModelAdded.getContentShape(), is(request.getContentShape()));

            //----------------------------------------------
            result = mockMvc
                    .perform(delete(BASE_URI + "/{id}", modelId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isOk());
            result.andExpect(jsonPath("$.payload.modelId", is(String.valueOf(modelId))));
            contentModelAdded = this.contentModelManager.getContentModel(modelId);
            assertThat(contentModelAdded, is(nullValue()));

            //----------------------------------------------
        } finally {
            ContentModel model = this.contentModelManager.getContentModel(modelId);
            if (null != model) {

                this.contentModelManager.removeContentModel(model);
            }
        }
    }

    @Test
    void testAddWithInvalidContentType() throws Exception {
        long modelId = 2001;
        try {
            String payload = null;

            ContentModelDto request = new ContentModelDto();
            request.setId(modelId);
            request.setContentType("XXX");
            request.setDescr("testChangeContentType");
            request.setContentShape("testChangeContentType");

            payload = mapper.writeValueAsString(request);

            ResultActions result = mockMvc
                    .perform(post(BASE_URI)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isConflict());
            result.andExpect(jsonPath("$.errors[0].code", is("6")));

        } finally {
            ContentModel model = this.contentModelManager.getContentModel(modelId);
            if (null != model) {

                this.contentModelManager.removeContentModel(model);
            }
        }
    }

    @Test
    void testAddWithIdAboveMax() throws Exception {
        long modelId = new Long("2147483648");
        try {
            String payload = null;

            ContentModelDto request = new ContentModelDto();
            request.setId(modelId);
            request.setContentType("EVN");
            request.setDescr("testChangeContentType");
            request.setContentShape("testChangeContentType");

            payload = mapper.writeValueAsString(request);

            ResultActions result = mockMvc
                    .perform(post(BASE_URI)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isBadRequest());
            result.andExpect(jsonPath("$.errors[0].code", is("56")));

        } finally {
            ContentModel model = this.contentModelManager.getContentModel(modelId);
            if (null != model) {

                this.contentModelManager.removeContentModel(model);
            }
        }
    }

    @Test
    void testChangeContentType() throws Exception {
        long modelId = 2001;
        try {
            String payload = null;

            ContentModelDto request = new ContentModelDto();
            request.setId(modelId);
            request.setContentType("ART");
            request.setDescr("testChangeContentType");
            request.setContentShape("testChangeContentType");

            payload = mapper.writeValueAsString(request);

            ResultActions result = mockMvc
                    .perform(post(BASE_URI)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isOk());

            //----------------------------------------------
            request.setId(modelId);
            request.setContentType("EVN");
            request.setDescr("testCrudContentModel".toUpperCase());
            request.setContentShape("testCrudContentModel".toUpperCase());
            request.setStylesheet("Stylesheet".toUpperCase());

            payload = mapper.writeValueAsString(request);

            result = mockMvc
                    .perform(put(BASE_URI + "/{id}", modelId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.errors.size()", is(0)))
                    .andExpect(jsonPath("$.payload.contentType", is("EVN")));

        } finally {
            ContentModel model = this.contentModelManager.getContentModel(modelId);
            if (null != model) {

                this.contentModelManager.removeContentModel(model);
            }
        }
    }

    @Test
    void testChangeWithInvalidContentType() throws Exception {
        long modelId = 2001;
        try {
            String payload = null;

            ContentModelDto request = new ContentModelDto();
            request.setId(modelId);
            request.setContentType("ART");
            request.setDescr("testChangeContentType");
            request.setContentShape("testChangeContentType");

            payload = mapper.writeValueAsString(request);

            ResultActions result = mockMvc
                    .perform(post(BASE_URI)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isOk());

            request.setId(modelId);
            request.setContentType("EV9");
            request.setDescr("testCrudContentModel".toUpperCase());
            request.setContentShape("testCrudContentModel".toUpperCase());
            request.setStylesheet("Stylesheet".toUpperCase());

            payload = mapper.writeValueAsString(request);

            result = mockMvc
                    .perform(put(BASE_URI + "/{id}", modelId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errors[0].code", is(String.valueOf("6"))));

        } finally {
            ContentModel model = this.contentModelManager.getContentModel(modelId);
            if (null != model) {
                this.contentModelManager.removeContentModel(model);
            }
        }
    }

    @Test
    void testChangeContentShapeToNull() throws Exception {
        long modelId = 2001;
        try {
            String payload = null;

            ContentModelDto request = new ContentModelDto();
            request.setId(modelId);
            request.setContentType("ART");
            request.setDescr("testChangeContentType");
            request.setContentShape("testChangeContentType");

            payload = mapper.writeValueAsString(request);

            ResultActions result = mockMvc
                    .perform(post(BASE_URI)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isOk());

            //----------------------------------------------
            request.setId(modelId);
            request.setContentType("ART");
            request.setDescr("testChangeContentType");
            request.setContentShape(null);

            payload = mapper.writeValueAsString(request);

            result = mockMvc
                    .perform(put(BASE_URI + "/{id}", modelId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(payload)
                            .header("Authorization", "Bearer " + accessToken));

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.size()", is(1)));

        } finally {
            ContentModel model = this.contentModelManager.getContentModel(modelId);
            if (null != model) {

                this.contentModelManager.removeContentModel(model);
            }
        }
    }



    @Test
    void testDeleteReferencedModel() throws Throwable {

        ResultActions result = mockMvc
                .perform(delete(BASE_URI + "/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));

        result.andExpect(status().isConflict());
        result.andExpect(jsonPath("$.errors[0].code", is(String.valueOf("5"))));
    }

    @Test
    void testGetModelPageReferences() throws Throwable {

        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/{id}/pagereferences", 2)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
    }

    @Test
    void testGetTemplateUsage() throws Throwable {
        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/{id}/usage", 2)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(jsonPath("$.payload.type", is("contentTemplate")));
        result.andExpect(jsonPath("$.payload.code", is("2")));
        result.andExpect(jsonPath("$.payload.usage", is(2)));
    }

    @Test
    void testGetTemplateUsageCount() throws Throwable {
        ResultActions result = mockMvc
                .perform(get(BASE_URI + "/{id}/usage/details", 2)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(jsonPath("$.payload[0].type", is("page")));
        result.andExpect(jsonPath("$.payload[0].code", is("homepage")));
        result.andExpect(jsonPath("$.payload[0].status", is("offline")));
    }


    @Test
    void askingForUsageCountForNotExistingCodeShouldReturnZero() throws Throwable {

        String code = "9999";

        this.mockMvcHelper.setAccessToken(this.accessToken);
        this.mockMvcHelper.getMockMvc(BASE_URI + "/{code}/usage", null, code)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.type", is("contentTemplate")))
                .andExpect(jsonPath("$.payload.code", is(code)))
                .andExpect(jsonPath("$.payload.usage", is(0)));
    }

    @Test
    void testComponentExistenceAnalysis() throws Exception {
        // should return DIFF for existing component
        AnalysisControllerDiffAnalysisEngineTestsStubs.testComponentCmsAnalysisResult(
                AnalysisControllerDiffAnalysisEngineTestsStubs.COMPONENT_CONTENT_TEMPLATES,
                "2",
                AnalysisControllerDiffAnalysisEngineTestsStubs.STATUS_DIFF,
                new ContextOfControllerTests(mockMvc, mapper)
        );

        // should return NEW for NON existing component
        AnalysisControllerDiffAnalysisEngineTestsStubs.testComponentCmsAnalysisResult(
                AnalysisControllerDiffAnalysisEngineTestsStubs.COMPONENT_CONTENT_TEMPLATES,
                "999",
                AnalysisControllerDiffAnalysisEngineTestsStubs.STATUS_NEW,
                new ContextOfControllerTests(mockMvc, mapper)
        );
    }

    @Test
    void testGetContentModelsFilterId() throws Exception {
        
        mockMvc
                .perform(get(BASE_URI)
                        .param("filters[0].attribute", "id")
                        .param("filters[0].value", "1")
                        .param("filters[0].operator", "like")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.length()", is(1)));

        mockMvc
                .perform(get(BASE_URI)
                        .param("filters[0].attribute", "id")
                        .param("filters[0].value", "abc")
                        .param("filters[0].operator", "like")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.payload.length()", is(0)));
    }
}