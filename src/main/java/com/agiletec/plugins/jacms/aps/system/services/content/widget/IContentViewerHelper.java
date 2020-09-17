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
package com.agiletec.plugins.jacms.aps.system.services.content.widget;

import com.agiletec.aps.system.RequestContext;
import org.entando.entando.ent.exception.EntException;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.PublicContentAuthorizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.dispenser.ContentRenderizationInfo;

/**
 * Interfaccia base per le classi helper per le showlet erogatori contenuti.
 * @author E.Santoboni
 */
public interface IContentViewerHelper {
    
    /**
     * Restituisce il contenuto da visualizzare nella showlet.
     * @param contentId L'identificativo del contenuto ricavato dal tag.
     * @param modelId Il modello del contenuto ricavato dal tag.
     * @param reqCtx Il contesto della richiesta.
     * @return Il contenuto da visualizzare nella showlet.
     * @throws EntException In caso di errore
     */
	public String getRenderedContent(String contentId, String modelId, RequestContext reqCtx) throws EntException;

	public String getRenderedContent(String contentId, String modelId, boolean publishExtraTitle, RequestContext reqCtx) throws EntException;
	
	public ContentRenderizationInfo getRenderizationInfo(String contentId, String modelId, boolean publishExtraTitle, RequestContext reqCtx) throws EntException;
	
	public PublicContentAuthorizationInfo getAuthorizationInfo(String contentId, RequestContext reqCtx) throws EntException;
	
}