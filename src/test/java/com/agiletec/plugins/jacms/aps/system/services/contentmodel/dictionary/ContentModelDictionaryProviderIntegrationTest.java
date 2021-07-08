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

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContentModelDictionaryProviderIntegrationTest extends BaseTestCase {
    
    @Test
    void testGetMethods() {
        IContentManager contentManager = BaseTestCase.getApplicationContext().getBean(IContentManager.class);
        ContentModelDictionaryProvider provider = BaseTestCase.getApplicationContext().getBean(ContentModelDictionaryProvider.class);
        Map<String, IApsEntity> prototypes = contentManager.getEntityPrototypes();
        prototypes.values().stream().forEach(prototype -> {
            ContentModelDictionary dictionary = (ContentModelDictionary) provider.buildDictionary(prototype);
            Map<String, Object> data = dictionary.getData();
            data.values().stream().filter(v -> v != null).forEach(v -> {
                Assertions.assertTrue(v instanceof Map);
                Map<String, Object> map = (Map) v;
                Assertions.assertTrue(map.size() > 0);
            });
        });
    }
    
    @Test
    void testGetMethods_2() {
        IContentManager contentManager = BaseTestCase.getApplicationContext().getBean(IContentManager.class);
        Map<String, IApsEntity> prototypes = contentManager.getEntityPrototypes();
        prototypes.values().stream().forEach(prototype -> {
            List<AttributeInterface> attributes = prototype.getAttributeList();
            for (int i = 0; i < attributes.size(); i++) {
                AttributeInterface attribute = attributes.get(i);
                List<String> methods = ContentModelDictionary.getAllowedAttributeMethods(attribute, null);
                Assertions.assertTrue(methods.size() > 0);
            }
        });
    }
    
}
