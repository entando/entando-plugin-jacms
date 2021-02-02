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
package com.agiletec.plugins.jacms.aps.system.services.content.parse.attribute;

import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.ImageAttribute;
import com.agiletec.plugins.jacms.aps.system.services.resource.IResourceManager;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ImageResource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author E.Santoboni
 */
@ExtendWith(MockitoExtension.class)
class ResourceAttributeHandlerTest {

    @Mock
    public Attributes attributes;

    @Mock
    public ImageAttribute currentAttr;

    @Spy
    public IResourceManager resourceManager;

    @InjectMocks
    private ResourceAttributeHandler handler;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void startAttribute_1() throws SAXException {
        handler.setIntoMetadatas(true);
        when(attributes.getIndex(ArgumentMatchers.anyString())).thenReturn(1);
        when(attributes.getValue(ArgumentMatchers.anyInt())).thenReturn(null);
        Assertions.assertThrows(SAXException.class, () -> {
            handler.startAttribute(attributes, "metadata");
        });
    }

    @Test
    public void startAttribute_2() throws SAXException {
        handler.setIntoMetadatas(true);
        when(attributes.getIndex("lang")).thenReturn(1);
        when(attributes.getValue(1)).thenReturn("en");
        when(attributes.getIndex("key")).thenReturn(2);
        when(attributes.getValue(2)).thenReturn("test_key");
        handler.startAttribute(attributes, "metadata");
        Assertions.assertEquals("test_key", handler.getMetadataKey());
        Assertions.assertEquals("en", handler.getCurrentLangId());
    }

    @Test
    public void startAttribute_3() throws Exception {
        when(attributes.getIndex("lang")).thenReturn(1);
        when(attributes.getValue(1)).thenReturn("en");
        when(attributes.getIndex("id")).thenReturn(2);
        when(attributes.getValue(2)).thenReturn("id_1");
        ImageResource resource = new ImageResource();
        when(resourceManager.loadResource("id_1")).thenReturn(resource);
        handler.startAttribute(attributes, "resource");
        Mockito.verify(currentAttr, Mockito.times(1)).setResource(ArgumentMatchers.any(ImageResource.class), ArgumentMatchers.eq("en"));
    }

    @Test
    public void startAttribute_4() throws Exception {
        when(attributes.getIndex("lang")).thenReturn(1);
        when(attributes.getValue(1)).thenReturn(null);
        when(attributes.getIndex("id")).thenReturn(2);
        when(attributes.getValue(2)).thenReturn("id_2");
        when(resourceManager.loadResource("id_2")).thenReturn(null);
        handler.startAttribute(attributes, "resource");
        Mockito.verify(currentAttr, Mockito.times(0)).setResource(ArgumentMatchers.any(ImageResource.class), ArgumentMatchers.eq("en"));
    }

    @Test
    public void startAttribute_5() throws Exception {
        Assertions.assertFalse(handler.isIntoMetadatas());
        handler.startAttribute(attributes, "metadatas");
        Assertions.assertTrue(handler.isIntoMetadatas());
    }

    @Test
    public void startAttribute_6() throws SAXException {
        when(attributes.getIndex("lang")).thenReturn(1);
        when(attributes.getValue(1)).thenReturn("it");
        handler.startAttribute(attributes, IResourceManager.LEGEND_METADATA_KEY);
        Assertions.assertEquals(IResourceManager.LEGEND_METADATA_KEY, handler.getMetadataKey());
        Assertions.assertEquals("it", handler.getCurrentLangId());
    }

    @Test
    public void startAttribute_7() throws SAXException {
        when(attributes.getIndex("lang")).thenReturn(1);
        when(attributes.getValue(1)).thenReturn(null);
        Assertions.assertThrows(SAXException.class, () -> {
            handler.startAttribute(attributes, "text");
        });
    }

    @Test
    public void startAttribute_8() throws SAXException {
        when(attributes.getIndex("lang")).thenReturn(2);
        when(attributes.getValue(2)).thenReturn("en");
        handler.startAttribute(attributes, "text");
        Assertions.assertEquals("en", handler.getCurrentLangId());
    }

    @Test
    public void endAttribute_1() throws SAXException {
        handler.setIntoMetadatas(true);
        handler.setMetadataKey("key_1");
        handler.setCurrentLangId("en");
        StringBuffer buffer = new StringBuffer("value");
        handler.endAttribute("metadata", buffer);
        Mockito.verify(currentAttr, Mockito.times(1)).setMetadata(ArgumentMatchers.eq("key_1"),
                ArgumentMatchers.eq("en"), ArgumentMatchers.eq("value"));
        Assertions.assertNull(handler.getCurrentLangId());
        Assertions.assertNull(handler.getMetadataKey());
    }

    @Test
    public void endAttribute_3() throws SAXException {
        handler.endAttribute("resource", null);
        Mockito.verify(currentAttr, Mockito.times(0)).setMetadata(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.verify(currentAttr, Mockito.times(0)).setResource(ArgumentMatchers.any(ImageResource.class), ArgumentMatchers.anyString());
    }

    @Test
    public void endAttribute_4() throws SAXException {
        handler.setCurrentLangId("dr");
        StringBuffer buffer = new StringBuffer("value");
        handler.endAttribute(IResourceManager.ALT_METADATA_KEY, buffer);
        Mockito.verify(currentAttr, Mockito.times(1)).setMetadata(ArgumentMatchers.eq(IResourceManager.ALT_METADATA_KEY),
                ArgumentMatchers.eq("dr"), ArgumentMatchers.eq("value"));
        Assertions.assertNull(handler.getCurrentLangId());
        Assertions.assertNull(handler.getMetadataKey());
    }

    @Test
    public void endAttribute_5() throws SAXException {
        handler.setCurrentLangId("pl");
        StringBuffer buffer = new StringBuffer("new text");
        handler.endAttribute("text", buffer);
        Mockito.verify(currentAttr, Mockito.times(1)).setText(ArgumentMatchers.eq("new text"), ArgumentMatchers.eq("pl"));
        Assertions.assertNull(handler.getCurrentLangId());
    }

}
