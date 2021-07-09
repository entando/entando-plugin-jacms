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
package org.entando.entando.plugins.jacms.aps.system.services.api.model;

import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.JAXBEntity;
import com.agiletec.aps.system.common.entity.model.attribute.JAXBBooleanAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.JAXBCompositeAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.JAXBDateAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.JAXBHypertextAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.JAXBListAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.JAXBNumberAttribute;
import com.agiletec.aps.system.common.entity.model.attribute.JAXBTextAttribute;
import com.agiletec.aps.system.services.category.Category;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SymbolicLink;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.JAXBLinkAttribute;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.JAXBLinkValue;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.JAXBResourceAttribute;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.JAXBResourceValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.entando.entando.aps.system.common.entity.model.attribute.JAXBEnumeratorMapAttribute;

/**
 * @author E.Santoboni
 */
@XmlRootElement(name = "content")
@XmlType(propOrder = {"categories", "created", "lastModified", "version", "lastEditor"})
@XmlSeeAlso({ArrayList.class, HashMap.class, JAXBBooleanAttribute.class, JAXBEnumeratorMapAttribute.class, JAXBCompositeAttribute.class, JAXBDateAttribute.class, JAXBHypertextAttribute.class, JAXBListAttribute.class, JAXBNumberAttribute.class, JAXBTextAttribute.class, JAXBResourceAttribute.class, JAXBLinkAttribute.class, JAXBResourceValue.class, JAXBLinkValue.class, SymbolicLink.class})
public class JAXBContent extends JAXBEntity implements Serializable {
    
    public JAXBContent() {
        super();
    }
    
    public JAXBContent(Content mainContent, String langCode) {
        super(mainContent, langCode);
        this.setCreated(mainContent.getCreated());
        this.setLastModified(mainContent.getLastModified());
        this.setVersion(mainContent.getVersion());
        this.setLastEditor(mainContent.getLastEditor());
        this.setCategories(mainContent.getCategories());
    }

    public IApsEntity buildEntity(Content prototype, ICategoryManager categoryManager, String langCode) {
        Content filledEntity = (Content) super.buildEntity(prototype, langCode);
        if (null != this.getCategories() && !this.getCategories().isEmpty()) {
            Iterator<String> iter = this.getCategories().iterator();
            while (iter.hasNext()) {
                String categoryCode = iter.next();
                Category category = categoryManager.getCategory(categoryCode);
                if (null != category) {
                    filledEntity.addCategory(category);
                }
            }
        }
        return filledEntity;
    }
    
    @XmlElement(name = "created", required = true)
    public Date getCreated() {
        return _created;
    }
    public void setCreated(Date created) {
        this._created = created;
    }

    @XmlElement(name = "lastModified", required = true)
    public Date getLastModified() {
        return _lastModified;
    }
    public void setLastModified(Date lastModified) {
        this._lastModified = lastModified;
    }

    @XmlElement(name = "version", required = true)
    public String getVersion() {
        return _version;
    }
    public void setVersion(String version) {
        this._version = version;
    }

    @XmlElement(name = "lastEditor", required = true)
    public String getLastEditor() {
        return _lastEditor;
    }
    public void setLastEditor(String lastEditor) {
        this._lastEditor = lastEditor;
    }

    /*
     * Return the set of codes of the additional categories.
     *
     * @return The set of codes belonging to the additional categories.
     */
    @XmlElement(name = "category", required = true)
    @XmlElementWrapper(name = "categories")
    public Set<String> getCategories() {
        return _categories;
    }

    protected void setCategories(Set<String> categories) {
        this._categories = categories;
    }

    protected void setCategories(List<Category> categories) {
        if (null != categories && categories.size() > 0) {
            this.setCategories(new HashSet<>());
            for (int i = 0; i < categories.size(); i++) {
                Category category = categories.get(i);
                if (null != category) {
                    this.getCategories().add(category.getCode());
                }
            }
        }
    }
    
    private Date _created;
    private Date _lastModified;
    private String _version;
    private String _lastEditor;

    private Set<String> _categories;
    
}