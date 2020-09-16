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

import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.BaseContentListHelper;
import com.agiletec.plugins.jacms.aps.system.services.resource.ResourceManager;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import java.util.Collection;
import org.entando.entando.aps.util.GenericResourceUtils;
import org.entando.entando.plugins.jacms.web.resource.model.ImageAssetDto;
import org.entando.entando.web.common.validator.AbstractPaginationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class ResourcesValidator extends AbstractPaginationValidator {

    @Autowired
    private ResourceManager resourceManager;
    
    public boolean isResourceDeletableByUser(String resourceId, UserDetails user) throws ApsSystemException {
        final Collection<String> allowedGroupCodes = BaseContentListHelper.getAllowedGroupCodes(user);
        final ResourceInterface resource = resourceManager.loadResource(resourceId);
        if (resource != null)  {
            return allowedGroupCodes.stream().anyMatch(group ->
                GenericResourceUtils
                        .isResourceAccessibleByGroup(group, resource.getMainGroup(), null)
        );
        }
        return false;
    }

    public boolean resourceExists(String resourceId) throws ApsSystemException {
        final ResourceInterface resource = resourceManager.loadResource(resourceId);
        if (resource!=null){
            return true;
        }
        return false;
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
