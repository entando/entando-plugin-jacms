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
package org.entando.entando.plugins.jacms.web.analysis;

import com.agiletec.aps.system.services.role.Permission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entando.entando.aps.system.services.IComponentExistsService;
import org.entando.entando.aps.system.services.analysis.component_existence.ComponentExistenceAnalysis;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.plugins.jacms.aps.system.services.ContentModelService;
import org.entando.entando.plugins.jacms.aps.system.services.ContentTypeService;
import org.entando.entando.plugins.jacms.aps.system.services.content.ContentService;
import org.entando.entando.plugins.jacms.aps.system.services.resource.ResourcesService;
import org.entando.entando.web.analysis.AnalysisResponse;
import org.entando.entando.web.analysis.IAnalysisController;
import org.entando.entando.web.common.annotation.RestAccessControl;
import org.entando.entando.web.common.exceptions.ValidationGenericException;
import org.entando.entando.web.common.model.SimpleRestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/analysis/cms")
public class AnalysisControllerCms implements IAnalysisController {

    private final EntLogger logger = EntLogFactory.getSanitizedLogger(this.getClass());

    /**
     * COMPONENT EXISTENCE ANALYSIS REPORT
     */
    @RestAccessControl(permission = Permission.SUPERUSER)
    @PostMapping(value = "/components/diff", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<SimpleRestResponse<AnalysisResponse>> runComponentExistenceAnalysis(
            @RequestBody Map<String, List<String>> idCodesByObjectType) {
        //-
        Set<String> objectTypes = idCodesByObjectType.keySet();
        logger.debug("Running analysis on types {}", objectTypes);
        return runComponentExistenceAnalysisDefaultImpl(
                new ComponentExistenceAnalysis(),
                idCodesByObjectType);
    }

    @Override
    public IComponentExistsService mapComponentTypeToService(String objectType) {
        try {
            switch (CmsObjectType.valueOf(objectType)) {
                case contents:
                    return contentService;
                case contentTypes:
                    return contentTypeService;
                case contentTemplates:
                    return contentModelService;
                case assets:
                    return resourcesService;
                default:
                    throw new IllegalStateException("Unexpected value: " + objectType);
            }
        } catch (IllegalArgumentException ex) {
            logger.debug("Illegal objectType value detected: {}", objectType);
            MapBindingResult bindingResult = new MapBindingResult(new HashMap<>(), "analysis.Component.Diff");
            bindingResult.rejectValue("objectType", "1", new String[]{objectType}, "generic.notValid");
            throw new ValidationGenericException(bindingResult);
        }
    }

    public enum CmsObjectType {
        contents, contentTypes, contentTemplates, assets,    //NOSONAR
    }


    @Autowired
    ContentService contentService;
    @Autowired
    ContentTypeService contentTypeService;
    @Autowired
    ContentModelService contentModelService;
    @Autowired
    ResourcesService resourcesService;
}
