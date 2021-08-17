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
package com.agiletec.plugins.jacms.aps.system.services.resource.event;

import com.agiletec.aps.system.common.IManager;
import com.agiletec.aps.system.common.notify.ApsEvent;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import java.util.Map;

/**
 * The event of resource changing
 * @author E.Santoboni - M.Diana
 */
public class ResourceChangedEvent extends ApsEvent {
    
    public static final int INSERT_OPERATION_CODE = 1;
	
	public static final int REMOVE_OPERATION_CODE = 2;
	
	public static final int UPDATE_OPERATION_CODE = 3;

    public ResourceChangedEvent(String channel, Map<String, String> properties) {
        super(channel, properties);
    }
	
	@Override
	public void notify(IManager srv) {
		((ResourceChangedObserver) srv).updateFromResourceChanged(this);
	}
	
    @Override
	public Class getObserverInterface() {
		return ResourceChangedObserver.class;
	}
	
	public ResourceInterface getResource() {
		return resource;
	}
	public void setResource(ResourceInterface resource) {
		this.resource = resource;
	}
    
    public int getOperationCode() {
		return operationCode;
	}

	public void setOperationCode(int operationCode) {
		this.operationCode = operationCode;
	}
	
	private int operationCode;
	
	private transient ResourceInterface resource;
	
}