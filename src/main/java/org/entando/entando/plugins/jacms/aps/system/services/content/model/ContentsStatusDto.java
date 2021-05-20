/*
 * Copyright 2021-Present Entando Inc. (http://www.entando.com) All rights reserved.
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
package org.entando.entando.plugins.jacms.aps.system.services.content.model;

import com.agiletec.plugins.jacms.aps.system.services.content.ContentsStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.Date;
import org.entando.entando.web.common.json.JsonDateSerializer;

/**
 * @author E.Santoboni
 */
public class ContentsStatusDto implements Serializable {
    
    private Integer unpublished;
    private Integer ready;
    private Integer published;
    private Integer total;
    private Date latestModificationDate;
    
    public ContentsStatusDto(ContentsStatus status) {
        this.latestModificationDate = status.getLastUpdate();
        this.unpublished = status.getDraft();
        this.ready = status.getOnlineWithChanges();
        this.published = status.getOnline();
        this.total = status.getTotal();
    }

    public Integer getUnpublished() {
        return unpublished;
    }

    public Integer getReady() {
        return ready;
    }

    public Integer getPublished() {
        return published;
    }

    public Integer getTotal() {
        return total;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getLatestModificationDate() {
        return latestModificationDate;
    }
    
}
