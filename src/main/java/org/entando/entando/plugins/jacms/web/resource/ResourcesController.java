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
package org.entando.entando.plugins.jacms.web.resource;

import com.agiletec.aps.system.services.role.Permission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.entando.entando.aps.system.exception.ResourceNotFoundException;
import org.entando.entando.aps.util.HttpSessionHelper;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.plugins.jacms.aps.system.services.resource.ResourcesService;
import org.entando.entando.plugins.jacms.web.resource.model.AssetDto;
import org.entando.entando.plugins.jacms.web.resource.model.ListAssetsFolderResponse;
import org.entando.entando.plugins.jacms.web.resource.request.CreateResourceRequest;
import org.entando.entando.plugins.jacms.web.resource.request.ListResourceRequest;
import org.entando.entando.plugins.jacms.web.resource.request.UpdateResourceRequest;
import org.entando.entando.plugins.jacms.web.resource.validator.ResourcesValidator;
import org.entando.entando.web.common.annotation.RestAccessControl;
import org.entando.entando.web.common.exceptions.ResourcePermissionsException;
import org.entando.entando.web.common.exceptions.ValidationGenericException;
import org.entando.entando.web.common.model.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static org.entando.entando.aps.util.HttpSessionHelper.extractCurrentUser;

@RequiredArgsConstructor
@RestController
public class ResourcesController {
    public static final String ERRCODE_RESOURCE_NOT_FOUND = "1";
    public static final String ERRCODE_CATEGORY_NOT_FOUND = "2";
    public static final String ERRCODE_GROUP_NOT_FOUND = "3";
    public static final String ERRCODE_INVALID_FILE_TYPE = "4";
    public static final String ERRCODE_INVALID_RESOURCE_TYPE = "5";
    public static final String ERRCODE_RESOURCE_FORBIDDEN = "6";
    public static final String ERRCODE_RESOURCE_CONFLICT = "7";
    public static final String ERRCODE_RESOURCE_FILTER_DATE_INVALID = "8";
    public static final String ID_NAME_OF_CORRELATION_CODE = "cc";

    private final EntLogger logger = EntLogFactory.getSanitizedLogger(getClass());

    @NonNull private final ResourcesService service;
    @NonNull private final ResourcesValidator resourceValidator;
    @NonNull private final HttpSession httpSession;

    @ApiOperation(value = "LIST Resources", nickname = "listResources", tags = {"resources-controller"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 401, message = "Unauthorized")})
    @GetMapping("/plugins/cms/assets")
    @RestAccessControl(permission = {Permission.MANAGE_RESOURCES, Permission.CONTENT_SUPERVISOR, Permission.CONTENT_EDITOR})
    public ResponseEntity<PagedRestResponse<AssetDto>> listAssets(ListResourceRequest requestList) {
        logger.debug("REST request - list image resources");

        resourceValidator.validateRestListRequest(requestList, AssetDto.class);
        PagedMetadata<AssetDto> result = service.listAssets(requestList,HttpSessionHelper.extractCurrentUser(httpSession));
        resourceValidator.validateRestListResult(requestList, result);
        return ResponseEntity.ok(new PagedRestResponse<>(result));
    }

    @ApiOperation(value = "LIST Resources", nickname = "listResources", tags = {"resources-controller"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 401, message = "Unauthorized")})
    @GetMapping("/plugins/cms/assets/folder")
    @RestAccessControl(permission = {Permission.MANAGE_RESOURCES, Permission.CONTENT_SUPERVISOR, Permission.CONTENT_EDITOR})
    public ResponseEntity<RestResponse<List<AssetDto>, Map<String, Object>>> listAssetsFolder(
            @RequestParam(value = "folderPath", required = false) String folderPath) {
        logger.debug("REST request - list resources folder");

        folderPath = sanitizeFolderPath(folderPath);

        ListAssetsFolderResponse result = service.listAssetsFolder(folderPath);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("folderPath", result.getFolderPath());
        metadata.put("subfolders", result.getSubfolders());

        return ResponseEntity.ok(new RestResponse<>(result.getAssets(), metadata));
    }

    private String sanitizeFolderPath(String folderPath) {

        if (folderPath == null) {
            return null;
        }

        String result = folderPath.trim().replaceAll("/+", "/");

        if (result.equals("/") || result.equals("")) {
            return null;
        }

        if (result.startsWith("/")) {
            result = result.substring(1);
        }

        if (result.endsWith("/")) {
            result = result.substring(0, result.length()-1);
        }

        return result;
    }

    @ApiOperation(value = "CREATE Resource", nickname = "createResource", tags = {"resources-controller"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized")})
    @PostMapping(value = "/plugins/cms/assets")
    @RestAccessControl(permission = {Permission.MANAGE_RESOURCES, Permission.CONTENT_SUPERVISOR, Permission.CONTENT_EDITOR})
    public ResponseEntity<SimpleRestResponse<AssetDto>> createAsset(
            @RequestParam(value = "metadata") String request,
            @RequestParam(value = "file") MultipartFile file) throws JsonProcessingException {
        logger.debug("REST request - create new resource");

        CreateResourceRequest resourceRequest = new ObjectMapper().readValue(request, CreateResourceRequest.class);

        List<String> categoriesList = Optional.ofNullable(resourceRequest.getCategories()).orElse(Collections.emptyList())
                .stream()
                .map(String::trim)
                .filter(c -> c.length() > 0)
                .collect(Collectors.toList());

        AssetDto result = service
                .createAsset(resourceRequest.getCorrelationCode(), resourceRequest.getType(), file, resourceRequest.getGroup(), categoriesList, resourceRequest.getFolderPath(),
                        extractCurrentUser(httpSession));
        return ResponseEntity.ok(new SimpleRestResponse<>(result));
    }

    @ApiOperation(value = "CLONE Resource", nickname = "cloneResource", tags = {"resources-controller"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized")})
    @PostMapping("/plugins/cms/assets/{resourceId}/clone")
    @RestAccessControl(permission = {Permission.MANAGE_RESOURCES, Permission.CONTENT_SUPERVISOR, Permission.CONTENT_EDITOR})
    public ResponseEntity<SimpleRestResponse<AssetDto>> cloneAsset(@PathVariable("resourceId") String resourceId) {
        logger.debug("REST request - clone resource");
        AssetDto result = service.cloneAsset(resourceId);
        return ResponseEntity.ok(new SimpleRestResponse<>(result));
    }

    @ApiOperation(value = "EDIT Resource", nickname = "editResource", tags = {"resources-controller"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized")})
    @PostMapping(value = "/plugins/cms/assets/{resourceIdOrCC}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RestAccessControl(permission = {Permission.MANAGE_RESOURCES, Permission.CONTENT_SUPERVISOR, Permission.CONTENT_EDITOR})

    public ResponseEntity<SimpleRestResponse<AssetDto>> editAsset(
            @PathVariable("resourceIdOrCC") RestNamedId resourceIdOrCC,
            @RequestParam(value = "metadata") String request,
            @RequestParam(value = "file", required = false) MultipartFile file) throws JsonProcessingException {
        //-
        logger.debug("REST request - edit image resource with id {}", resourceIdOrCC);

        String correlationCode = resourceIdOrCC.getValidValue(ID_NAME_OF_CORRELATION_CODE).orElse(null);
        String resourceId = correlationCode == null ? resourceIdOrCC.value : null;

        UpdateResourceRequest resourceRequest = new ObjectMapper().readValue(request, UpdateResourceRequest.class);

        List<String> categoriesList = Optional.ofNullable(resourceRequest.getCategories())
                .orElse(Collections.emptyList())
                .stream()
                .map(String::trim)
                .filter(c -> c.length() > 0)
                .collect(Collectors.toList());

        AssetDto result = service.editAsset(resourceId, correlationCode, file,
                resourceRequest.getDescription(), categoriesList, sanitizeFolderPath(resourceRequest.getFolderPath()));

        return ResponseEntity.ok(new SimpleRestResponse<>(result));
    }

    @ApiOperation(value = "DELETE Resource", nickname = "deleteResource", tags = {"resources-controller"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")})
    @DeleteMapping("/plugins/cms/assets/{resourceIdOrCC}")
    @RestAccessControl(permission = {Permission.MANAGE_RESOURCES, Permission.CONTENT_SUPERVISOR, Permission.CONTENT_EDITOR})

    public ResponseEntity<SimpleRestResponse<Map<String, String>>> deleteAsset(
            @PathVariable("resourceIdOrCC") RestNamedId resourceIdOrCC)
            throws EntException {
        //-
        logger.debug("REST request - delete resource with id {}", resourceIdOrCC);
        String correlationCode = resourceIdOrCC.getValidValue(ID_NAME_OF_CORRELATION_CODE).orElse(null);
        String resourceId = correlationCode == null ? resourceIdOrCC.value : null;

        if (!resourceValidator.resourceExists(resourceId, correlationCode)) {
            throw new ResourceNotFoundException(ERRCODE_RESOURCE_NOT_FOUND, "asset", resourceIdOrCC.toString());
        }
        DataBinder binder = new DataBinder(resourceId);
        BindingResult bindingResult = binder.getBindingResult();

        if (!resourceValidator.isResourceDeletableByUser(resourceId, correlationCode, extractCurrentUser(httpSession))) {
            bindingResult.reject(ERRCODE_RESOURCE_FORBIDDEN, new String[]{resourceIdOrCC.value}, "plugins.jacms.resources.resourceManager.error.delete");
            throw new ResourcePermissionsException(bindingResult);
        }

        resourceValidator.resourceReferencesValidation(resourceId, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ValidationGenericException(bindingResult);
        }

        service.deleteAsset(resourceId, correlationCode);
        return ResponseEntity.ok(new SimpleRestResponse<>(new HashMap()));
    }
}
