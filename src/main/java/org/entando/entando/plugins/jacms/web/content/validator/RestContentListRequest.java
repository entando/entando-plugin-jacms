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
package org.entando.entando.plugins.jacms.web.content.validator;

import java.util.List;

import io.swagger.annotations.ApiParam;
import org.entando.entando.plugins.jacms.aps.system.services.content.IContentService;
import org.entando.entando.web.common.model.RestEntityListRequest;

public class RestContentListRequest extends RestEntityListRequest {

    private String model;
    private boolean resolveLink;
    private String status = IContentService.STATUS_DRAFT;
    private String lang;

    private String[] categories;
    private boolean orClauseCategoryFilter;
    private String text;

    private String forLinkingWithOwnerGroup;
    private List<String> forLinkingWithExtraGroups;

    @ApiParam(name = "mode", defaultValue = IContentService.MODE_FULL,
            value = "list -> light version, full -> brings extra data in the content.", allowableValues = "list, full")
    private String mode = IContentService.MODE_FULL;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isResolveLink() {
        return resolveLink;
    }

    public void setResolveLink(boolean resolveLink) {
        this.resolveLink = resolveLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public boolean isOrClauseCategoryFilter() {
        return orClauseCategoryFilter;
    }

    public void setOrClauseCategoryFilter(boolean orClauseCategoryFilter) {
        this.orClauseCategoryFilter = orClauseCategoryFilter;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getForLinkingWithOwnerGroup() {
        return forLinkingWithOwnerGroup;
    }

    public void setForLinkingWithOwnerGroup(String forLinkingWithOwnerGroup) {
        this.forLinkingWithOwnerGroup = forLinkingWithOwnerGroup;
    }

    public List<String> getForLinkingWithExtraGroups() {
        return forLinkingWithExtraGroups;
    }

    public void setForLinkingWithExtraGroups(List<String> forLinkingWithExtraGroups) {
        this.forLinkingWithExtraGroups = forLinkingWithExtraGroups;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
