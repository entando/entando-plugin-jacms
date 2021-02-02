/*
 * Copyright 2018-Present Entando Inc. (http://www.entando.com) All rights reserved.
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

import com.agiletec.plugins.jacms.aps.system.services.resource.model.ImageResource;
import java.util.List;
import org.jdom.Element;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author E.Santoboni
 */
@ExtendWith(MockitoExtension.class)
class ResourceAttributeTest {

    @Mock
    public ImageResource resource;

    @InjectMocks
    private ImageAttribute imageAttribute;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.imageAttribute.setType("Image");
        this.imageAttribute.setName("ImageKey");
        imageAttribute.setText("Text EN", "en");
        imageAttribute.setText("Text IT", "it");
        when(resource.getId()).thenReturn("idResource");
        when(resource.getType()).thenReturn("Image");
        imageAttribute.getResources().put("en", this.resource);
    }

    @Test
    public void getJDOMElement_1() throws Exception {
        this.imageAttribute.setMetadata("metadata_key_1", "en", "metadata_value_1");
        this.imageAttribute.setMetadata("metadata_key_2", "en", "metadata_value_2");
        this.testGetJDOMElement(true);
    }

    @Test
    public void getJDOMElement_2() throws Exception {
        this.testGetJDOMElement(false);
    }

    private void testGetJDOMElement(boolean hasMetadata) throws Exception {
        Assertions.assertNotNull(imageAttribute.getResources());
        Element element = this.imageAttribute.getJDOMElement();
        Assertions.assertNotNull(element);
        Assertions.assertEquals("Image", element.getAttributeValue("attributetype"));
        Assertions.assertEquals("ImageKey", element.getAttributeValue("name"));
        List<Element> resourceElements = element.getChildren("resource");
        Assertions.assertNotNull(resourceElements);
        Assertions.assertEquals(1, resourceElements.size());
        Element resourceElement = resourceElements.get(0);
        Assertions.assertEquals("Image", resourceElement.getAttributeValue("resourcetype"));
        Assertions.assertEquals("idResource", resourceElement.getAttributeValue("id"));
        List<Element> textElements = element.getChildren("text");
        Assertions.assertEquals(2, textElements.size());
        for (Element textElement : textElements) {
            String langCode = textElement.getAttributeValue("lang");
            String text = textElement.getText();
            if (langCode.equals("en")) {
                Assertions.assertEquals("Text EN", text);
            } else if (langCode.equals("it")) {
                Assertions.assertEquals("Text IT", text);
            } else {
                Assertions.fail();
            }
        }
        Element metadatasElement = element.getChild("metadatas");
        if (hasMetadata) {
            Assertions.assertNotNull(metadatasElement);
            List<Element> metadataElements = metadatasElement.getChildren("metadata");
            Assertions.assertEquals(2, metadataElements.size());
            for (Element metadataElement : metadataElements) {
                Assertions.assertEquals("en", metadataElement.getAttributeValue("lang"));
                String key = metadataElement.getAttributeValue("key");
                String value = metadataElement.getText();
                if (key.equals("metadata_key_1")) {
                    Assertions.assertEquals("metadata_value_1", value);
                } else if (key.equals("metadata_key_2")) {
                    Assertions.assertEquals("metadata_value_2", value);
                } else {
                    Assertions.fail();
                }
            }
        } else {
            Assertions.assertNull(metadatasElement);
        }
    }

}
