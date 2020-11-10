/*
 * Copyright 2020-Present Entando Inc. (http://www.entando.com) All rights reserved.
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
package com.agiletec.plugins.jacms.aps.system.services.searchengine;

import java.util.List;
import org.entando.entando.aps.system.services.searchengine.SearchEngineFilter;

public class NumericSearchEngineFilter extends SearchEngineFilter<Number> {

    public NumericSearchEngineFilter(String key, boolean attributeFilter) {
        super(key, attributeFilter);
    }
    
    public NumericSearchEngineFilter(String key, boolean attributeFilter, Number value) {
        super(key, attributeFilter, value);
    }
    
    public static NumericSearchEngineFilter createAllowedValuesFilter(String key, boolean attributeFilter, List<Number> allowedValues) {
        NumericSearchEngineFilter filter = new NumericSearchEngineFilter(key, attributeFilter);
        filter.setAllowedValues(allowedValues);
        return filter;
    }

    public static NumericSearchEngineFilter createRangeFilter(String key, boolean attributeFilter, Number start, Number end) {
        NumericSearchEngineFilter filter = new NumericSearchEngineFilter(key, attributeFilter);
        filter.setStart(start);
        filter.setEnd(end);
        return filter;
    }
    
}
