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
package org.entando.entando.plugins.jacms.web.resource.validator;

import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.content.ContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.BaseContentListHelper;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import com.agiletec.plugins.jacms.aps.system.services.resource.ResourceManager;
import com.agiletec.plugins.jacms.aps.system.services.resource.ResourceUtilizer;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import org.entando.entando.aps.util.GenericResourceUtils;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.plugins.jacms.web.resource.model.ImageAssetDto;
import org.entando.entando.web.common.validator.AbstractPaginationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Collection;
import java.util.List;

@Component
public class ResourcesValidator extends AbstractPaginationValidator {

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private ContentManager contentManager;

    private final static String ERR_CODE_RESOURCE_REFERENCED_CONTENT_ERROR ="20";
    private final static String ERR_CODE_RESOURCE_REFERENCED_NOT_DELETABLE ="21";
    private final static String ERR_MSG_RESOURCE_REFERENCED_CONTENT_ERROR ="plugins.jacms.resources.delete.error.contentError";
    private final static String ERR_MSG_RESOURCE_REFERENCED_NOT_DELETABLE ="plugins.jacms.resources.delete.error.referenced";

    public boolean isResourceDeletableByUser(String resourceId, String correlationCode, UserDetails user) throws EntException {
        final Collection<String> allowedGroupCodes = BaseContentListHelper.getAllowedGroupCodes(user);
        final ResourceInterface resource = resourceManager.loadResource(resourceId, correlationCode);
        if (resource != null)  {
            return allowedGroupCodes.stream().anyMatch(group ->
                GenericResourceUtils
                        .isResourceAccessibleByGroup(group, resource.getMainGroup(), null)
        );
        }
        return false;
    }

    public boolean resourceExists(String resourceId, String correlationCode) throws EntException {
        final ResourceInterface resource = resourceManager.loadResource(resourceId, correlationCode);
        if (resource!=null){
            return true;
        }
        return false;
    }

    public void resourceReferencesValidation(String resourceId, Errors errors) throws EntException {
        List<String> references = ((ResourceUtilizer) contentManager).getResourceUtilizers(resourceId);
        if (references != null && references.size() > 0) {
            references.forEach(reference->{
                ContentRecordVO record = null;
                try {
                    record = contentManager.loadContentVO(reference);
                } catch (EntException e) {
                    errors.reject(ERR_CODE_RESOURCE_REFERENCED_CONTENT_ERROR, new String[]{resourceId, record.getDescription()}, ERR_MSG_RESOURCE_REFERENCED_CONTENT_ERROR);
                }
                if (null != record) {
                    errors.reject(ERR_CODE_RESOURCE_REFERENCED_NOT_DELETABLE, new String[]{resourceId, record.getId(), record.getDescription()}, ERR_MSG_RESOURCE_REFERENCED_NOT_DELETABLE);
                }
                });
            }
    }

    @Override
    public boolean supports(Class<?> paramClass) {
        return ImageAssetDto.class.equals(paramClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

    }

    @Override
    protected String getDefaultSortProperty() {
        return "name";
    }
}
