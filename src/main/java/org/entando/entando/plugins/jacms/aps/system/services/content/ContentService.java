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
package org.entando.entando.plugins.jacms.aps.system.services.content;

import static org.entando.entando.plugins.jacms.web.content.ContentController.ERRCODE_CONTENT_NOT_FOUND;
import static org.entando.entando.plugins.jacms.web.content.ContentController.ERRCODE_CONTENT_REFERENCES;

import com.agiletec.aps.system.common.IManager;
import com.agiletec.aps.system.common.entity.IEntityManager;
import com.agiletec.aps.system.common.entity.model.EntitySearchFilter;
import com.agiletec.aps.system.common.entity.model.attribute.AbstractComplexAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.aps.system.common.entity.model.attribute.BooleanAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.CheckBoxAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.CompositeAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.ListAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.MonoListAttribute;
import com.agiletec.aps.system.common.model.dao.SearcherDaoPaginatedResult;
import com.agiletec.aps.system.services.authorization.IAuthorizationManager;
import com.agiletec.aps.system.services.category.CategoryUtilizer;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.group.GroupUtilizer;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.PageUtilizer;
import com.agiletec.aps.system.services.role.Permission;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.ContentUtilizer;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.IContentAuthorizationHelper;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.PublicContentAuthorizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentDto;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SymbolicLink;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.AbstractResourceAttribute;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.LinkAttribute;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentRestriction;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentTypeDto;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.ContentRenderizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.IContentDispenser;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import com.agiletec.plugins.jacms.aps.system.services.searchengine.ICmsSearchEngineManager;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.entando.entando.aps.system.exception.ResourceNotFoundException;
import org.entando.entando.aps.system.exception.RestServerError;
import org.entando.entando.aps.system.services.DtoBuilder;
import org.entando.entando.aps.system.services.IDtoBuilder;
import org.entando.entando.aps.system.services.category.CategoryServiceUtilizer;
import org.entando.entando.aps.system.services.entity.AbstractEntityService;
import org.entando.entando.aps.system.services.entity.model.EntityAttributeDto;
import org.entando.entando.aps.system.services.entity.model.EntityDto;
import org.entando.entando.aps.system.services.group.GroupServiceUtilizer;
import org.entando.entando.aps.system.services.page.PageServiceUtilizer;
import org.entando.entando.aps.util.GenericResourceUtils;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.plugins.jacms.aps.system.services.ContentTypeService;
import org.entando.entando.plugins.jacms.aps.system.services.content.model.ContentsStatusDto;
import org.entando.entando.plugins.jacms.aps.system.services.resource.ResourcesService;
import org.entando.entando.plugins.jacms.web.content.ContentController;
import org.entando.entando.plugins.jacms.web.content.validator.RestContentListRequest;
import org.entando.entando.plugins.jacms.web.resource.model.AssetDto;
import org.entando.entando.plugins.jacms.web.resource.model.ImageAssetDto;
import org.entando.entando.web.common.exceptions.ResourcePermissionsException;
import org.entando.entando.web.common.exceptions.ValidationGenericException;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.common.model.RestListRequest;
import org.entando.entando.web.entity.validator.EntityValidator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

public class ContentService extends AbstractEntityService<Content, ContentDto>
        implements IContentService,
        GroupServiceUtilizer<ContentDto>, CategoryServiceUtilizer<ContentDto>,
        PageServiceUtilizer<ContentDto>, ContentServiceUtilizer<ContentDto>,
        ApplicationContextAware {

    private final EntLogger logger = EntLogFactory.getSanitizedLogger(getClass());

    private ICategoryManager categoryManager;
    private IContentManager contentManager;
    private IContentModelManager contentModelManager;
    private IAuthorizationManager authorizationManager;
    private IContentAuthorizationHelper contentAuthorizationHelper;
    private IContentDispenser contentDispenser;
    private ICmsSearchEngineManager searchEngineManager;
    private ApplicationContext applicationContext;

    @Autowired
    private ResourcesService resourcesService;

    @Autowired
    private ContentTypeService contentTypeService;

    public ICategoryManager getCategoryManager() {
        return categoryManager;
    }

    public void setCategoryManager(ICategoryManager categoryManager) {
        this.categoryManager = categoryManager;
    }

    protected IContentManager getContentManager() {
        return contentManager;
    }

    public void setContentManager(IContentManager contentManager) {
        this.contentManager = contentManager;
    }

    protected IContentModelManager getContentModelManager() {
        return contentModelManager;
    }

    public void setContentModelManager(IContentModelManager contentModelManager) {
        this.contentModelManager = contentModelManager;
    }

    public IAuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    public void setAuthorizationManager(IAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    protected IContentAuthorizationHelper getContentAuthorizationHelper() {
        return contentAuthorizationHelper;
    }

    public void setContentAuthorizationHelper(IContentAuthorizationHelper contentAuthorizationHelper) {
        this.contentAuthorizationHelper = contentAuthorizationHelper;
    }

    protected IContentDispenser getContentDispenser() {
        return contentDispenser;
    }

    public void setContentDispenser(IContentDispenser contentDispenser) {
        this.contentDispenser = contentDispenser;
    }

    protected ICmsSearchEngineManager getSearchEngineManager() {
        return searchEngineManager;
    }
    
    public void setSearchEngineManager(ICmsSearchEngineManager searchEngineManager) {
        this.searchEngineManager = searchEngineManager;
    }

    public IDtoBuilder<Content, ContentDto> getDtoBuilder() {
        return new DtoBuilder<Content, ContentDto>() {
            @Override
            protected ContentDto toDto(Content src) {
                ContentDto contentDto = new ContentDto(src);
                fillAttributes(src.getAttributeList(), contentDto.getAttributes());
                return contentDto;
            }
        };
    }

    private void fillAttributes(List<AttributeInterface> srcAttrs, List<EntityAttributeDto> attrs) {
        for (EntityAttributeDto attr : attrs) {
            for (AttributeInterface srcAttr : srcAttrs) {
                if (srcAttr.getName().equals(attr.getCode())) {
                    fillAttribute(srcAttr, attr);
                    break;
                }
            }
        }
    }

    private void fillComplexAttributes(List<AttributeInterface> srcAttrs, List<EntityAttributeDto> attrs) {
        for (int i = 0; i < attrs.size(); i++) {
            if (srcAttrs.get(i).getName().equals(attrs.get(i).getCode())) {
                fillAttribute(srcAttrs.get(i), attrs.get(i));
            }
        }
    }

    private void fillAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto) {
        fillResourceAttribute(attribute, attributeDto);
        fillCompositeAttribute(attribute, attributeDto);
        fillListAttribute(attribute, attributeDto);
        fillMonolistAttribute(attribute, attributeDto);
        fillBooleanAttribute(attribute, attributeDto);
        fillLinkAttributes(attribute, attributeDto);
    }

    private void fillLinkAttributes(final AttributeInterface attribute, final EntityAttributeDto attributeDto) {
        if (attributeDto.getElements() != null && (LinkAttribute.class.isAssignableFrom(attribute.getClass()))) {
            ((LinkAttribute) attribute).setSymbolicLink((SymbolicLink) attribute.getValue());
            ((LinkAttribute) attribute).setLinkProperties(((LinkAttribute) attribute).getLinkProperties());
            final SymbolicLink symbolicLink = ((LinkAttribute) attribute).getSymbolicLink();
            if (symbolicLink != null) {
                final Map<String, String> linkPoperties = ((LinkAttribute) attribute).getLinkProperties();
                final String contentDest = symbolicLink.getContentDest();
                final String pageDest = symbolicLink.getPageDest();
                final String resourceDest = symbolicLink.getResourceDest();
                final String symbolicDestination = symbolicLink.getSymbolicDestination();
                Map<String, Object> result = new HashMap<>();
                result.put("contentDest", contentDest);
                result.put("pageDest", pageDest);
                result.put("resourceDest", resourceDest);
                result.put("symbolicDestination", symbolicDestination);
                result.put("destType", symbolicLink.getDestType());
                result.put("urlDest", symbolicLink.getUrlDest());
                result.putAll(linkPoperties);
                attributeDto.setValue(result);
            }
        }
    }

    private void fillBooleanAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto) {
        if (attributeDto.getElements() != null && (CheckBoxAttribute.class.isAssignableFrom(attribute.getClass()))) {
            attributeDto.setValue(((CheckBoxAttribute) attribute).getValue());
        } else if (attributeDto.getElements() != null && (BooleanAttribute.class
                .isAssignableFrom(attribute.getClass()))) {
            attributeDto.setValue(((BooleanAttribute) attribute).getBooleanValue());
        }
    }

    private void fillResourceAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto) {
        if (attributeDto.getValues() != null && AbstractResourceAttribute.class
                .isAssignableFrom(attribute.getClass())) {
            convertResourceAttributeToDto((AbstractResourceAttribute) attribute, attributeDto);
        }
    }

    private void fillCompositeAttribute(AttributeInterface attribute, EntityAttributeDto attr) {
        if (attr.getCompositeElements() != null && CompositeAttribute.class.isAssignableFrom(attribute.getClass())) {
            CompositeAttribute compAttr = (CompositeAttribute) attribute;
            fillAttributes(compAttr.getAttributes(), attr.getCompositeElements());
        }
    }

    private void fillListAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto) {
        if (attributeDto.getElements() != null && (ListAttribute.class.isAssignableFrom(attribute.getClass()))) {
            ListAttribute listAttribute = (ListAttribute) attribute;
            for (Entry<String, List<EntityAttributeDto>> entry : attributeDto.getListElements().entrySet()) {
                int index = 0;
                for (AttributeInterface element : listAttribute.getAttributeList(entry.getKey())) {
                    fillAttribute(element, entry.getValue().get(index));
                    index++;
                }
            }
        }
    }

    private void fillMonolistAttribute(AttributeInterface attribute, EntityAttributeDto attributeDto) {
        if (attributeDto.getElements() != null && (MonoListAttribute.class.isAssignableFrom(attribute.getClass()))) {
            MonoListAttribute monolistAttribute = (MonoListAttribute) attribute;
            fillComplexAttributes(monolistAttribute.getAttributes(), attributeDto.getElements());
        }
    }

    private void convertResourceAttributeToDto(AbstractResourceAttribute contentAttr, EntityAttributeDto attr) {
        attr.setValues(
                attr.getValues().entrySet().stream()
                        .filter(e -> e.getValue() != null)
                        .map(e -> {
                            AssetDto assetDto = resourcesService.convertResourceToDto((ResourceInterface) e.getValue());
                            if (contentAttr.getMetadatas() != null && ImageAssetDto.class
                                    .isAssignableFrom(assetDto.getClass())) {
                                ((ImageAssetDto) assetDto).setMetadata(contentAttr.getMetadatas().get(e.getKey()));
                            }
                            assetDto.setName(contentAttr.getTextForLang(e.getKey()));

                            return new AbstractMap.SimpleEntry<>(e.getKey(), assetDto);
                        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public String getManagerName() {
        return ((IManager) this.getContentManager()).getName();
    }

    @Override
    protected void fillEntity(EntityDto request, Content entity, BindingResult bindingResult) {
        ContentDto contentDto = (ContentDto) request;
        contentDto.fillEntity(entity, this.getCategoryManager(), bindingResult);
    }

    @Override
    public List<ContentDto> getGroupUtilizer(String groupCode) {
        try {
            List<String> contentIds = ((GroupUtilizer<String>) this.getContentManager()).getGroupUtilizers(groupCode);
            return this.buildDtoList(contentIds);
        } catch (EntException ex) {
            logger.error("Error loading content references for group {}", groupCode, ex);
            throw new RestServerError("Error loading content references for group", ex);
        }
    }

    @Override
    public List<ContentDto> getCategoryUtilizer(String categoryCode) {
        try {
            List<String> contentIds = ((CategoryUtilizer) this.getContentManager()).getCategoryUtilizers(categoryCode);
            return this.buildDtoList(contentIds);
        } catch (EntException ex) {
            logger.error("Error loading content references for category {}", categoryCode, ex);
            throw new RestServerError("Error loading content references for category", ex);
        }
    }

    @Override
    public List<ContentDto> getPageUtilizer(String pageCode) {
        try {
            List<String> contentIds = ((PageUtilizer) this.getContentManager()).getPageUtilizers(pageCode);
            return this.buildDtoList(contentIds);
        } catch (EntException ex) {
            logger.error("Error loading content references for page {}", pageCode, ex);
            throw new RestServerError("Error loading content references for page", ex);
        }
    }

    @Override
    public List<ContentDto> getContentUtilizer(String contentId) {
        try {
            List<String> contentIds = ((ContentUtilizer) this.getContentManager()).getContentUtilizers(contentId);
            return this.buildDtoList(contentIds);
        } catch (EntException ex) {
            logger.error("Error loading content references for content {}", contentId, ex);
            throw new RestServerError("Error loading content references for content", ex);
        }
    }

    private List<ContentDto> buildDtoList(List<String> contentIds) {
        List<ContentDto> dtoList = new ArrayList<>();
        if (null != contentIds) {
            contentIds.stream().forEach(i -> {
                try {
                    Content content = this.getContentManager().loadContent(i, true);
                    if (content != null) {
                        dtoList.add(this.getDtoBuilder().convert(content));
                    }
                } catch (EntException e) {
                    logger.error("error loading content {}", i, e);
                }
            });
        }
        return dtoList;
    }

    @Override
    protected ContentDto buildEntityDto(Content entity) {
        return this.getDtoBuilder().convert(entity);
    }

    @Override
    public PagedMetadata<ContentDto> getContents(RestContentListRequest request, UserDetails user) {
        try {
            List<String> contentIds = getContentIds(request, user);
            List<ContentDto> result = toContentDto(request, user, contentIds);
            return toPagedMetadata(request, contentIds, result);
        } catch (ResourceNotFoundException | ValidationGenericException e) {
            throw e;
        } catch (Exception t) {
            logger.error("error in search contents", t);
            throw new RestServerError("error in search contents", t);
        }
    }

    private List<String> getContentIds(RestContentListRequest request, UserDetails user) throws EntException {
        boolean online = isStatusOnline(request.getStatus());
        EntitySearchFilter[] filters = getEntitySearchFilters(request);
        List<String> allowedGroups = this.getAllowedGroups(user, online);
        List<String> result = online
                ? this.getContentManager()
                        .loadPublicContentsId(request.getCategories(), request.isOrClauseCategoryFilter(),
                                filters, allowedGroups)
                : this.getContentManager()
                        .loadWorkContentsId(request.getCategories(), request.isOrClauseCategoryFilter(),
                                filters, allowedGroups);
        if (!StringUtils.isBlank(request.getText()) && online) {
            String langCode
                    = (StringUtils.isBlank(request.getLang())) ? this.getLangManager().getDefaultLang().getCode()
                    : request.getLang();
            List<String> fullTextResult = this.getSearchEngineManager()
                    .searchEntityId(langCode, request.getText(), allowedGroups);
            result.removeIf(i -> !fullTextResult.contains(i));
        }
        return result;
    }

    private EntitySearchFilter[] getEntitySearchFilters(RestContentListRequest request) {
        List<EntitySearchFilter> filters = request.buildEntitySearchFilters();
        EntitySearchFilter[] filtersArr = new EntitySearchFilter[filters.size()];
        filtersArr = filters.toArray(filtersArr);
        return filtersArr;
    }

    private boolean isStatusOnline(String status) {
        return IContentService.STATUS_ONLINE.equalsIgnoreCase(status);
    }

    private boolean isModeFull(String mode) {
        return IContentService.MODE_FULL.equalsIgnoreCase(mode);
    }

    private List<ContentDto> toContentDto(RestContentListRequest request, UserDetails user, List<String> contentIds) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "content");
        boolean full = isModeFull(request.getMode());
        List<ContentDto> masterList = new ArrayList<>();
        for (String contentId : request.getSublist(contentIds)) {
            ContentDto dto = full ? buildFullContentDto(user, bindingResult, contentId,
                    isStatusOnline(request.getStatus()),
                    request.getModel(), request.getLang(), request.isResolveLink()) : buildLightContentDto(contentId);

            boolean compatible = isCompatibleWithLinkabilityFilter(dto, request);

            if (compatible) {
                masterList.add(dto);
            }
        }
        return masterList;
    }

    private ContentDto buildFullContentDto(UserDetails user, BeanPropertyBindingResult bindingResult, String contentId,
            boolean online, String modelId, String lang, boolean resolveLink) {
        ContentDto dto = null;
        try {
            Content content = this.getContentManager().loadContent(contentId, online);
            if (null == content) {
                throw new ResourceNotFoundException(EntityValidator.ERRCODE_ENTITY_DOES_NOT_EXIST, "Content", contentId);
            }
            dto = this.buildEntityDto(content);
        } catch (ResourceNotFoundException rnf) {
            throw rnf;
        } catch (Exception e) {
            logger.error("Error extracting content", e);
            throw new RestServerError("error extracting content", e);
        }
        dto.setHtml(this.extractRenderedContent(dto, modelId, lang, resolveLink, user, bindingResult));
        dto.setReferences(this.getReferencesInfo(dto.getId()));
        return dto;
    }

    private ContentDto buildLightContentDto(String contentId) {
        ContentDto dto = null;
        try {
            ContentRecordVO contentVo = this.getContentManager().loadContentVO(contentId);
            if (null == contentVo) {
                throw new ResourceNotFoundException(EntityValidator.ERRCODE_ENTITY_DOES_NOT_EXIST, "Content", contentId);
            }
            dto = new ContentDto(toContent(contentVo));
        } catch (ResourceNotFoundException rnf) {
            throw rnf;
        } catch (Exception e) {
            logger.error("Error extracting content", e);
            throw new RestServerError("error extracting content", e);
        }
        return dto;
    }

    private Content toContent(ContentRecordVO contentVo) {

        Content result = new Content();
        result.setId(contentVo.getId());
        result.setTypeCode(contentVo.getTypeCode());
        result.setTypeDescription(getTypeDescription(contentVo));
        result.setDescription(contentVo.getDescription());
        result.setStatus(contentVo.getStatus());
        result.setCreated(contentVo.getCreate());
        result.setLastModified(contentVo.getModify());
        result.setPublished(contentVo.getPublish());
        result.setOnLine(contentVo.isOnLine());
        result.setSync(contentVo.isSync());
        result.setMainGroup(contentVo.getMainGroupCode());
        result.setVersion(contentVo.getVersion());
        result.setFirstEditor(contentVo.getFirstEditor());
        result.setLastEditor(contentVo.getLastEditor());
        result.setRestriction(contentVo.getRestriction());

        return result;
    }

    private String getTypeDescription(ContentRecordVO contentVo) {
        Optional<ContentTypeDto> contentTypeDtoOptional = contentTypeService.findOne(contentVo.getTypeCode());
        if (contentTypeDtoOptional.isPresent()) {
            return contentTypeDtoOptional.get().getName();
        }
        return null;
    }

    private PagedMetadata<ContentDto> toPagedMetadata(RestContentListRequest request,
            List<String> contentIds, List<ContentDto> result) {
        PagedMetadata<ContentDto> pagedMetadata = new PagedMetadata<>(request, contentIds.size());
        pagedMetadata.setBody(result);
        return pagedMetadata;
    }

    private boolean isCompatibleWithLinkabilityFilter(ContentDto dto, RestContentListRequest requestList) {
        if (requestList.getForLinkingWithOwnerGroup() == null) {
            return true;
        }

        return GenericResourceUtils.isResourceLinkableByContent(
                dto.getMainGroup(),
                dto.getGroups(),
                requestList.getForLinkingWithOwnerGroup(),
                Optional.ofNullable(requestList.getForLinkingWithExtraGroups())
                        .orElse(null)
        );
    }

    @Override
    public Integer countContentsByType(String contentType) {
        try {
            EntitySearchFilter[] filters = new EntitySearchFilter[]{
                new EntitySearchFilter("typeCode", false, contentType, false)
            };

            List<String> userGroupCodes = Collections.singletonList("administrators");

            return getContentManager().countWorkContents(null, false, filters, userGroupCodes);
        } catch (Exception t) {
            logger.error("error in contents count by type", t);
            throw new RestServerError("error in contents count by type", t);
        }
    }

    protected List<String> getAllowedGroups(UserDetails currentUser, boolean requiredOnlineContents) {
        List<String> groupCodes = new ArrayList<>();
        if (null == currentUser) {
            return groupCodes;
        }
        if (requiredOnlineContents) {
            List<Group> groups = this.getAuthorizationManager().getUserGroups(currentUser);
            groupCodes.addAll(groups.stream().map(Group::getName).collect(Collectors.toList()));
        } else {
            List<Group> groupsByPermission = this.getAuthorizationManager()
                    .getGroupsByPermission(currentUser, Permission.CONTENT_EDITOR);
            groupCodes.addAll(groupsByPermission.stream().map(Group::getName).collect(Collectors.toList()));
        }
        return groupCodes;
    }

    @Override
    public ContentDto getContent(String code, String modelId, String status, String langCode, boolean resolveLink,
            UserDetails user) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(code, "content");
        boolean online = isStatusOnline(status);
        this.checkContentAuthorization(user, code, online, false, bindingResult);
        ContentDto dto = buildFullContentDto(user, bindingResult, code, online, modelId, langCode, resolveLink);
        dto.setReferences(this.getReferencesInfo(dto.getId()));
        return dto;
    }

    protected String extractRenderedContent(ContentDto dto, String modelId,
            String langCode, boolean resolveLink, UserDetails user, BindingResult bindingResult) {
        String render = null;
        if (null == modelId || modelId.trim().length() == 0) {
            return null;
        }
        Integer modelIdInteger = this.checkModel(dto, modelId, bindingResult);
        if (null != modelIdInteger) {
            Lang lang = this.getLangManager().getDefaultLang();
            if (!StringUtils.isBlank(langCode)) {
                lang = this.getLangManager().getLang(langCode);
                if (null == lang) {
                    bindingResult.reject(ContentController.ERRCODE_INVALID_LANG_CODE,
                            new String[]{modelId, dto.getTypeCode()}, "plugins.jacms.content.model.invalidLangCode");
                    throw new ValidationGenericException(bindingResult);
                }
            }
            ContentRenderizationInfo renderizationInfo = this.getContentDispenser()
                    .getRenderizationInfo(dto.getId(), modelIdInteger, lang.getCode(), user, true);
            if (null != renderizationInfo) {
                if (resolveLink) {
                    this.getContentDispenser().resolveLinks(renderizationInfo, null);
                    render = renderizationInfo.getRenderedContent();
                } else {
                    render = renderizationInfo.getCachedRenderedContent();
                }
            }
        }
        return render;
    }

    protected Integer checkModel(ContentDto dto, String modelId, BindingResult bindingResult) {
        Integer modelIdInteger = null;
        if (StringUtils.isBlank(modelId)) {
            return null;
        }
        if (modelId.equals(ContentModel.MODEL_ID_DEFAULT)) {
            if (null == dto.getDefaultModel()) {
                bindingResult.reject(ContentController.ERRCODE_INVALID_MODEL,
                        "plugins.jacms.content.model.nullDefaultModel");
                throw new ValidationGenericException(bindingResult);
            }
            modelIdInteger = Integer.parseInt(dto.getDefaultModel());
        } else if (modelId.equals(ContentModel.MODEL_ID_LIST)) {
            if (null == dto.getListModel()) {
                bindingResult
                        .reject(ContentController.ERRCODE_INVALID_MODEL, "plugins.jacms.content.model.nullListModel");
                throw new ValidationGenericException(bindingResult);
            }
            modelIdInteger = Integer.parseInt(dto.getListModel());
        } else {
            modelIdInteger = Integer.parseInt(modelId);
        }
        ContentModel model = this.getContentModelManager().getContentModel(modelIdInteger);
        if (model == null) {
            bindingResult.reject(ContentController.ERRCODE_INVALID_MODEL, new String[]{modelId},
                    "plugins.jacms.content.model.nullModel");
            throw new ValidationGenericException(bindingResult);
        } else if (!dto.getTypeCode().equals(model.getContentType())) {
            bindingResult.reject(ContentController.ERRCODE_INVALID_MODEL,
                    new String[]{modelId, dto.getTypeCode()}, "plugins.jacms.content.model.invalid");
            throw new ValidationGenericException(bindingResult);
        }
        return modelIdInteger;
    }

    @Override
    public ContentDto addContent(ContentDto request, UserDetails user, BindingResult bindingResult) {
        if (!this.getAuthorizationManager().isAuthOnGroup(user, request.getMainGroup())) {
            bindingResult.reject(ContentController.ERRCODE_UNAUTHORIZED_CONTENT, new String[]{request.getMainGroup()},
                    "plugins.jacms.content.group.unauthorized");
            throw new ResourcePermissionsException(bindingResult);
        }
        request.setFirstEditor(user.getUsername());
        request.setLastEditor(user.getUsername());
        request.setRestriction(ContentRestriction.getRestrictionValue(request.getMainGroup()));
        return this.addEntity(JacmsSystemConstants.CONTENT_MANAGER, request, bindingResult);
    }

    @Override
    public ContentDto updateContent(ContentDto request, UserDetails user, BindingResult bindingResult) {
        this.checkContentExists(request.getId());
        this.checkContentAuthorization(user, request.getId(), false, true, bindingResult);
        request.setLastEditor(user.getUsername());
        request.setRestriction(ContentRestriction.getRestrictionValue(request.getMainGroup()));
        return super.updateEntity(JacmsSystemConstants.CONTENT_MANAGER, request, bindingResult);
    }

    @Override
    public void deleteContent(String code, UserDetails user) {
        this.checkContentAuthorization(user, code, false, true, null);
        try {
            Content content = this.getContentManager().loadContent(code, false);
            if (null == content) {
                throw new ResourceNotFoundException(ERRCODE_CONTENT_NOT_FOUND, "content", code);
            }
            Content publicContent = this.getContentManager().loadContent(code, true);
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(code, "content");
            if (null != publicContent) {
                bindingResult.reject(ContentController.ERRCODE_DELETE_PUBLIC_PAGE,
                        new String[]{code}, "plugins.jacms.content.status.published");
                throw new ValidationGenericException(bindingResult);
            }
            this.getContentManager().deleteContent(content);
        } catch (ValidationGenericException e) {
            throw e;
        } catch (EntException e) {
            logger.error("Error deleting content {}", code, e);
            throw new RestServerError("error deleting content", e);
        }
    }

    @Override
    public ContentDto updateContentStatus(String code, String status, UserDetails user) {
        return updateContentStatus(code, status, user, null);
    }

    private ContentDto updateContentStatus(String code, String status, UserDetails user,
            BeanPropertyBindingResult bindingResult) {
        try {
            Content content = this.getContentManager().loadContent(code, false);
            if (bindingResult == null) {
                bindingResult = new BeanPropertyBindingResult(code, "content");
            }
            if (null == content) {
                throw new ResourceNotFoundException(ERRCODE_CONTENT_NOT_FOUND, "content", code);
            }
            if (status.equals(STATUS_DRAFT) && null == this.getContentManager().loadContent(code, true)) {
                return this.getDtoBuilder().convert(content);
            }
            Content newContent = null;
            if (status.equals(STATUS_ONLINE)) {
                //need to check referenced objects
                this.getContentManager().insertOnLineContent(content);
                newContent = this.getContentManager().loadContent(code, true);
            } else if (status.equals(STATUS_DRAFT)) {
                Map<String, ContentServiceUtilizer> beans = applicationContext
                        .getBeansOfType(ContentServiceUtilizer.class);
                if (null != beans) {
                    Iterator<ContentServiceUtilizer> iter = beans.values().iterator();
                    while (iter.hasNext()) {
                        ContentServiceUtilizer serviceUtilizer = iter.next();
                        List utilizer = serviceUtilizer.getContentUtilizer(code);
                        if (null != utilizer && utilizer.size() > 0) {
                            bindingResult
                                    .reject(ContentController.ERRCODE_REFERENCED_ONLINE_CONTENT, new String[]{code},
                                            "plugins.jacms.content.status.invalid.online.ref");
                            throw new ValidationGenericException(bindingResult);
                        }
                    }
                }
                this.getContentManager().removeOnLineContent(content);
                newContent = this.getContentManager().loadContent(code, false);
            }
            return this.getDtoBuilder().convert(newContent);
        } catch (ValidationGenericException | ResourceNotFoundException e) {
            throw e;
        } catch (EntException e) {
            logger.error("Error updating content {} status", code, e);
            throw new RestServerError("error in update page content", e);
        }
    }

    @Override
    public List<ContentDto> updateContentsStatus(List<String> codes, String status, UserDetails user) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(codes, "content");
        List<ContentDto> result = codes.stream()
                .map(code -> {
                    try {
                        return updateContentStatus(code, status, user, bindingResult);
                    } catch (Exception e) {
                        logger.error("Error in updating content(code: {}) status {}", code, e);
                        return null;
                    }
                })
                .filter(content -> content != null)
                .collect(Collectors.toList());
        if (bindingResult.hasErrors()) {
            throw new ValidationGenericException(bindingResult);
        }

        return result;
    }

    @Override
    public ContentDto cloneContent(String code, UserDetails user, BindingResult bindingResult) {
        try {
            boolean online = this.contentManager.loadContentVO(code).isOnLine();
            Content content = this.contentManager.loadContent(code, online);
            if (!this.getAuthorizationManager().isAuthOnGroup(user, content.getMainGroup())) {
                bindingResult.reject(ContentController.ERRCODE_UNAUTHORIZED_CONTENT, new String[]{content.getMainGroup()},
                        "plugins.jacms.content.group.unauthorized");
                throw new ResourcePermissionsException(bindingResult);
            }
            content.setId(null);
            content.setFirstEditor(user.getUsername());
            content.setLastEditor(user.getUsername());
            content.setRestriction(ContentRestriction.getRestrictionValue(content.getMainGroup()));
            String id = this.contentManager.addContent(content);
            content.setId(id);
            return this.buildEntityDto(content);
        } catch (EntException e) {
            throw new RestServerError("Error cloning content: " + code, e);
        }
    }

    @Override
    public PagedMetadata<?> getContentReferences(String code, String managerName, UserDetails user,
            RestListRequest requestList) {
        try {
            Content content = this.getContentManager().loadContent(code, false);
            if (null == content) {
                logger.warn("no content found with code {}", code);
                throw new ResourceNotFoundException(ERRCODE_CONTENT_NOT_FOUND, "content", code);
            }
            ContentServiceUtilizer<?> utilizer = this.getContentServiceUtilizer(managerName);
            if (null == utilizer) {
                logger.warn("no references found for {}", managerName);
                throw new ResourceNotFoundException(ERRCODE_CONTENT_REFERENCES, "reference", managerName);
            }
            List<?> dtoList = utilizer.getContentUtilizer(code);
            List<?> subList = requestList.getSublist(dtoList);
            SearcherDaoPaginatedResult<?> pagedResult = new SearcherDaoPaginatedResult(dtoList.size(), subList);
            PagedMetadata<Object> pagedMetadata = new PagedMetadata<>(requestList, pagedResult);
            pagedMetadata.setBody((List<Object>) subList);
            return pagedMetadata;
        } catch (EntException ex) {
            logger.error("Error extracting content references - content {} - manager {}", code, managerName, ex);
            throw new RestServerError("Error extracting content references", ex);
        }
    }

    private ContentServiceUtilizer<?> getContentServiceUtilizer(String managerName) {
        Map<String, ContentServiceUtilizer> beans = this.applicationContext
                .getBeansOfType(ContentServiceUtilizer.class);
        ContentServiceUtilizer defName = beans.values().stream()
                .filter(service -> service.getManagerName().equals(managerName))
                .findFirst().orElse(null);
        return defName;
    }

    @Override
    protected Content addEntity(IEntityManager entityManager, Content entityToAdd) {
        return this.updateEntity(entityManager, entityToAdd);
    }

    @Override
    protected Content updateEntity(IEntityManager entityManager, Content entityToUpdate) {
        try {
            this.getContentManager().saveContent(entityToUpdate);
            return this.getContentManager().loadContent(entityToUpdate.getId(), false);
        } catch (Exception e) {
            logger.error("Error saving content", e);
            throw new RestServerError("Error saving content", e);
        }
    }

    @Override
    protected void scanEntity(Content currentEntity, BindingResult bindingResult) {
        super.scanEntity(currentEntity, bindingResult);
        for (AttributeInterface attr : currentEntity.getAttributeList()) {
            this.scanResourceElement(currentEntity, bindingResult, attr);
        }
    }
    
    protected void scanResourceElement(Content currentEntity, BindingResult bindingResult, AttributeInterface attr) {
        if (attr.isSimple()) {
            this.scanAbstractResourceAttribute(currentEntity, bindingResult, attr);
        } else {
            List<AttributeInterface> elements = ((AbstractComplexAttribute) attr).getAttributes();
            for (int i = 0; i < elements.size(); i++) {
                AttributeInterface element = elements.get(i);
                this.scanResourceElement(currentEntity, bindingResult, element);
            }
        }
    }
    
    private boolean scanAbstractResourceAttribute(Content currentEntity, BindingResult bindingResult, AttributeInterface attr) {
        if (AbstractResourceAttribute.class.isAssignableFrom(attr.getClass())) {
            AbstractResourceAttribute resAttr = (AbstractResourceAttribute) attr;
            for (ResourceInterface res : resAttr.getResources().values()) {
                String idOrCode = res.getId() == null ? res.getCorrelationCode() : res.getId();
                AssetDto resource;
                try {
                    resource = resourcesService.getAsset(res.getId(), res.getCorrelationCode());
                    res.setId(resource.getId());
                } catch (ResourceNotFoundException e) {
                    logger.error("Resource not found: " + idOrCode);
                    bindingResult.reject(EntityValidator.ERRCODE_ATTRIBUTE_INVALID,
                            "Resource not found - " + idOrCode);
                    return true;
                }
                String resourceMainGroup = resource.getGroup();
                if (!resourceMainGroup.equals(Group.FREE_GROUP_NAME)
                        && !resourceMainGroup.equals(currentEntity.getMainGroup())
                        && !currentEntity.getGroups().contains(resourceMainGroup)) {
                    bindingResult.reject(EntityValidator.ERRCODE_ATTRIBUTE_INVALID,
                            "Invalid resource group - " + resourceMainGroup);
                }
            }
        }
        return false;
    }

    private Map<String, Boolean> getReferencesInfo(String contentId) {
        Map<String, Boolean> references = new HashMap<>();
        Map<String, ContentServiceUtilizer> beans = this.applicationContext
                .getBeansOfType(ContentServiceUtilizer.class);
        beans.values().stream().forEach(service -> {
            List<?> utilizers = service.getContentUtilizer(contentId);
            references.put(service.getManagerName(), (utilizers != null && !utilizers.isEmpty()));
        });
        return references;
    }

    protected void checkContentAuthorization(UserDetails userDetails, String contentId, boolean publicVersion,
            boolean edit, BindingResult mainBindingResult) {
        try {
            PublicContentAuthorizationInfo pcai
                    = (publicVersion) ? this.getContentAuthorizationHelper().getAuthorizationInfo(contentId) : null;
            if (publicVersion && null == pcai) {
                throw new ResourceNotFoundException(ERRCODE_CONTENT_NOT_FOUND, "content", contentId);
            }
            List<String> userGroupCodes = new ArrayList<>();
            List<Group> groups = (null != userDetails) ? this.getAuthorizationManager().getUserGroups(userDetails)
                    : new ArrayList<>();
            userGroupCodes.addAll(groups.stream().map(Group::getName).collect(Collectors.toList()));
            userGroupCodes.add(Group.FREE_GROUP_NAME);
            if (!(publicVersion && !edit && null != pcai && pcai.isUserAllowed(userGroupCodes))
                    && !this.getContentAuthorizationHelper().isAuthToEdit(userDetails, contentId, publicVersion)) {
                BindingResult bindingResult
                        = (null == mainBindingResult) ? new BeanPropertyBindingResult(contentId, "content")
                                : mainBindingResult;
                bindingResult.reject(ContentController.ERRCODE_UNAUTHORIZED_CONTENT, new String[]{contentId},
                        "plugins.jacms.content.unauthorized.access");
                throw new ResourcePermissionsException(bindingResult);
            }
        } catch (ResourceNotFoundException | ResourcePermissionsException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("error checking auth for content {}", contentId, ex);
            throw new RestServerError("error checking auth for content", ex);
        }
    }

    protected void checkContentExists(String code) {
        try {
            if (null == getContentManager().loadContent(code, false)) {
                logger.error("Content not found: " + code);
                throw new ResourceNotFoundException(ERRCODE_CONTENT_NOT_FOUND, "content", code);
            }
        } catch (EntException ex) {
            throw new RestServerError("plugins.jacms.content.contentManager.error.read", null);
        }
    }

    public boolean exists(String code, boolean online) throws EntException {
        return (null != getContentManager().loadContent(code, online));
    }

    @Override
    public boolean exists(String code) throws EntException {
        return exists(code, true) || exists(code, false);
    }

    @Override
    public ContentsStatusDto getContentsStatus() {
        return new ContentsStatusDto(this.getContentManager().getContentsStatus());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
