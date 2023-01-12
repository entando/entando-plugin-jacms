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
package org.entando.entando.plugins.jacms.apsadmin.content.bulk.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.HashedMap;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands.ApsCommandErrorCode;


/**
 * The default report for a bulk {@link ApsCommand}.
 * It's a wrapper of the {@link BaseBulkCommand} and of its {@link BulkCommandTracer}.
 * @author E.Mezzano
 *
 * @param <I> The type of items on which the command is applied.
 */
public class DefaultBulkCommandReport<I> {

    private String commandName;
    private Integer applyTotal;
    private Integer total;
    private Integer applySuccesses;
    private Integer applyErrors;
    private Date endingTime;
    private List<I> successes = new ArrayList<>();
    private Map<I, ApsCommandErrorCode> errors = new HashedMap<>();

    public String getCommandName() {
        return commandName;
    }
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public Integer getApplyTotal() {
        return applyTotal;
    }
    public void setApplyTotal(Integer applyTotal) {
        this.applyTotal = applyTotal;
    }

    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getApplySuccesses() {
        return applySuccesses;
    }
    public void setApplySuccesses(Integer applySuccesses) {
        this.applySuccesses = applySuccesses;
    }

    public Integer getApplyErrors() {
        return applyErrors;
    }
    public void setApplyErrors(Integer applyErrors) {
        this.applyErrors = applyErrors;
    }

    public Date getEndingTime() {
        return endingTime;
    }
    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    public List<I> getSuccesses() {
        return successes;
    }
    public void setSuccesses(List<I> successes) {
        this.successes = successes;
    }

    public Map<I, ApsCommandErrorCode> getErrors() {
        return errors;
    }
    public void setErrors(Map<I, ApsCommandErrorCode> errors) {
        this.errors = errors;
    }

}