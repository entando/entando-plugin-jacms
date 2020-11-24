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
package com.agiletec.plugins.jacms.aps.system.services.resource.model;

import java.util.List;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;

/**
 * Classe rappresentante una risorsa Attach.
 * @author W.Ambu - E.Santoboni
 */
public class AttachResource extends AbstractMonoInstanceResource  {

	private static final EntLogger _logger = EntLogFactory.getSanitizedLogger(AttachResource.class);
	
    /**
     * Restituisce il path della risorsa attach.
     * La stringa restituita è comprensiva del folder della risorsa e 
     * del nome del file dell'istanza richiesta.
     * @return Il path della risorsa attach.
     */
    public String getAttachPath() {
    	ResourceInstance instance = this.getInstance();
    	String path = this.getUrlPath(instance);
    	return path;
    }
    
    /**
     * Restituisce il path della risorsa attach.
     * La stringa restituita è comprensiva del folder della risorsa e 
     * del nome del file dell'istanza richiesta.
     * @return Il path della risorsa attach.
     * @deprecated use getAttachPath
     */
    public String getDocumentPath() {
    	return this.getAttachPath();
    }

    @Override
	public void saveResourceInstances(ResourceDataBean bean, List<String> ignoreMetadataKeys,
			boolean instancesAlreadySaved) throws EntException {
		try {
			String fileName = this.getNewInstanceFileName(bean.getFileName());
			String subPath = this.getDiskSubFolder() + fileName;
			this.getStorageManager().saveFile(subPath, this.isProtectedResource(), bean.getInputStream());
			ResourceInstance instance = new ResourceInstance();
			instance.setSize(0);
			instance.setFileName(fileName);
			String mimeType = bean.getMimeType();
			instance.setMimeType(mimeType);
			instance.setFileLength(bean.getFileSize() + " Kb");
			this.addInstance(instance);
		} catch (Throwable t) {
			_logger.error("Error on saving attach resource instances", t);
			throw new EntException("Error on saving attach resource instances", t);
		}
	}

	@Override
	public void reloadResourceInstances() throws EntException {
		//Not supported
	}
    
}