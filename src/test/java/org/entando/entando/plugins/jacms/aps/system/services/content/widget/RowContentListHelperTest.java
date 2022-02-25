/*
 * Copyright 2022-Present Entando Inc. (http://www.entando.com) All rights reserved.
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
package org.entando.entando.plugins.jacms.aps.system.services.content.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author E.Santoboni
 */
public class RowContentListHelperTest {
    
    @Test
    public void testWidgetParameter() {
        List<Properties> contents = new ArrayList<>();
        Properties prop1 = new Properties();
        prop1.put("contentId", "ART34");
        prop1.put("modelId", "10012");
        contents.add(prop1);
        String parameterString = RowContentListHelper.fromContentsToParameter(contents);
        List<Properties> contentsExtracted = RowContentListHelper.fromParameterToContents(parameterString);
        Assertions.assertEquals(1, contentsExtracted.size());
        Assertions.assertEquals(2, contentsExtracted.get(0).size());
        Assertions.assertEquals("ART34", contentsExtracted.get(0).get("contentId"));
        Assertions.assertEquals("10012", contentsExtracted.get(0).get("modelId"));
        
        Properties prop2 = new Properties();
        prop2.put("contentId", "NEW56");
        prop2.put("modelId", "20012");
        contentsExtracted.add(prop2);
        parameterString = RowContentListHelper.fromContentsToParameter(contentsExtracted);
        
        contentsExtracted = RowContentListHelper.fromParameterToContents(parameterString);
        Assertions.assertEquals(2, contentsExtracted.size());
        Assertions.assertEquals(2, contentsExtracted.get(0).size());
        Assertions.assertEquals("ART34", contentsExtracted.get(0).get("contentId"));
        Assertions.assertEquals("10012", contentsExtracted.get(0).get("modelId"));
        Assertions.assertEquals(2, contentsExtracted.get(1).size());
        Assertions.assertEquals("NEW56", contentsExtracted.get(1).get("contentId"));
        Assertions.assertEquals("20012", contentsExtracted.get(1).get("modelId"));
        
        Properties prop3 = new Properties();
        prop3.put("contentId", "ALL756");
        prop3.put("modelId", "30012");
        contentsExtracted.add(prop3);
        parameterString = RowContentListHelper.fromContentsToParameter(contentsExtracted);
        contentsExtracted = RowContentListHelper.fromParameterToContents(parameterString);
        Assertions.assertEquals(3, contentsExtracted.size());
        Assertions.assertEquals(2, contentsExtracted.get(0).size());
        Assertions.assertEquals("ART34", contentsExtracted.get(0).get("contentId"));
        Assertions.assertEquals("10012", contentsExtracted.get(0).get("modelId"));
        Assertions.assertEquals(2, contentsExtracted.get(1).size());
        Assertions.assertEquals("NEW56", contentsExtracted.get(1).get("contentId"));
        Assertions.assertEquals("20012", contentsExtracted.get(1).get("modelId"));
        Assertions.assertEquals(2, contentsExtracted.get(2).size());
        Assertions.assertEquals("ALL756", contentsExtracted.get(2).get("contentId"));
        Assertions.assertEquals("30012", contentsExtracted.get(2).get("modelId"));
        
        Properties prop4 = new Properties();
        prop4.put("contentId", "GEN756");
        prop4.put("modelId", "40072");
        contentsExtracted.add(prop4);
        parameterString = RowContentListHelper.fromContentsToParameter(contentsExtracted);
        contentsExtracted = RowContentListHelper.fromParameterToContents(parameterString);
        Assertions.assertEquals(4, contentsExtracted.size());
        Assertions.assertEquals(2, contentsExtracted.get(0).size());
        Assertions.assertEquals("ART34", contentsExtracted.get(0).get("contentId"));
        Assertions.assertEquals("10012", contentsExtracted.get(0).get("modelId"));
        Assertions.assertEquals(2, contentsExtracted.get(1).size());
        Assertions.assertEquals("NEW56", contentsExtracted.get(1).get("contentId"));
        Assertions.assertEquals("20012", contentsExtracted.get(1).get("modelId"));
        Assertions.assertEquals(2, contentsExtracted.get(2).size());
        Assertions.assertEquals("ALL756", contentsExtracted.get(2).get("contentId"));
        Assertions.assertEquals("30012", contentsExtracted.get(2).get("modelId"));
        Assertions.assertEquals(2, contentsExtracted.get(3).size());
        Assertions.assertEquals("GEN756", contentsExtracted.get(3).get("contentId"));
        Assertions.assertEquals("40072", contentsExtracted.get(3).get("modelId"));
        
    }
    
}
