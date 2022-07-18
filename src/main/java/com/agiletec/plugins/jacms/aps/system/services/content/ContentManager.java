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
package com.agiletec.plugins.jacms.aps.system.services.content;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.entity.ApsEntityManager;
import com.agiletec.aps.system.common.entity.IEntityDAO;
import com.agiletec.aps.system.common.entity.IEntitySearcherDAO;
import com.agiletec.aps.system.common.entity.model.EntitySearchFilter;
import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.SmallEntityType;
import com.agiletec.aps.system.common.model.dao.SearcherDaoPaginatedResult;
import com.agiletec.aps.system.services.category.CategoryUtilizer;
import com.agiletec.aps.system.services.group.GroupUtilizer;
import com.agiletec.aps.system.services.keygenerator.IKeyGeneratorManager;
import com.agiletec.aps.system.services.page.PageUtilizer;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.event.PublicContentChangedEvent;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SmallContentType;
import com.agiletec.plugins.jacms.aps.system.services.resource.ResourceUtilizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entando.entando.aps.system.services.cache.CacheInfoEvict;
import org.entando.entando.aps.system.services.cache.CacheInfoManager;
import org.entando.entando.aps.system.services.cache.ICacheInfoManager;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.ent.exception.EntRuntimeException;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;

/**
 * Contents manager. This implements all the methods needed to create and manage
 * the contents.
 */
public class ContentManager extends ApsEntityManager
                            implements IContentManager, GroupUtilizer<String>, PageUtilizer, ContentUtilizer, ResourceUtilizer, CategoryUtilizer {

    private static final EntLogger logger = EntLogFactory.getSanitizedLogger(ContentManager.class);
    
    private static final String ERROR_WHILE_LOADING_CONTENTS = "Error while loading contents";

    public static final String CONTENT_TYPE_CACHE_PREFIX = "jacms_ContentType_";

    private IContentDAO contentDAO;

    private IContentSearcherDAO workContentSearcherDAO;

    private IContentSearcherDAO publicContentSearcherDAO;

    private IContentUpdaterService contentUpdaterService;
    
    private ICacheInfoManager cacheInfoManager;

    @Override
    protected String getConfigItemName() {
        return JacmsSystemConstants.CONFIG_ITEM_CONTENT_TYPES;
    }

    /**
     * Create a new instance of the requested content. The new content is forked
     * (or cloned) from the corresponding prototype, and it's returned empty.
     *
     * @param typeCode The code of the requested (proto)type, as declared in the
     * configuration.
     * @return The new content.
     */
    @Override
    public Content createContentType(String typeCode) {
        return (Content) super.getEntityPrototype(typeCode);
    }

    /**
     * Return a list of the of the content types in a 'small form'. 'Small form'
     * mans that the contents returned are purged from all unnecessary
     * information (eg. attributes).
     *
     * @return The list of the types in a (small form).
     * @deprecated From Entando 4.1.2, use getSmallEntityTypes() method
     */
    @Override
    public List<SmallContentType> getSmallContentTypes() {
        List<SmallContentType> smallContentTypes = new ArrayList<>(getSmallContentTypesMap().values());
        Collections.sort(smallContentTypes);
        return smallContentTypes;
    }

    /**
     * Return the map of the prototypes of the contents types. Return a map,
     * index by the code of the type, of the prototypes of the available content
     * types.
     *
     * @return The map of the prototypes of the content types in a
     * 'SmallContentType' objects.
     */
    @Override
    public Map<String, SmallContentType> getSmallContentTypesMap() {
        Map<String, SmallContentType> smallContentTypes = new HashMap<>();
        List<SmallEntityType> entityTypes = super.getSmallEntityTypes();
        for (SmallEntityType entityType : entityTypes) {
            SmallContentType sct = new SmallContentType();
            sct.setCode(entityType.getCode());
            sct.setDescription(entityType.getDescription());
            smallContentTypes.put(entityType.getCode(), sct);
        }
        return smallContentTypes;
    }

    /**
     * Return the code of the default page used to display the given content.
     * The default page is defined at content type level; the type is
     * extrapolated from the code built following the conventions.
     *
     * @param contentId The content ID
     * @return The page code.
     */
    @Override
    public String getViewPage(String contentId) {
        Content type = this.getTypeById(contentId);
        return type.getViewPage();
    }

    /**
     * Return the code of the default model of content.
     *
     * @param contentId The content code
     * @return Il requested model code
     */
    @Override
    public String getDefaultModel(String contentId) {
        Content type = this.getTypeById(contentId);
        return type.getDefaultModel();
    }

    /**
     * Return the code of the model to be used when the content is rendered in
     * list
     *
     * @param contentId The code of the content
     * @return The code of the model
     */
    @Override
    public String getListModel(String contentId) {
        Content type = this.getTypeById(contentId);
        return type.getListModel();
    }

    /**
     * Return a complete content given its ID; it is possible to choose to
     * return the published -unmodifiable!- content or the working copy. It also
     * returns the data in the form of XML.
     *
     * @param id The ID of the content
     * @param onLine Specifies the type of the content to return: 'true'
     * references the published content, 'false' the freely modifiable one.
     * @return The requested content.
     * @throws EntException In case of error.
     */
    @Override
    public Content loadContent(String id, boolean onLine) throws EntException {
        try {
            ContentRecordVO contentVo = this.loadContentVO(id);
            return this.createContent(contentVo, onLine);
        } catch (EntException e) {
            logger.error("Error while loading content : id {}", id, e);
            throw new EntException("Error while loading content : id " + id, e);
        }
    }

    protected Content createContent(ContentRecordVO contentVo, boolean onLine) throws EntException {
        Content content = null;
        try {
            if (contentVo != null) {
                String xmlData;
                if (onLine) {
                    xmlData = contentVo.getXmlOnLine();
                } else {
                    xmlData = contentVo.getXmlWork();
                }
                if (xmlData != null) {
                    content = (Content) this.createEntityFromXml(contentVo.getTypeCode(), xmlData);
                    content.setId(contentVo.getId());
                    content.setTypeCode(contentVo.getTypeCode());
                    content.setDescription(contentVo.getDescription());
                    content.setOnLine(contentVo.isOnLine());
                    content.setMainGroup(contentVo.getMainGroupCode());
                    content.setSync(contentVo.isSync());
                    content.setStatus(contentVo.getStatus());
                    if (null != contentVo.getVersion() && !onLine) {
                        content.setVersion(contentVo.getVersion());
                    }
                    if (null == content.getFirstEditor()) {
                        content.setFirstEditor(contentVo.getFirstEditor());
                    }
                    if (null == content.getLastEditor()) {
                        content.setLastEditor(contentVo.getLastEditor());
                    }
                    if (null == content.getRestriction()) {
                        content.setRestriction(contentVo.getRestriction());
                    }
                    if (null == content.getCreated()) {
                        content.setCreated(contentVo.getCreate());
                    }
                    if (null == content.getLastModified()) {
                        content.setLastModified(contentVo.getModify());
                    }
                    if (null == content.getPublished()) {
                        content.setPublished(contentVo.getPublish());
                    }
                }
            }
        } catch (EntException e) {
            logger.error("Error while creating content by vo", e);
            throw new EntException("Error while creating content by vo", e);
        }
        return content;
    }

    /**
     * Return a {@link ContentRecordVO} (shortly: VO) containing the all content
     * informations stored in the DB.
     *
     * @param id The id of the requested content.
     * @return The VO object corresponding to the wanted content.
     * @throws EntException in case of error.
     */
    @Override
    public ContentRecordVO loadContentVO(String id) throws EntException {
        try {
            return (ContentRecordVO) this.getContentDAO().loadEntityRecord(id);
        } catch (Throwable t) {
            logger.error("Error while loading content vo : id {}", id, t);
            throw new EntException("Error while loading content vo : id " + id, t);
        }
    }

    /**
     * Save a content in the DB.
     *
     * @param content The content to add.
     * @return Id of the added content
     * @throws EntException in case of error.
     */
    @Override
    public String saveContent(Content content) throws EntException {
        return this.addContent(content);
    }

    @Override
    public String saveContentAndContinue(Content content) throws EntException {
        return this.addUpdateContent(content, false);
    }

    /**
     * Save a content in the DB. Hopefully this method has no annotation
     * attached
     * @param content The content to add.
     * @return Id of the added content
     * @throws EntException in case of error.
     */
    @Override
    public String addContent(Content content) throws EntException {
        return this.addUpdateContent(content, true);
    }

    private String addUpdateContent(Content content, boolean updateDate) throws EntException {
        String id = null;
        try {
            content.setLastModified(new Date());
            if (updateDate) {
                content.incrementVersion(false);
            }

            String status = content.getStatus();
            if (null == status || status.equals(Content.STATUS_PUBLIC)) {
                content.setStatus(Content.STATUS_DRAFT);
            } else {
                content.setStatus(status);
            }
            id = content.getId();
            if (null == id) {
                IKeyGeneratorManager keyGenerator = (IKeyGeneratorManager) this
                        .getService(SystemConstants.KEY_GENERATOR_MANAGER);
                int key = keyGenerator.getUniqueKeyCurrentValue();
                id = content.getTypeCode() + key;
                content.setId(id);
            }

            boolean exists = loadContentVO(content.getId()) != null;
            if (exists) {
                this.getContentDAO().updateContent(content, updateDate);
            } else {
                this.getContentDAO().addEntity(content);
            }
        } catch (Throwable t) {
            logger.error("Error while saving content", t);
            throw new EntException("Error while saving content", t);
        }
        return id;
    }

    /**
     * Publish a content.
     *
     * @param content The ID associated to the content to be displayed in the
     * portal.
     * @return Id of the published content
     * @throws EntException in case of error.
     */
    @Override
    @CacheEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            key = "T(com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants).CONTENT_CACHE_PREFIX.concat(#content.id)", condition = "#content.id != null")
    @CacheInfoEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            groups = "T(com.agiletec.plugins.jacms.aps.system.services.cache.CmsCacheWrapperManager).getContentCacheGroupsToEvictCsv(#content.id, #content.typeCode)")
    public String insertOnLineContent(Content content) throws EntException {
        String id = null;
        try {
            content.setLastModified(new Date());
            if (null == content.getId()) {
                content.setCreated(new Date());
                this.saveContent(content);
            }
            content.incrementVersion(true);
            content.setStatus(Content.STATUS_PUBLIC);
            this.getContentDAO().insertOnLineContent(content);
            int operationEventCode;
            if (content.isOnLine()) {
                operationEventCode = PublicContentChangedEvent.UPDATE_OPERATION_CODE;
            } else {
                operationEventCode = PublicContentChangedEvent.INSERT_OPERATION_CODE;
            }
            id = content.getId();
            this.notifyPublicContentChanging(content, operationEventCode);
        } catch (Throwable t) {
            logger.error("Error while inserting content on line", t);
            throw new EntException("Error while inserting content on line", t);
        }
        return id;
    }

    /**
     * Return the list of all the content IDs.
     *
     * @return The list of all the content IDs.
     * @throws EntException In case of error
     * @deprecated Since Entando 2.0 version 2.0.9, use
     * searchId(EntitySearchFilter[]) method
     */
    @Override
    @Deprecated
    public List<String> getAllContentsId() throws EntException {
        return super.getAllEntityId();
    }

    @Override
    public void reloadEntityReferences(String entityId) {
        try {
            ContentRecordVO contentVo = this.loadContentVO(entityId);
            Content content = this.createContent(contentVo, true);
            if (content != null) {
                this.getContentDAO().reloadPublicContentReferences(content);
            }
            Content workcontent = this.createContent(contentVo, false);
            if (workcontent != null) {
                this.getContentDAO().reloadWorkContentReferences(workcontent);
            }
            logger.debug("Reloaded content references for content {}", entityId);
        } catch (Throwable t) {
            logger.error("Error while reloading content references for content {}", entityId, t);
        }
    }

    /**
     * Unpublish a content, preventing it from being displayed in the portal.
     * Obviously the content itself is not deleted.
     *
     * @param content the content to unpublish.
     * @return Id of unpublished content
     * @throws EntException in case of error
     */
    @Override
    @CacheEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            key = "T(com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants).CONTENT_CACHE_PREFIX.concat(#content.id)", condition = "#content.id != null")
    @CacheInfoEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            groups = "T(com.agiletec.plugins.jacms.aps.system.services.cache.CmsCacheWrapperManager).getContentCacheGroupsToEvictCsv(#content.id, #content.typeCode)")
    public String removeOnLineContent(Content content) throws EntException {
        try {
            content.setLastModified(new Date());
            content.incrementVersion(false);
            if (null != content.getStatus() && content.getStatus().equals(Content.STATUS_PUBLIC)) {
                content.setStatus(Content.STATUS_DRAFT);
            }
            this.getContentDAO().removeOnLineContent(content);
            this.notifyPublicContentChanging(content, PublicContentChangedEvent.REMOVE_OPERATION_CODE);
            return content.getId();
        } catch (Throwable t) {
            logger.error("Error while removing onLine content", t);
            throw new EntException("Error while removing onLine content", t);
        }
    }

    /**
     * Notify the modification of a published content.
     *
     * @param content The modified content.
     * @param operationCode the operation code to notify.
     */
    private void notifyPublicContentChanging(Content content, int operationCode) {
        Map<String, String> properties = new HashMap<>();
        if (null != content) {
            properties.put("contentId", content.getId());
        }
        properties.put("operationCode", String.valueOf(operationCode));
        PublicContentChangedEvent event = new PublicContentChangedEvent(JacmsSystemConstants.CONTENT_EVENT_CHANNEL, properties);
        event.setContent(content);
        event.setOperationCode(operationCode);
        this.notifyEvent(event);
    }

    /**
     * Return the content type from the given ID code. The code is extracted
     * following the coding conventions: the first three characters are the type
     * of the code.
     *
     * @param contentId the content ID whose content type is extracted.
     * @return The content type requested
     */
    protected Content getTypeById(String contentId) {
        String typeCode = contentId.substring(0, 3);
        return this.getEntityPrototype(typeCode);
    }
    
    @Override
    public Content getEntityPrototype(String typeCode) {
        Content type = null;
        try {
            String cacheKey = CONTENT_TYPE_CACHE_PREFIX + typeCode;
            type = (Content) ((CacheInfoManager) this.getCacheInfoManager()).getFromCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey);
            if (null == type) {
                type = (Content) super.getEntityPrototype(typeCode);
                if (null != type) {
                    String typeGroupKey = JacmsSystemConstants.CONTENT_TYPE_CACHE_GROUP_PREFIX + typeCode;
                    ((CacheInfoManager) this.getCacheInfoManager()).putInCache(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey, type, new String[]{typeGroupKey});
                }
            }
            if (null != type) {
                return (Content) type.getEntityPrototype();
            }
        } catch (Exception e) {
            logger.error("Error while extracting content type {}", typeCode, e);
            throw new EntRuntimeException("Error while extracting content type " + typeCode, e);
        }
        return type;
    }
    
    /**
     * Deletes a content from the DB.
     *
     * @param content The content to delete.
     * @return Id of deleted content
     * @throws EntException in case of error.
     */
    @Override
    @CacheEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            key = "T(com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants).CONTENT_CACHE_PREFIX.concat(#content.id)", condition = "#content.id != null")
    @CacheInfoEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            groups = "T(com.agiletec.plugins.jacms.aps.system.services.cache.CmsCacheWrapperManager).getContentCacheGroupsToEvictCsv(#content.id)")
    public String deleteContent(Content content) throws EntException {
        return this.deleteContent(content.getId());
    }

    @Override
    @CacheEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            key = "T(com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants).CONTENT_CACHE_PREFIX.concat(#contentId)", condition = "#contentId != null")
    @CacheInfoEvict(value = ICacheInfoManager.DEFAULT_CACHE_NAME,
            groups = "T(com.agiletec.plugins.jacms.aps.system.services.cache.CmsCacheWrapperManager).getContentCacheGroupsToEvictCsv(#contentId)")
    public String deleteContent(String contentId) throws EntException {
        try {
            this.getContentDAO().deleteEntity(contentId);
            return contentId;
        } catch (Throwable t) {
            logger.error("Error while deleting content {}", contentId, t);
            throw new EntException("Error while deleting content " + contentId, t);
        }
    }

    @Override
    public List<String> loadPublicContentsId(String contentType, String[] categories, EntitySearchFilter[] filters,
            Collection<String> userGroupCodes) throws EntException {
        return this.loadPublicContentsId(contentType, categories, false, filters, userGroupCodes);
    }

    @Override
    public List<String> loadPublicContentsId(String contentType, String[] categories, boolean orClauseCategoryFilter,
            EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        try {
            return this.getPublicContentSearcherDAO().loadContentsId(contentType, categories, orClauseCategoryFilter, filters, userGroupCodes);
        } catch (Throwable t) {
            logger.error(ERROR_WHILE_LOADING_CONTENTS, t);
            throw new EntException(ERROR_WHILE_LOADING_CONTENTS, t);
        }
    }

    @Override
    public List<String> loadPublicContentsId(String[] categories,
            EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        return this.loadPublicContentsId(categories, false, filters, userGroupCodes);
    }

    @Override
    public List<String> loadPublicContentsId(String[] categories, boolean orClauseCategoryFilter,
            EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        try {
            return this.getPublicContentSearcherDAO().loadContentsId(categories, orClauseCategoryFilter, filters, userGroupCodes);
        } catch (Throwable t) {
            logger.error(ERROR_WHILE_LOADING_CONTENTS, t);
            throw new EntException(ERROR_WHILE_LOADING_CONTENTS, t);
        }
    }

    @Override
    public List<String> loadWorkContentsId(EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        return this.loadWorkContentsId(null, false, filters, userGroupCodes);
    }

    @Override
    public List<String> loadWorkContentsId(String[] categories, EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        return this.loadWorkContentsId(categories, false, filters, userGroupCodes);
    }

    @Override
    public List<String> loadWorkContentsId(String[] categories, boolean orClauseCategoryFilter,
            EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        try {
            return this.getWorkContentSearcherDAO().loadContentsId(categories, orClauseCategoryFilter, filters, userGroupCodes);
        } catch (Throwable t) {
            logger.error("Error while loading work contents", t);
            throw new EntException("Error while loading work contents", t);
        }
    }

    @Override
    public Integer countWorkContents(String[] categories, boolean orClauseCategoryFilter,
            EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        try {
            return this.getWorkContentSearcherDAO().countContents(categories, orClauseCategoryFilter, filters, userGroupCodes);
        } catch (Throwable t) {
            logger.error("Error while counting work contents", t);
            throw new EntException("Error while counting work contents", t);
        }
    }

    @Override
    public SearcherDaoPaginatedResult<String> getPaginatedWorkContentsId(String[] categories, boolean orClauseCategoryFilter, EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        return this.getPaginatedContentsId(categories, orClauseCategoryFilter, filters, userGroupCodes, this.getWorkContentSearcherDAO());
    }

    @Override
    public SearcherDaoPaginatedResult<String> getPaginatedPublicContentsId(String[] categories, boolean orClauseCategoryFilter, EntitySearchFilter[] filters, Collection<String> userGroupCodes) throws EntException {
        return this.getPaginatedContentsId(categories, orClauseCategoryFilter, filters, userGroupCodes, this.getPublicContentSearcherDAO());
    }
    
    private SearcherDaoPaginatedResult<String> getPaginatedContentsId(String[] categories, boolean orClauseCategoryFilter, 
            EntitySearchFilter[] filters, Collection<String> userGroupCodes, IContentSearcherDAO searcherDao) throws EntException {
        SearcherDaoPaginatedResult<String> pagedResult = null;
        try {
            int count = searcherDao.countContents(categories, orClauseCategoryFilter, filters, userGroupCodes);
            List<String> contentsId = searcherDao.loadContentsId(categories, orClauseCategoryFilter, filters, userGroupCodes);
            pagedResult = new SearcherDaoPaginatedResult<>(count, contentsId);
        } catch (Throwable t) {
            logger.error("Error searching paginated contents id", t);
            throw new EntException("Error searching paginated contents id", t);
        }
        return pagedResult;
    }
    
    @Override
    public List getPageUtilizers(String pageCode) throws EntException {
        try {
            return this.getContentDAO().getPageUtilizers(pageCode);
        } catch (Throwable t) {
            throw new EntException("Error while loading referenced contents : page " + pageCode, t);
        }
    }

    @Override
    public List getContentUtilizers(String contentId) throws EntException {
        try {
            return this.getContentDAO().getContentUtilizers(contentId);
        } catch (Throwable t) {
            throw new EntException("Error while loading referenced contents : content " + contentId, t);
        }
    }

    @Override
    public List<String> getGroupUtilizers(String groupName) throws EntException {
        try {
            return this.getContentDAO().getGroupUtilizers(groupName);
        } catch (Throwable t) {
            throw new EntException("Error while loading referenced contents : group " + groupName, t);
        }
    }

    @Override
    public List getResourceUtilizers(String resourceId) throws EntException {
        try {
            return this.getContentDAO().getResourceUtilizers(resourceId);
        } catch (Throwable t) {
            throw new EntException("Error while loading referenced contents : resource " + resourceId, t);
        }
    }

    @Override
    public List getCategoryUtilizers(String resourceId) throws EntException {
        try {
            return this.getContentDAO().getCategoryUtilizers(resourceId);
        } catch (Throwable t) {
            throw new EntException("Error while loading referenced contents : category " + resourceId, t);
        }
    }

    @Override
    public void reloadCategoryReferences(String categoryCode) {
        try {
            this.getContentUpdaterService().reloadCategoryReferences(categoryCode);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "reloadCategoryReferences");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getCategoryUtilizersForReloadReferences(String categoryCode) {
        List<String> contentIdToReload = new ArrayList<>();
        try {
            Set<String> contents = this.getContentUpdaterService().getContentsId(categoryCode);
            if (null != contents) {
                contentIdToReload.addAll(contents);
            }
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "getCategoryUtilizersForReloadReferences");
        }
        return contentIdToReload;
    }

    @Override
    public boolean isSearchEngineUser() {
        return true;
    }

    @Override
    public ContentsStatus getContentsStatus() {
        ContentsStatus status = null;
        try {
            status = this.getContentDAO().loadContentStatus();
        } catch (Throwable t) {
            logger.error("error in getContentsStatus", t);
        }
        return status;
    }

    @Override
    public void removeEntityPrototype(String entityTypeCode) throws EntException {
        super.removeEntityPrototype(entityTypeCode);
        String cacheKey = CONTENT_TYPE_CACHE_PREFIX + entityTypeCode;
        this.getCacheInfoManager().flushEntry(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey);
    }

    @Override
    public void updateEntityPrototype(IApsEntity entityType) throws EntException {
        super.updateEntityPrototype(entityType);
        String cacheKey = CONTENT_TYPE_CACHE_PREFIX + entityType.getTypeCode();
        this.getCacheInfoManager().flushEntry(ICacheInfoManager.DEFAULT_CACHE_NAME, cacheKey);
    }

    /**
     * Return the DAO which handles all the operations on the contents.
     *
     * @return The DAO managing the contents.
     */
    protected IContentDAO getContentDAO() {
        return contentDAO;
    }

    /**
     * Set the DAO which handles the operations on the contents.
     *
     * @param contentDao The DAO managing the contents.
     */
    public void setContentDAO(IContentDAO contentDao) {
        this.contentDAO = contentDao;
    }

    @Override
    protected IEntitySearcherDAO getEntitySearcherDao() {
        return this.getWorkContentSearcherDAO();
    }

    @Override
    protected IEntityDAO getEntityDao() {
        return this.getContentDAO();
    }

    protected IContentSearcherDAO getWorkContentSearcherDAO() {
        return workContentSearcherDAO;
    }

    public void setWorkContentSearcherDAO(IContentSearcherDAO workContentSearcherDAO) {
        this.workContentSearcherDAO = workContentSearcherDAO;
    }

    public IContentSearcherDAO getPublicContentSearcherDAO() {
        return publicContentSearcherDAO;
    }

    public void setPublicContentSearcherDAO(IContentSearcherDAO publicContentSearcherDAO) {
        this.publicContentSearcherDAO = publicContentSearcherDAO;
    }

    protected IContentUpdaterService getContentUpdaterService() {
        return contentUpdaterService;
    }

    public void setContentUpdaterService(IContentUpdaterService contentUpdaterService) {
        this.contentUpdaterService = contentUpdaterService;
    }

    @Override
    public IApsEntity getEntity(String entityId) throws EntException {
        return this.loadContent(entityId, false);
    }

    /**
     * @deprecated From jAPS 2.0 version 2.0.9, use getStatus()
     */
    @Override
    @Deprecated
    public int getState() {
        return super.getStatus();
    }

    protected ICacheInfoManager getCacheInfoManager() {
        return this.cacheInfoManager;
    }
    @Autowired
    public void setCacheInfoManager(ICacheInfoManager cacheInfoManager) {
        this.cacheInfoManager = cacheInfoManager;
    }

}
