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

import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentDto;
import java.util.List;
import org.entando.entando.aps.system.services.IComponentExistsService;
import org.entando.entando.plugins.jacms.aps.system.services.content.model.ContentsStatusDto;
import org.entando.entando.plugins.jacms.web.content.validator.RestContentListRequest;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.common.model.RestListRequest;
import org.springframework.validation.BindingResult;

/**
 * @author E.Santoboni
 */
public interface IContentService extends IComponentExistsService {

    String STATUS_ONLINE = "published";
    String STATUS_DRAFT = "draft";
    String STATUS_UNPUBLISHED = "unpublished";

    String MODE_LIST = "list";
    String MODE_FULL = "full";

    ContentDto getContent(String code, String modelId, String status, String langCode, boolean resolveLinks, UserDetails user);

    ContentDto addContent(ContentDto request, UserDetails user, BindingResult bindingResult);

    ContentDto updateContent(ContentDto request, UserDetails user, BindingResult bindingResult);

    void deleteContent(String code, UserDetails user);

    ContentDto updateContentStatus(String code, String status, UserDetails user);
    
    List<ContentDto> updateContentsStatus(List<String> codes, String status, UserDetails user);

    PagedMetadata<?> getContentReferences(String code, String manager, UserDetails user, RestListRequest requestList);

    PagedMetadata<ContentDto> getContents(RestContentListRequest requestList, UserDetails user);

    Integer countContentsByType(String contentType);

    ContentsStatusDto getContentsStatus();

    ContentDto cloneContent(String code, UserDetails user, BindingResult bindingResult);

}
