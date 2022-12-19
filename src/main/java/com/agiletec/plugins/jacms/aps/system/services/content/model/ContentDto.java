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
package com.agiletec.plugins.jacms.aps.system.services.content.model;

import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.aps.system.services.category.Category;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.AbstractResourceAttribute;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.LinkAttribute;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentRestriction;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.AttachResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ImageResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.entando.entando.aps.system.services.entity.model.EntityAttributeDto;
import org.entando.entando.aps.system.services.entity.model.EntityDto;
import org.entando.entando.web.common.json.JsonDateDeserializer;
import org.entando.entando.web.common.json.JsonDateSerializer;
import org.entando.entando.web.entity.validator.EntityValidator;
import org.springframework.validation.BindingResult;

public class ContentDto extends EntityDto implements Serializable {

    private String status;
    private boolean onLine;
    private String viewPage;
    private String listModel;
    private String defaultModel;

    private Date created;
    private Date lastModified;

    private String version;
    private String firstEditor;
    private String lastEditor;
    private String restriction;
    private String html;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> categories;

    /**
     * The references grouped by service name.
     * <p>
     * Lists all the managers that may contain references by indicating with
     * <code>true</code> the presence of references
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Boolean> references;

    public ContentDto() {
        super();
    }

    public ContentDto(Content src) {
        super(src);
        this.setStatus(src.getStatus());
        this.setOnLine(src.isOnLine());
        this.setViewPage(src.getViewPage());
        this.setListModel(src.getListModel());
        this.setDefaultModel(src.getDefaultModel());
        this.setCreated(src.getCreated());
        this.setLastModified(src.getLastModified());
        this.setVersion(src.getVersion());
        this.setFirstEditor(src.getFirstEditor());
        this.setLastEditor(src.getLastEditor());
        this.setRestriction(src.getRestriction());
        if (null != src.getCategories()) {
            this.setCategories(src.getCategories().stream().map(Category::getCode).collect(Collectors.toList()));
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

    public String getViewPage() {
        return viewPage;
    }

    public void setViewPage(String viewPage) {
        this.viewPage = viewPage;
    }

    public String getListModel() {
        return listModel;
    }

    public void setListModel(String listModel) {
        this.listModel = listModel;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFirstEditor() {
        return firstEditor;
    }

    public void setFirstEditor(String firstEditor) {
        this.firstEditor = firstEditor;
    }

    public String getLastEditor() {
        return lastEditor;
    }

    public void setLastEditor(String lastEditor) {
        this.lastEditor = lastEditor;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Map<String, Boolean> getReferences() {
        return references;
    }

    public void setReferences(Map<String, Boolean> references) {
        this.references = references;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void fillEntity(IApsEntity prototype, ICategoryManager categoryManager, BindingResult bindingResult) {
        Content content = (Content) prototype;
        super.fillEntity(prototype, bindingResult);
        if (null != this.getCategories()) {
            content.getCategories().clear();
            this.getCategories().stream().forEach(i -> {
                Category category = categoryManager.getCategory(i);
                if (null != category) {
                    content.addCategory(category);
                }
            });
        }
        content.setFirstEditor(getFirstEditor() == null ? content.getFirstEditor() : getFirstEditor());
        content.setLastEditor(getLastEditor());
        content.setRestriction(ContentRestriction.getRestrictionValue(getMainGroup()));
        content.setStatus(getStatus() == null ? content.getStatus() : getStatus());
    }

    @Override
    protected void clearAttribute(AttributeInterface attribute) {
        clearAbstractResourceAttribute(attribute);
        clearLinkAttribute(attribute);
        super.clearAttribute(attribute);
    }

    @Override
    protected void fillAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto,
            BindingResult bindingResult) {
        super.fillAttribute(attribute, attributeDto, bindingResult);
        fillAbstractResourceAttribute(attribute, attributeDto);
        fillLinkAttribute(attribute, attributeDto);
    }

    @Override
    protected void rejectAttributeNotFound(BindingResult bindingResult, EntityAttributeDto attributeDto) {
        bindingResult.reject(EntityValidator.ERRCODE_ATTRIBUTE_INVALID, new String[]{attributeDto.getCode()},
                "content.attribute.code.invalid");
    }

    private void clearAbstractResourceAttribute(AttributeInterface attribute) {
        if (AbstractResourceAttribute.class.isAssignableFrom(attribute.getClass())) {
            AbstractResourceAttribute resourceAttribute = (AbstractResourceAttribute) attribute;
            resourceAttribute.getTextMap().clear();
            resourceAttribute.getResources().clear();
            resourceAttribute.getMetadatas().clear();
        }
    }

    private void clearLinkAttribute(AttributeInterface attribute) {
        if (LinkAttribute.class.isAssignableFrom(attribute.getClass())) {
            LinkAttribute linkAttribute = (LinkAttribute) attribute;
            linkAttribute.getTextMap().clear();
            linkAttribute.setSymbolicLink(null);
        }
    }

    private void fillAbstractResourceAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto) {
        if (AbstractResourceAttribute.class.isAssignableFrom(attribute.getClass())) {
            AbstractResourceAttribute resourceAttribute = (AbstractResourceAttribute) attribute;
            for (Entry<String, Object> resourceEntry : attributeDto.getValues().entrySet()) {
                Map<String, Object> resourceMap = (Map<String, Object>) resourceEntry.getValue();
                this.setResourceAttribute(resourceAttribute, resourceMap, resourceEntry.getKey());
            }
        }
    }

    private void setResourceAttribute(AbstractResourceAttribute resourceAttribute, Map<String, Object> resource,
            String langCode) {
        String correlationCode = (String) resource.get("correlationCode");
        String resourceId = (String) resource.get("id");
        String name = (String) resource.get("name");
        if (name != null) {
            resourceAttribute.setText(name, langCode);
        }
        ResourceInterface resourceInterface = new AttachResource();
        resourceInterface.setId(resourceId);
        resourceInterface.setCorrelationCode(correlationCode);
        resourceAttribute.setResource(resourceInterface, langCode);
        Map<String, Object> values = (Map<String, Object>) resource.get("metadata");
        if (values != null) {
            Map<String, String> metadata = values.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> (String) e.getValue()));
            resourceAttribute.setMetadataMap(langCode, metadata);
        }
    }

    private Map<String, String> getAdditionalLinkAttributes(final EntityAttributeDto attributeDto) {
        final Map<String, String> linkProperties = new HashMap<>();
        final String rel = (String) ((Map) attributeDto.getValue()).get("rel");
        if (rel != null) {
            linkProperties.put("rel", rel);
        }
        final String target = (String) ((Map) attributeDto.getValue()).get("target");
        if (target != null) {
            linkProperties.put("target", target);
        }
        final String hreflang = (String) ((Map) attributeDto.getValue()).get("hreflang");
        if (hreflang != null) {
            linkProperties.put("hreflang", hreflang);
        }
        return linkProperties;
    }

    private void fillLinkAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto) {
        if (LinkAttribute.class.isAssignableFrom(attribute.getClass())) {
            LinkAttribute linkAttribute = (LinkAttribute) attribute;
            if (attributeDto.getValue() != null && attributeDto.getValue() instanceof SymbolicLink) {
                linkAttribute.setSymbolicLink((SymbolicLink) attributeDto.getValue());
            } else {
                SymbolicLink link = new SymbolicLink();
                Map<String, String> additionalLinkAttributes = processLinkAttribute(attributeDto, link);
                linkAttribute.setSymbolicLink(link);
                if (!additionalLinkAttributes.isEmpty()) {
                    linkAttribute.setLinkProperties(additionalLinkAttributes);
                }
            }
        }
    }

    private Map<String, String> processLinkAttribute(EntityAttributeDto attributeDto, SymbolicLink link) {
        Map<String, String> result = new HashMap<>();
        if (attributeDto.getValue() != null && Map.class.isAssignableFrom(attributeDto.getValue().getClass())) {
            Object destType = ((Map) attributeDto.getValue()).get("destType");
            if (destType != null) {
                switch ((Integer) destType) {
                    case SymbolicLink.URL_TYPE:
                        link.setDestinationToUrl((String) ((Map) attributeDto.getValue()).get("urlDest"));
                        result = getAdditionalLinkAttributes(attributeDto);
                        break;
                    case SymbolicLink.PAGE_TYPE:
                        link.setDestinationToPage(
                                (String) ((Map) attributeDto.getValue()).get("pageDest"));
                        result = getAdditionalLinkAttributes(attributeDto);
                        break;
                    case SymbolicLink.RESOURCE_TYPE:
                        link.setDestinationToResource(
                                (String) ((Map) attributeDto.getValue()).get("resourceDest"));
                        break;
                    case SymbolicLink.CONTENT_TYPE:
                        link.setDestinationToContent(
                                (String) ((Map) attributeDto.getValue()).get("contentDest"));
                        result = getAdditionalLinkAttributes(attributeDto);
                        break;
                    case SymbolicLink.CONTENT_ON_PAGE_TYPE:
                        link.setDestinationToContentOnPage(
                                (String) ((Map) attributeDto.getValue()).get("contentDest"),
                                (String) ((Map) attributeDto.getValue()).get("pageDest"));
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }

}
