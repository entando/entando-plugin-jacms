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
package com.agiletec.plugins.jacms.aps.system.services.content.model.attribute;

import com.agiletec.plugins.jacms.aps.system.services.resource.model.AttachResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import org.apache.commons.lang3.StringUtils;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;

/**
 * Rappresenta un attributo di entità di tipo attachment. L'attachment e il
 * testo associato possono essere diversi per ciascun lingua.
 *
 * @author M.Diana - S.Didaci - E.Santoboni
 */
public class AttachAttribute extends AbstractResourceAttribute {

	private static final EntLogger _logger = EntLogFactory.getSanitizedLogger(AttachAttribute.class);

	/**
	 * Restituisce il path URL dell'attachment.
	 *
	 * @return Il path dell'attachment.
	 */
	public String getAttachPath() {
		String attachPath = "";
		ResourceInterface res = this.getResource();
		if (null != res) {
			attachPath = ((AttachResource) res).getAttachPath();
			attachPath = this.appendContentReference(attachPath);
		}
		return attachPath;
	}

	@Override
	protected String getDefaultPath() {
		return this.getAttachPath();
	}

	@Override
	public String getIndexeableFieldValue() {
		StringBuilder buffer = new StringBuilder();
		String parentText = super.getIndexeableFieldValue();
		if (!StringUtils.isBlank(parentText)) {
			buffer.append(parentText);
		}
		ResourceInterface resource = this.getResource();
		String extraValue = this.getResourceManager().getResourceText(resource);
		if (!StringUtils.isBlank(extraValue)) {
			buffer.append(" ").append(extraValue);
		}
		return buffer.toString();
	}

	@Override
	public boolean isSearchableOptionSupported() {
		return false;
	}

}
