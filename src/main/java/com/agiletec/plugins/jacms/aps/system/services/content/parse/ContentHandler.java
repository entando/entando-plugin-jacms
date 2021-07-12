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
package com.agiletec.plugins.jacms.aps.system.services.content.parse;

import com.agiletec.aps.system.common.entity.parse.EntityHandler;
import com.agiletec.aps.system.services.category.Category;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.aps.util.DateConverter;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import java.util.Date;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Classe "handler" di supporto all'interpretazione
 * dell'XML che rappresenta un contenuto.
 * @author M.Diana - E.Santoboni
 */
public class ContentHandler extends EntityHandler {

	private static final EntLogger _logger = EntLogFactory.getSanitizedLogger(ContentHandler.class);

	private ICategoryManager categoryManager;

	@Override
	public EntityHandler getHandlerPrototype() {
		ContentHandler handler = (ContentHandler) super.getHandlerPrototype();
		handler.setCategoryManager(this.getCategoryManager());
		return handler;
	}

	@Override
	protected void startEntityElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try {
			if (qName.equals("categories")) {
				this.startCategories(attributes, qName);
			} else if (qName.equals("category")) {
				this.startCategory(attributes, qName);
			} else if (qName.equals("status")) {
				this.startStatus(attributes, qName);
			} else if (qName.equals("version")) {
				this.startVersion(attributes, qName);
			} else if (qName.equals("firstEditor")) {
				this.startFirstEditor(attributes, qName);
			} else if (qName.equals("lastEditor")) {
				this.startLastEditor(attributes, qName);
			} else if (qName.equals("created")) {
				this.startCreated(attributes, qName);
			} else if (qName.equals("lastModified")) {
				this.startLastModified(attributes, qName);
			} else if (qName.equals("restriction")) {
				this.startRestriction(attributes, qName);
			}
		} catch (SAXException e) {
			_logger.error("error in start element", e);
			throw e;
		} catch (Throwable t) {
			_logger.error("error in start element", t);
			throw new SAXException(new Exception(t));
		}
	}

	@Override
	protected void endEntityElement(String uri, String localName, String qName) throws SAXException {
		try {
			if (qName.equals("categories")) {
				this.endCategories();
			} else if (qName.equals("category")) {
				this.endCategory();
			} else if (qName.equals("status")) {
				this.endStatus();
			} else if (qName.equals("version")) {
				this.endVersion();
			} else if (qName.equals("firstEditor")) {
				this.endFirstEditor();
			} else if (qName.equals("lastEditor")) {
				this.endLastEditor();
			} else if (qName.equals("created")) {
				this.endCreated();
			} else if (qName.equals("lastModified")) {
				this.endLastModified();
			} else if (qName.equals("restriction")) {
				this.endRestriction();
			}
		} catch (Throwable t) {
			_logger.error("error in end element", t);
			throw new SAXException(new Exception(t));
		}
	}

	private void startCategories(Attributes attributes, String qName) {
		// nothing to do
	}

	private void endCategories() {
		// nothing to do
	}

	private void startCategory(Attributes attributes, String qName) throws SAXException {
		String categoryCode = extractXmlAttribute(attributes, "id", qName, true);
		Category category = this.getCategoryManager().getCategory(categoryCode);
		if (null != category) {
			((Content) this.getCurrentEntity()).addCategory(category);
		}
	}

	private void endCategory() {
		// nothing to do
	}

	private void startStatus(Attributes attributes, String qName) throws SAXException {
		return; // nothing to do
	}

	private void startVersion(Attributes attributes, String qName) throws SAXException {
		return; // nothing to do
	}

	private void startLastEditor(Attributes attributes, String qName) throws SAXException {
		return; // nothing to do
	}

	private void startFirstEditor(Attributes attributes, String qName) throws SAXException {
		return; // nothing to do
	}

	private void startCreated(Attributes attributes, String qName) throws SAXException {
		return; // nothing to do
	}

	private void startLastModified(Attributes attributes, String qName) throws SAXException {
		return; // nothing to do
	}

	private void startRestriction(Attributes attributes, String qName) throws SAXException {
		return; // nothing to do
	}

	private void endStatus() {
		StringBuffer textBuffer = this.getTextBuffer();
		if (null != textBuffer) {
			((Content) this.getCurrentEntity()).setStatus(textBuffer.toString());
		}
	}

	private void endVersion() {
		StringBuffer textBuffer = this.getTextBuffer();
		if (null != textBuffer) {
			((Content) this.getCurrentEntity()).setVersion(textBuffer.toString());
		}
	}

	private void endFirstEditor() {
		StringBuffer textBuffer = this.getTextBuffer();
		if (null != textBuffer) {
			((Content) this.getCurrentEntity()).setFirstEditor(textBuffer.toString());
		}
	}

	private void endLastEditor() {
		StringBuffer textBuffer = this.getTextBuffer();
		if (null != textBuffer) {
			((Content) this.getCurrentEntity()).setLastEditor(textBuffer.toString());
		}
	}

	private void endCreated() {
		StringBuffer textBuffer = this.getTextBuffer();
		if (null != textBuffer) {
			Date date = DateConverter.parseDate(textBuffer.toString(), JacmsSystemConstants.CONTENT_METADATA_DATE_FORMAT);
			((Content) this.getCurrentEntity()).setCreated(date);
		}
	}

	private void endLastModified() {
		StringBuffer textBuffer = this.getTextBuffer();
		if (null != textBuffer) {
			Date date = DateConverter.parseDate(textBuffer.toString(), JacmsSystemConstants.CONTENT_METADATA_DATE_FORMAT);
			((Content) this.getCurrentEntity()).setLastModified(date);
		}
	}

	private void endRestriction() {
		StringBuffer textBuffer = this.getTextBuffer();
		if (null != textBuffer) {
			((Content) this.getCurrentEntity()).setRestriction(textBuffer.toString());
		}
	}

	protected ICategoryManager getCategoryManager() {
		return categoryManager;
	}
	@Autowired
	public void setCategoryManager(ICategoryManager categoryManager) {
		this.categoryManager = categoryManager;
	}

}