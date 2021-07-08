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
package com.agiletec.plugins.jacms.aps.system.services.contentmodel.dictionary;

import java.util.List;
import java.util.Properties;

import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.IEntityModelDictionary;
import org.springframework.stereotype.Component;

@Component
public class ContentModelDictionaryProvider {

    private List<String> contentMap;
    private List<String> i18nMap;
    private List<String> infoMap;
    private List<String> commonMap;
    private Properties allowedPublicAttributeMethods;

    public List<String> getContentMap() {
        return contentMap;
    }

    public void setContentMap(List<String> contentMap) {
        this.contentMap = contentMap;
    }

    public List<String> getI18nMap() {
        return i18nMap;
    }

    public void setI18nMap(List<String> i18nMap) {
        this.i18nMap = i18nMap;
    }

    public List<String> getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(List<String> infoMap) {
        this.infoMap = infoMap;
    }

    public List<String> getCommonMap() {
        return commonMap;
    }

    public void setCommonMap(List<String> commonMap) {
        this.commonMap = commonMap;
    }

    public Properties getAllowedPublicAttributeMethods() {
        return allowedPublicAttributeMethods;
    }

    public void setAllowedPublicAttributeMethods(Properties allowedPublicAttributeMethods) {
        this.allowedPublicAttributeMethods = allowedPublicAttributeMethods;
    }

    public IEntityModelDictionary buildDictionary() {
        return buildDictionary(null);
    }

    public IEntityModelDictionary buildDictionary(IApsEntity prototype) {
        IEntityModelDictionary dictionary = new ContentModelDictionary(contentMap, i18nMap, infoMap, commonMap, allowedPublicAttributeMethods, prototype);
        return dictionary;
    }
}
