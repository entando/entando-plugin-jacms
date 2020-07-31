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
package org.entando.entando.plugins.jacms.web.resource.request;

import java.util.List;
import java.util.Objects;
import org.entando.entando.web.common.model.RestListRequest;

public class ListResourceRequest extends RestListRequest {

    private String type;
    private String forLinkingWithOwnerGroup;
    private List<String> forLinkingWithExtraGroups;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ListResourceRequest that = (ListResourceRequest) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(forLinkingWithOwnerGroup, that.forLinkingWithOwnerGroup) &&
                Objects.equals(forLinkingWithExtraGroups, that.forLinkingWithExtraGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, forLinkingWithOwnerGroup, forLinkingWithExtraGroups);
    }
}
