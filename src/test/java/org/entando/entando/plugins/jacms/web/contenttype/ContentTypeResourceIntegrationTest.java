/*
 * Copyright 2018-Present Entando Inc. (http://www.entando.com) All rights reserved.
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
package org.entando.entando.plugins.jacms.web.contenttype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agiletec.aps.system.common.entity.IEntityTypesConfigurer;
import com.agiletec.aps.system.common.notify.NotifyManager;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.aps.util.FileTextReader;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentTypeDto;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentTypeDtoRequest;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentTypeRefreshRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.entando.entando.aps.system.services.entity.model.EntityTypeAttributeFullDto;
import org.entando.entando.plugins.jacms.web.content.ContentTypeResourceController;
import org.entando.entando.web.AbstractControllerIntegrationTest;
import org.entando.entando.web.MockMvcHelper;
import org.entando.entando.web.analysis.AnalysisControllerDiffAnalysisEngineTestsStubs;
import org.entando.entando.web.common.model.RestResponse;
import org.entando.entando.web.utils.OAuth2TestUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class ContentTypeResourceIntegrationTest extends AbstractControllerIntegrationTest {

    private ObjectMapper jsonMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private String accessToken;

    private MockMvcHelper mockMvcHelper;

    @Autowired
    private IContentManager contentManager;

    @BeforeEach
    public void setupTest() throws Throwable {
        mockMvcHelper = new MockMvcHelper(mockMvc);
        UserDetails user = new OAuth2TestUtils.UserBuilder("jack_bauer", "0x24").grantedToRoleAdmin().build();
        accessToken = mockOAuthInterceptor(user);
    }
    
    @Test
    void testGetReturnsList() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.metaData.pageSize").value("100"))
                .andReturn();

        mockMvc.perform(
                get("/plugins/cms/contentTypes")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUnauthorizedContentType() throws Exception {
        String typeCode = "TX0";
        Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        Content content = new Content();
        content.setTypeCode(typeCode);
        content.setTypeDescription("My content type " + typeCode);
        content.setDefaultModel("My Model");
        content.setListModel("Model list");
        ContentTypeDtoRequest contentTypeRequest = new ContentTypeDtoRequest(content);
        contentTypeRequest.setName("Content request");
        mockMvc.perform(
                post("/plugins/cms/contentTypes")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsString(contentTypeRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isUnauthorized());
        Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
    }

    @Test
    void testCreateContentType() throws Exception {
        String typeCode = "TX1";
        try {
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
            Content content = new Content();
            content.setTypeCode(typeCode);
            content.setTypeDescription("My content type " + typeCode);
            content.setDefaultModel("My Model");
            content.setListModel("Model list");
            content.setViewPage("View Page");
            ContentTypeDtoRequest contentTypeRequest = new ContentTypeDtoRequest(content);
            contentTypeRequest.setName("Content request");
            ResultActions result = mockMvc.perform(
                    post("/plugins/cms/contentTypes")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(jsonMapper.writeValueAsString(contentTypeRequest))
                    .accept(MediaType.APPLICATION_JSON_UTF8));

            result.andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.payload.code", is(typeCode)))
                    .andExpect(jsonPath("$.payload.viewPage", is("View Page")))
                    .andExpect(jsonPath("$.payload.defaultContentModel", is("My Model")))
                    .andExpect(jsonPath("$.payload.defaultContentModelList", is("Model list")));

            Assertions.assertNotNull(this.contentManager.getEntityPrototype(typeCode));
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testUpdateContentType() throws Exception {
        String typeCode = "TX2";
        try {
            ContentTypeDto createdContentType = createContentType(typeCode);
            createdContentType.setName("MyContentType");
            createdContentType.setViewPage("Updated View Page");
            createdContentType.setDefaultContentModel("Updated Model");
            createdContentType.setDefaultContentModelList("Updated Model list");

            ResultActions result = mockMvc.perform(put("/plugins/cms/contentTypes")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(jsonMapper.writeValueAsString(createdContentType))
                    .accept(MediaType.APPLICATION_JSON_UTF8));

            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.payload.code").value(createdContentType.getCode()))
                    .andExpect(jsonPath("$.payload.name").value("MyContentType"))
                    .andExpect(jsonPath("$.payload.viewPage", is("Updated View Page")))
                    .andExpect(jsonPath("$.payload.defaultContentModel", is("Updated Model")))
                    .andExpect(jsonPath("$.payload.defaultContentModelList", is("Updated Model list")));
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testUpdateNonExistentContentType() throws Exception {
        String typeCode = "TX2";
        try {
            ContentTypeDto createdContentType = createContentType(typeCode);
            createdContentType.setCode("999");

            ResultActions result = mockMvc.perform(put("/plugins/cms/contentTypes")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(jsonMapper.writeValueAsString(createdContentType))
                    .accept(MediaType.APPLICATION_JSON_UTF8));

            result.andDo(print())
                    .andExpect(status().isNotFound());
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testDeleteContentType() throws Exception {
        String typeCode = "TX3";
        try {
            this.createContentType(typeCode);
            mockMvc.perform(
                    delete("/plugins/cms/contentTypes/{code}", typeCode)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.payload.contentTypeCode", is(typeCode)))
                    .andReturn();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        } catch (Exception e) {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
            throw e;
        }
    }

    @Test
    void testGetContentType() throws Exception {
        this.executeGetContentType("ART", status().isOk());
        this.executeGetContentType("XXX", status().isNotFound());
    }

    private void executeGetContentType(String typeCode, ResultMatcher expectedResult) throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypes/{code}", typeCode)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(expectedResult)
                .andReturn();
        mockMvc.perform(
                get("/plugins/cms/contentTypes/{code}", typeCode)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(expectedResult)
                .andReturn();
    }

    @Test
    void testCreateAndGetContentType() throws Exception {
        String typeCode = "TX4";
        mockMvc.perform(
                get("/plugins/cms/contentTypes/{code}", typeCode)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound())
                .andReturn();
        try {
            ContentTypeDto createdContentTypeDto = this.createContentType(typeCode);
            MvcResult mvcResult = mockMvc.perform(
                    get("/plugins/cms/contentTypes/{code}", createdContentTypeDto.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
            ContentTypeDto contentTypeDto = stringToContentTypeDto(mvcResult);
            assertThat(contentTypeDto).isEqualToComparingFieldByField(createdContentTypeDto);
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testCreateExistingContentType() throws Exception {
        String typeCode = "FIR";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("**MARKER**", typeCode);
        placeholders.put("**NAME**", "My Content Type");

        try {
            //Create ContentType
            executeContentTypePost("1_type_valid.json", placeholders, accessToken, status().isCreated())
                    .andDo(print())
                    .andExpect(jsonPath("$.payload.code", is(typeCode)));

            //Same request returns 201 Created
            executeContentTypePost("1_type_valid.json", placeholders, accessToken, status().isCreated())
                    .andDo(print())
                    .andExpect(jsonPath("$.payload.code", is(typeCode)));

            //Same code, different object, returns 409 Conflict
            placeholders.put("**NAME**", "Different name...");
            executeContentTypePost("1_type_valid.json", placeholders, accessToken, status().isConflict());

            Assertions.assertNotNull(this.contentManager.getEntityPrototype(typeCode));
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testGetAllAttributes() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath(
                        "$.payload",
                        containsInAnyOrder(
                                "Attach", "Boolean", "CheckBox", "Composite", "Date", "Enumerator",
                                "EnumeratorMap", "Hypertext", "Image", "Link", "List", "Longtext",
                                "Monolist", "Monotext", "Number", "Text", "ThreeState", "Timestamp", "Email")))
                .andReturn();
    }

    @Test
    void testGetAttachAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Attach")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Attach"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(false))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetBooleanAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Boolean")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Boolean"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(false))
                .andReturn();
    }

    @Test
    void testGetCheckBoxAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "CheckBox")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("CheckBox"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(false))
                .andReturn();
    }

    @Test
    void testGetCompositeAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Composite")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Composite"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(false))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(false))
                .andReturn();
    }

    @Test
    void testGetDateAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Date")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Date"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetEnumeratorAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Enumerator")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Enumerator"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetEnumeratorMapAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "EnumeratorMap")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("EnumeratorMap"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetHypertextAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Hypertext")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Hypertext"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(false))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetImageAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Image")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Image"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(false))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetLinkAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Link")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Link"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(false))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetListAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "List")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("List"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(false))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(false))
                .andReturn();
    }

    @Test
    void testGetLongtextAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Longtext")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Longtext"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetMonolistAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Monolist")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Monolist"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(false))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(false))
                .andReturn();
    }

    @Test
    void testGetMonotextAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Monotext")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Monotext"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetNumberAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Number")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Number"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetTextAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Text")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Text"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }

    @Test
    void testGetThreeStateAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "ThreeState")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("ThreeState"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(false))
                .andReturn();
    }

    @Test
    void testGetTimestampAttribute() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypeAttributes/{attributeTypeCode}", "Timestamp")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payload.code").value("Timestamp"))
                .andExpect(jsonPath("$.payload.searchableOptionSupported").value(true))
                .andExpect(jsonPath("$.payload.indexableOptionSupported").value(true))
                .andReturn();
    }
    
    @Test
    void testGetContentAttributeType() throws Exception {
        ResultActions result = mockMvc
                .perform(get("/plugins/cms/contentTypeAttributes/{contentTypeCode}/attribute/{attributeTypeCode}", new Object[]{"XXX", "Monotext"})
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isNotFound());
        result.andExpect(jsonPath("$.payload", Matchers.hasSize(0)));
        result.andExpect(jsonPath("$.errors", Matchers.hasSize(1)));
        result.andExpect(jsonPath("$.errors[0].code", is("1")));
        result.andExpect(jsonPath("$.metaData.size()", is(0)));

        result = mockMvc
                .perform(get("/plugins/cms/contentTypeAttributes/{contentTypeCode}/attribute/{attributeTypeCode}", new Object[]{"ART", "Monotext"})
                        .header("Authorization", "Bearer " + accessToken));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.payload.multilingual", is(false)));
        result.andExpect(jsonPath("$.payload.dateFilterSupported", is(false)));
        result.andExpect(jsonPath("$.payload.assignedRoles.size()", is(1)));
        result.andExpect(jsonPath("$.payload.assignedRoles.jacms:title", is("Titolo")));
        result.andExpect(jsonPath("$.payload.allowedRoles", Matchers.hasSize(1)));
        result.andExpect(jsonPath("$.payload.dateFilterSupported", is(false)));
        result.andExpect(jsonPath("$.payload.simple", is(true)));
        result.andExpect(jsonPath("$.errors", Matchers.hasSize(0)));
        result.andExpect(jsonPath("$.metaData.size()", is(0)));
    }
    
    @Test
    void testCreateContentTypeAttribute() throws Exception {
        String typeCode = "TX5";
        try {
            ContentTypeDto contentType = this.createContentType(typeCode);
            EntityTypeAttributeFullDto attribute = new EntityTypeAttributeFullDto();
            attribute.setCode("MyAttribute");
            attribute.setType("Text");
            attribute.setName("My test attribute");
            mockMvc.perform(
                    post("/plugins/cms/contentTypes/{code}/attributes", contentType.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(jsonMapper.writeValueAsString(attribute))
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    //                .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value(attribute.getName()))
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testGetAttributeFromContentType() throws Exception {
        String typeCode = "TX5";
        try {
            EntityTypeAttributeFullDto contentTypeAttribute = this.createContentTypeAttribute(typeCode);
            mockMvc.perform(
                    get("/plugins/cms/contentTypes/{contentCode}/attributes/{code}",
                            typeCode, contentTypeAttribute.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    //                .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.payload.code").value("MyAttribute"))
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testListAttributesFromContentType() throws Exception {
        String typeCode = "TX5";
        try {
            List<EntityTypeAttributeFullDto> contentTypeAttribute = this.createContentTypeAttributes(typeCode);

            mockMvc.perform(
                    get("/plugins/cms/contentTypes/{contentCode}/attributes", typeCode)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    //                .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.payload.size()").value(2))
                    .andExpect(jsonPath("$.payload[0].code").value("MyAttribute1"))
                    .andExpect(jsonPath("$.payload[1].code").value("MyAttribute2"))
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testUpdateAttributeFromContentType() throws Exception {
        String typeCode = "TX6";
        try {
            EntityTypeAttributeFullDto contentTypeAttribute = createContentTypeAttribute(typeCode);
            contentTypeAttribute.setName("My New Name");
            mockMvc.perform(
                    put("/plugins/cms/contentTypes/{contentCode}/attributes/{code}",
                            typeCode, contentTypeAttribute.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(jsonMapper.writeValueAsString(contentTypeAttribute))
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    //                .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.payload.name").value("My New Name"))
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testDeleteContentTypeAttribute() throws Exception {
        String typeCode = "TX7";
        EntityTypeAttributeFullDto attribute = createContentTypeAttribute(typeCode);
        try {
            mockMvc.perform(
                    get("/plugins/cms/contentTypes/{contentCode}/attributes/{code}",
                            typeCode, attribute.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isOk())
                    .andReturn();
            mockMvc.perform(
                    delete("/plugins/cms/contentTypes/{contentCode}/attributes/{code}",
                            typeCode, attribute.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    //                .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn();
            mockMvc.perform(
                    get("/plugins/cms/contentTypes/{contentCode}/attributes/{code}",
                            typeCode, attribute.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isNotFound())
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testRefreshContentType() throws Exception {
        String typeCode = "TX8";
        try {
            ContentTypeDto contentType = this.createContentType(typeCode);
            mockMvc.perform(
                    post("/plugins/cms/contentTypes/refresh/{contentTypeCode}", contentType.getCode())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isOk())
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testReloadReferences() throws Exception {
        ContentTypeRefreshRequest bodyRequest = new ContentTypeRefreshRequest();
        mockMvc.perform(
                post("/plugins/cms/contentTypesStatus")
                .header("Authorization", "Bearer " + accessToken)
                .content(jsonMapper.writeValueAsString(bodyRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void testExtractStatus() throws Exception {
        mockMvc.perform(
                get("/plugins/cms/contentTypesStatus")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void moveAttributeUp() throws Exception {
        String typeCode = "TX9";
        try {
            List<EntityTypeAttributeFullDto> attributes = createContentTypeAttributes(typeCode);
            String code = attributes.get(1).getCode();
            mockMvc.perform(
                    put("/plugins/cms/contentTypes/{contentTypeCode}/attributes/{attributeCode}/moveUp", typeCode, code)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.payload.attributeCode").value(code))
                    .andExpect(jsonPath("$.payload.movement").value("UP"))
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    public void moveAttributeDown() throws Exception {
        String typeCode = "TXA";
        try {
            List<EntityTypeAttributeFullDto> attributes = createContentTypeAttributes(typeCode);
            String code = attributes.get(0).getCode();
            mockMvc.perform(
                    put("/plugins/cms/contentTypes/{contentTypeCode}/attributes/{attributeCode}/moveDown", typeCode, code)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.payload.attributeCode").value(code))
                    .andExpect(jsonPath("$.payload.movement").value("DOWN"))
                    .andReturn();
        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testGetContentTypeUsage() throws Exception {
        String code = "ART";

        mockMvc.perform(get("/plugins/cms/contentTypes/{code}/usage", code)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.payload.type", CoreMatchers.is(ContentTypeResourceController.COMPONENT_ID)))
                .andExpect(jsonPath("$.payload.code", is(code)))
                .andExpect(jsonPath("$.payload.usage", is(11)))
                .andReturn();
    }


    @Test
    public void askingForUsageCountForNotExistingCodeShouldReturnZero() throws Throwable {

        String code = "NOT_EXISTING";

        this.mockMvcHelper.setAccessToken(this.accessToken);
        this.mockMvcHelper.getMockMvc("/plugins/cms/contentTypes/{code}/usage", null, code)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.type", is(ContentTypeResourceController.COMPONENT_ID)))
                .andExpect(jsonPath("$.payload.code", is(code)))
                .andExpect(jsonPath("$.payload.usage", is(0)));
    }

    @Test
    void testCreateContentTypeAttributeWithMultipleLanguagesName() throws Exception {
        String typeCode = "TX5";
        try {
            ContentTypeDto contentType = this.createContentType(typeCode);
            EntityTypeAttributeFullDto attribute = new EntityTypeAttributeFullDto();
            attribute.setCode("MyAttribute");
            attribute.setType("Text");
            attribute.setName("Attribute Name");
            Map<String, String> names = new HashMap<>();
            names.put("en", "English Name");
            names.put("it", "Italian Name");
            names.put("ka", "Georgiam Name");

            attribute.setNames(names);
            mockMvc.perform(
                    post("/plugins/cms/contentTypes/{code}/attributes", contentType.getCode())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsonMapper.writeValueAsString(attribute))
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value("Attribute Name"))
                    .andExpect(jsonPath("$.payload.names.size()").value(3))
                    .andExpect(jsonPath("$.payload.names.en").value("English Name"))
                    .andExpect(jsonPath("$.payload.names.it").value("Italian Name"))
                    .andExpect(jsonPath("$.payload.names.ka").value("Georgiam Name"))
                    .andReturn();

            names.clear();
            names.put("en", "English Name 2");
            names.put("it", "Italian Name 3");
            names.put("ka", "Georgiam Name 4");
            attribute.setNames(names);

            mockMvc.perform(
                    put("/plugins/cms/contentTypes/{code}/attributes/{attributeCode}", contentType.getCode(), attribute.getCode())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsonMapper.writeValueAsString(attribute))
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value("Attribute Name"))
                    .andExpect(jsonPath("$.payload.names.size()").value(3))
                    .andExpect(jsonPath("$.payload.names.en").value("English Name 2"))
                    .andExpect(jsonPath("$.payload.names.it").value("Italian Name 3"))
                    .andExpect(jsonPath("$.payload.names.ka").value("Georgiam Name 4"))
                    .andReturn();

        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }

    @Test
    void testContentTypeAttributeNamesToNameFallback() throws Exception {
        String typeCode = "TX6";
        try {
            ContentTypeDto contentType = this.createContentType(typeCode);
            EntityTypeAttributeFullDto attribute = new EntityTypeAttributeFullDto();
            attribute.setCode("MyAttribute");
            attribute.setType("Text");
            Map<String, String> names = new HashMap<>();
            names.put("en", "English Name");
            names.put("it", "Italian Name");
            names.put("ka", "Georgiam Name");

            attribute.setNames(names);
            mockMvc.perform(
                    post("/plugins/cms/contentTypes/{code}/attributes", contentType.getCode())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsonMapper.writeValueAsString(attribute))
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value("Italian Name"))
                    .andExpect(jsonPath("$.payload.names.size()").value(3))
                    .andExpect(jsonPath("$.payload.names.en").value("English Name"))
                    .andExpect(jsonPath("$.payload.names.it").value("Italian Name"))
                    .andExpect(jsonPath("$.payload.names.ka").value("Georgiam Name"))
                    .andReturn();

            names.clear();
            names.put("en", "English Name 2");
            names.put("it", "Italian Name 3");
            names.put("ka", "Georgiam Name 4");
            attribute.setNames(names);

            mockMvc.perform(
                    put("/plugins/cms/contentTypes/{code}/attributes/{attributeCode}", contentType.getCode(), attribute.getCode())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsonMapper.writeValueAsString(attribute))
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value("Italian Name 3"))
                    .andExpect(jsonPath("$.payload.names.size()").value(3))
                    .andExpect(jsonPath("$.payload.names.en").value("English Name 2"))
                    .andExpect(jsonPath("$.payload.names.it").value("Italian Name 3"))
                    .andExpect(jsonPath("$.payload.names.ka").value("Georgiam Name 4"))
                    .andReturn();

        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }
    
    @Test
    void testContentTypeAttributeNameToNamesFallback() throws Exception {
        String typeCode = "TX7";
        try {
            ContentTypeDto contentType = this.createContentType(typeCode);
            EntityTypeAttributeFullDto attribute = new EntityTypeAttributeFullDto();
            attribute.setCode("MyAttribute");
            attribute.setType("Text");
            attribute.setName("Italian Name");
            
            mockMvc.perform(
                    post("/plugins/cms/contentTypes/{code}/attributes", contentType.getCode())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsonMapper.writeValueAsString(attribute))
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value("Italian Name"))
                    .andExpect(jsonPath("$.payload.names.size()").value(1))
                    .andExpect(jsonPath("$.payload.names.it").value("Italian Name"))
                    .andReturn();

            attribute.setName("Italian Name 2");

            mockMvc.perform(
                    put("/plugins/cms/contentTypes/{code}/attributes/{attributeCode}", contentType.getCode(), attribute.getCode())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsonMapper.writeValueAsString(attribute))
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value("Italian Name 2"))
                    .andExpect(jsonPath("$.payload.names.size()").value(1))
                    .andExpect(jsonPath("$.payload.names.it").value("Italian Name 2"))
                    .andReturn();

            attribute.setName(null);

            Map<String, String> names = new HashMap<>();
            names.put("it", "Italian Name 3");
            attribute.setNames(names);

            mockMvc.perform(
                    put("/plugins/cms/contentTypes/{code}/attributes/{attributeCode}", contentType.getCode(), attribute.getCode())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(jsonMapper.writeValueAsString(attribute))
                            .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.metaData.contentTypeCode").value(contentType.getCode()))
                    .andExpect(jsonPath("$.payload.code").value(attribute.getCode()))
                    .andExpect(jsonPath("$.payload.type").value(attribute.getType()))
                    .andExpect(jsonPath("$.payload.name").value("Italian Name 3"))
                    .andExpect(jsonPath("$.payload.names.size()").value(1))
                    .andExpect(jsonPath("$.payload.names.it").value("Italian Name 3"))
                    .andReturn();

        } finally {
            if (null != this.contentManager.getEntityPrototype(typeCode)) {
                ((IEntityTypesConfigurer) this.contentManager).removeEntityPrototype(typeCode);
            }
            waitNotifyingThread();
            Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        }
    }
    
    @Test
    void testComponentExistenceAnalysis() throws Exception {
        // should return DIFF for existing component
        AnalysisControllerDiffAnalysisEngineTestsStubs.testComponentCmsAnalysisResult(
                AnalysisControllerDiffAnalysisEngineTestsStubs.COMPONENT_CONTENT_TYPES,
                "ART",
                AnalysisControllerDiffAnalysisEngineTestsStubs.STATUS_DIFF,
                new ContextOfControllerTests(mockMvc, jsonMapper)
        );

        // should return NEW for NON existing component
        AnalysisControllerDiffAnalysisEngineTestsStubs.testComponentCmsAnalysisResult(
                AnalysisControllerDiffAnalysisEngineTestsStubs.COMPONENT_CONTENT_TYPES,
                "AN_NONEXISTENT_CODE",
                AnalysisControllerDiffAnalysisEngineTestsStubs.STATUS_NEW,
                new ContextOfControllerTests(mockMvc, jsonMapper)
        );
    }

    private ResultActions executeContentTypePost(String fileName, Map<String, String> placeholders, String accessToken, ResultMatcher expected) throws Exception {
        InputStream file = this.getClass().getResourceAsStream(fileName);
        String body = FileTextReader.getText(file);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            body = body.replace(entry.getKey(), entry.getValue());
        }

        ResultActions result = mockMvc
                .perform(post("/plugins/cms/contentTypes")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", "Bearer " + accessToken));
        result.andDo(print()).andExpect(expected);
        return result;
    }
    
    private ContentTypeDto createContentType(String typeCode) throws Exception {
        Assertions.assertNull(this.contentManager.getEntityPrototype(typeCode));
        Content content = new Content();
        content.setTypeCode(typeCode);
        content.setTypeDescription("My content type " + typeCode);
        content.setDefaultModel("My Model");
        content.setListModel("Model list");
        ContentTypeDtoRequest contentTypeRequest = new ContentTypeDtoRequest(content);
        contentTypeRequest.setName("Content request");
        MvcResult mvcResult = mockMvc.perform(
                post("/plugins/cms/contentTypes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsString(contentTypeRequest))
                .accept(MediaType.APPLICATION_JSON_UTF8)).andReturn();
        Assertions.assertNotNull(this.contentManager.getEntityPrototype(typeCode));
        return stringToContentTypeDto(mvcResult);
    }

    private EntityTypeAttributeFullDto createContentTypeAttribute(String typeCode) throws Exception {
        ContentTypeDto contentType = createContentType(typeCode);
        return postForAttribute(contentType, "MyAttribute");
    }

    private List<EntityTypeAttributeFullDto> createContentTypeAttributes(String typeCode) throws Exception {
        ContentTypeDto contentType = createContentType(typeCode);
        return ImmutableList.of(
                postForAttribute(contentType, "MyAttribute1"),
                postForAttribute(contentType, "MyAttribute2")
        );
    }

    private EntityTypeAttributeFullDto postForAttribute(ContentTypeDto contentType, String code) throws Exception {
        EntityTypeAttributeFullDto attribute = new EntityTypeAttributeFullDto();
        attribute.setCode(code);
        attribute.setType("Text");
        MvcResult mvcResult = mockMvc.perform(
                post("/plugins/cms/contentTypes/{contentCode}/attributes", contentType.getCode())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsString(attribute))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        return stringToEntityTypeDto(mvcResult);
    }

    private ContentTypeDto stringToContentTypeDto(MvcResult mvcResult) throws IOException {
        RestResponse<Map<String, String>, Map> restResponse
                = jsonMapper.readerFor(RestResponse.class)
                .readValue(mvcResult.getResponse().getContentAsString());
        Map<String, String> payload = restResponse.getPayload();
        return new ContentTypeDto()
                .name(payload.get("name"))
                .code(payload.get("code"));
    }

    private EntityTypeAttributeFullDto stringToEntityTypeDto(MvcResult mvcResult) throws IOException {
        RestResponse<Map<String, String>, Map> restResponse
                = jsonMapper.readerFor(RestResponse.class)
                .readValue(mvcResult.getResponse().getContentAsString());
        Map<String, String> payload = restResponse.getPayload();
        EntityTypeAttributeFullDto result = new EntityTypeAttributeFullDto();
        result.setCode(payload.get("code"));
        result.setType(payload.get("type"));
        return result;
    }

    private void deleteContentType(String code) throws Exception {
        mockMvc.perform(
                delete("/plugins/cms/contentTypes/{code}", code)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.size()", is(0)))
                .andReturn();
    }

    protected void waitNotifyingThread() throws InterruptedException {
        synchronized (this) {
            this.wait(1000);
        }
        waitThreads(NotifyManager.NOTIFYING_THREAD_NAME);
    }

    protected void waitThreads(String threadNamePrefix) throws InterruptedException {
        Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);
        for (int i = 0; i < threads.length; i++) {
            Thread currentThread = threads[i];
            if (currentThread != null
                    && currentThread.getName().startsWith(threadNamePrefix)) {
                currentThread.join();
            }
        }
    }

}
