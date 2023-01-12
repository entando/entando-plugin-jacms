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
package org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.entando.entando.ent.exception.EntException;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.report.DefaultBulkCommandReport;

/**
 * A base class for the execution of a {@link ApsCommand} on multiple items.
 *
 * @author E.Mezzano
 *
 * @param <I> The type of items on which to apply the command.
 * @param <A> The applier of the command (for example: the Content Manager that execute the update of a Content).
 * @param <C>
 */
public abstract class BaseBulkCommand<I, A, C extends BulkCommandContext<I>> implements ApsCommand<C> {

    @Override
    public void init(C context) {
        this.setContext(context);
    }

    public DefaultBulkCommandReport<I> getReport() {
        DefaultBulkCommandReport<I> report = new DefaultBulkCommandReport<>();
        report.setApplyErrors(this.getErrors().size());
        report.setApplySuccesses(this.getSuccesses().size());
        report.setApplyTotal(this.getErrors().size() + this.getSuccesses().size());
        report.setEndingTime(this.getEndingTime());
        report.setErrors(this.getErrors());
        report.setSuccesses(this.getSuccesses());
        report.setTotal(this.getItems().size());
        report.setCommandName(this.getName());
        return report;
    }

    public abstract boolean apply(I item) throws EntException;

    /**
     * Returns the items on which to apply the command.
     * @return The items on which to apply the command.
     */
    public Collection<I> getItems() {
        return this.getContext().getItems();
    }

    /**
     * Returns the applier of the command (for example: the Content Manager that execute the update of a Content)
     * @return The applier of the command (for example: the Content Manager that execute the update of a Content)
     */
    public A getApplier() {
        return applier;
    }
    /**
     * Sets the applier of the command (for example: the Content Manager that execute the update of a Content)
     * @param applier The applier of the command (for example: the Content Manager that execute the update of a Content)
     */
    protected void setApplier(A applier) {
        this.applier = applier;
    }

    /**
     * Returns the number items onto apply the command.
     * @return The number items onto apply the command.
     */
    public int getTotal() {
        return this.getItems().size();
    }

    @Override
    public Date getEndingTime() {
        return endingTime;
    }
    @Override
    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    protected C getContext() {
        return context;
    }
    protected void setContext(C context) {
        this.context = context;
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

    private A applier;
    private Date endingTime;

    private C context;

    private List<I> successes = new CopyOnWriteArrayList<>();
    private Map<I, ApsCommandErrorCode> errors = new ConcurrentHashMap<>();

}