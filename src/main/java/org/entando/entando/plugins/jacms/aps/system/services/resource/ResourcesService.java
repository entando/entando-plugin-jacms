package org.entando.entando.plugins.jacms.aps.system.services.resource;

import static org.entando.entando.plugins.jacms.web.resource.ResourcesController.ERRCODE_CATEGORY_NOT_FOUND;
import static org.entando.entando.plugins.jacms.web.resource.ResourcesController.ERRCODE_GROUP_NOT_FOUND;
import static org.entando.entando.plugins.jacms.web.resource.ResourcesController.ERRCODE_INVALID_FILE_TYPE;
import static org.entando.entando.plugins.jacms.web.resource.ResourcesController.ERRCODE_INVALID_RESOURCE_TYPE;
import static org.entando.entando.plugins.jacms.web.resource.ResourcesController.ERRCODE_RESOURCE_CONFLICT;
import static org.entando.entando.plugins.jacms.web.resource.ResourcesController.ERRCODE_RESOURCE_FILTER_DATE_INVALID;
import static org.entando.entando.plugins.jacms.web.resource.ResourcesController.ERRCODE_RESOURCE_NOT_FOUND;

import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.common.model.dao.SearcherDaoPaginatedResult;
import com.agiletec.aps.system.services.authorization.Authorization;
import com.agiletec.aps.system.services.authorization.IAuthorizationManager;
import com.agiletec.aps.system.services.category.Category;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.role.Permission;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.resource.IResourceManager;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.AbstractMonoInstanceResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.AbstractResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.AttachResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.BaseResourceDataBean;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ImageResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ImageResourceDimension;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInstance;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.util.IImageDimensionReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.entando.entando.aps.system.exception.ResourceNotFoundException;
import org.entando.entando.aps.system.exception.RestServerError;
import org.entando.entando.aps.system.services.IComponentExistsService;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.plugins.jacms.web.resource.model.AssetDto;
import org.entando.entando.plugins.jacms.web.resource.model.FileAssetDto;
import org.entando.entando.plugins.jacms.web.resource.model.ImageAssetDto;
import org.entando.entando.plugins.jacms.web.resource.model.ImageMetadataDto;
import org.entando.entando.plugins.jacms.web.resource.model.ListAssetsFolderResponse;
import org.entando.entando.plugins.jacms.web.resource.request.ListResourceRequest;
import org.entando.entando.web.common.exceptions.ValidationConflictException;
import org.entando.entando.web.common.exceptions.ValidationGenericException;
import org.entando.entando.web.common.model.Filter;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.common.model.RestListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Slf4j
@Service
public class ResourcesService implements IComponentExistsService {

    @Autowired
    private IResourceManager resourceManager;

    @Autowired
    private ICategoryManager categoryManager;

    @Autowired
    private IImageDimensionReader imageDimensionManager;

    @Autowired
    private IAuthorizationManager authorizationManager;

    @Value("#{'${jacms.imageResource.allowedExtensions}'.split(',')}")
    private List<String> imageAllowedExtensions;

    @Value("#{'${jacms.attachResource.allowedExtensions}'.split(',')}")
    private List<String> fileAllowedExtensions;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");

    public PagedMetadata<AssetDto> listAssets(ListResourceRequest requestList, UserDetails user) {
        PagedMetadata<AssetDto> pagedResults = null;
        try {
            List<AssetDto> assets = new ArrayList<>();
            FieldSearchFilter[] filters = this.createSearchFilters(requestList);
            List<String> categoryFilters = this.extractCategoriesFromFilters(requestList);
            final Collection<String> allowedGroupCodes = this.getAllowedGroupCodes(user);
            SearcherDaoPaginatedResult<String> result = this.getResourceManager().getPaginatedResourcesId(filters, categoryFilters, allowedGroupCodes);
            for (String id : result.getList()) {
                AssetDto resource = convertResourceToDto(this.getResourceManager().loadResource(id));
                assets.add(resource);
            }
            SearcherDaoPaginatedResult<AssetDto> paginatedResult = new SearcherDaoPaginatedResult<>(result.getCount(), assets);
            pagedResults = new PagedMetadata<>(requestList, paginatedResult);
            pagedResults.setBody(assets);
        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.list", e);
        }
        return pagedResults;
    }
    
    private Set<String> getAllowedGroupCodes(UserDetails user) {
        Set<String> codes = new HashSet<>();
        Optional<List<Authorization>> authorizations = Optional.ofNullable(user.getAuthorizations());
        authorizations.ifPresent(list -> list.stream().filter(a -> a.getGroup() != null).forEach(a -> codes.add(a.getGroup().getName())));
        if (codes.contains(Group.ADMINS_GROUP_NAME)) {
            return new HashSet<>();
        }
        return codes;
    }

    public ListAssetsFolderResponse listAssetsFolder(String folderPath) {
        ListAssetsFolderResponse response = new ListAssetsFolderResponse();

        try {
            List<AssetDto> assets = new ArrayList<>();

            List<String> resourceIds = resourceManager
                    .searchResourcesId(createFolderPathSearchFilter(folderPath), null);

            Set<String> allFolders = new HashSet<>();

            for (String id : resourceIds) {
                AssetDto asset = convertResourceToDto(resourceManager.loadResource(id));
                if (asset.getFolderPath() != null && !asset.getFolderPath().equals(folderPath)) {
                    allFolders.add(asset.getFolderPath());
                }
                if (shouldAddAsset(folderPath, asset.getFolderPath())) {
                    assets.add(asset);
                }
            }

            response.setAssets(assets);
            response.setFolderPath(folderPath);
            response.setSubfolders(getSubfolders(folderPath, allFolders));

        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.list", e);
        }

        return response;
    }

    private boolean shouldAddAsset(String folderPath, String assetFolderPath) {
        return (assetFolderAndFolderPathIsNull(folderPath, assetFolderPath)
                || assetFolderStartsWithFolderPath(folderPath, assetFolderPath))
                && assetFolderAndFolderPathHasSameDepth(folderPath, assetFolderPath);
    }

    private boolean assetFolderStartsWithFolderPath(String folderPath, String assetFolderPath) {
        return folderPath != null && folderPath.startsWith(assetFolderPath);
    }

    private boolean assetFolderAndFolderPathIsNull(String folderPath, String assetFolderPath) {
        return folderPath == null && assetFolderPath == null;
    }

    private boolean assetFolderAndFolderPathHasSameDepth(String folderPath, String assetFolderPath) {
        return getFolderPathDepth(folderPath) == getFolderPathDepth(assetFolderPath);
    }

    private int getFolderPathDepth(String folderPath) {
        if (folderPath == null) {
            return 0;
        } else {
            return folderPath.split("/").length;
        }
    }

    private List<String> getSubfolders(String folderPath, Set<String> allFolders) {
        Set<String> result = new TreeSet<>();

        int folderPathDepth = getFolderPathDepth(folderPath);

        for (String folder : allFolders) {
            result.add(getFolderPathByDepth(folder, folderPathDepth));
        }

        return new ArrayList<>(result);
    }

    private String getFolderPathByDepth(String folder, int folderPathDepth) {
        StringBuilder sb = new StringBuilder();

        String[] folders = folder.split("/");
        int currentDepth = 0;

        while (folderPathDepth >= currentDepth && currentDepth < folders.length) {
            if (currentDepth > 0) {
                sb.append("/");
            }
            sb.append(folders[currentDepth]);
            currentDepth++;
        }

        return sb.toString();
    }

    public AssetDto createAsset(String correlationCode, String type, MultipartFile file, String group,
            List<String> categories, String folderPath, UserDetails user) {
        BaseResourceDataBean resourceFile = new BaseResourceDataBean();

        try {
            validateConflict(correlationCode);
            validateMimeType(type, file.getContentType());
            validateGroup(user, group);

            resourceFile.setInputStream(file.getInputStream());
            resourceFile.setFileSize(file.getBytes().length / 1000);
            resourceFile.setFileName(file.getOriginalFilename());
            resourceFile.setMimeType(file.getContentType());
            resourceFile.setDescr(file.getOriginalFilename());
            resourceFile.setMainGroup(group);
            resourceFile.setResourceType(convertResourceType(type));
            resourceFile.setCategories(convertCategories(categories));
            resourceFile.setOwner(user.getUsername());
            resourceFile.setFolderPath(folderPath);
            resourceFile.setCorrelationCode(correlationCode);

            ResourceInterface resource = resourceManager.addResource(resourceFile);
            return convertResourceToDto(resourceManager.loadResource(resource.getId()));
        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.list", e);
        } catch (IOException e) {
            log.error("Error reading file input stream", e);
            throw new RestServerError("plugins.jacms.resources.image.errorReadingStream", e);
        }
    }

    public AssetDto cloneAsset(String resourceId) {

        try {
            ResourceInterface clonedResource = loadResource(resourceId);
            clonedResource.setId(null);
            BaseResourceDataBean bean = createDataBeanFromResource(clonedResource);
            clonedResource = resourceManager.addResource(bean);
            return convertResourceToDto(resourceManager.loadResource(clonedResource.getId()));
        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.list", e);
        }
    }

    public AssetDto getAsset(String resourceId) {
        return getAsset(resourceId, null);
    }

    public AssetDto getAsset(String resourceId, String correlationCode) {
        try {
            ResourceInterface resource = resourceManager.loadResource(resourceId, correlationCode);
            if (resource == null) {
                throw new ResourceNotFoundException(ERRCODE_RESOURCE_NOT_FOUND, "asset", resourceId);
            }

            return convertResourceToDto(resource);
        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.get", e);
        }
    }

    public boolean exists(String code) throws EntException {
        return resourceManager.exists(null, code);
    }

    public void deleteAssetByCode(String correlationCode) {
        deleteAsset(null, correlationCode);
    }

    public void deleteAsset(String resourceId) {
        deleteAsset(resourceId, null);
    }

    public void deleteAsset(String resourceId, String correlationCode) {
        try {
            ResourceInterface resource = correlationCode == null
                    ? resourceManager.loadResource(resourceId)
                    : resourceManager.loadResource(resourceId, correlationCode);

            if (resource == null) {
                throw new ResourceNotFoundException(ERRCODE_RESOURCE_NOT_FOUND, "asset", resourceId);
            }
            resourceManager.deleteResource(resource);
        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.delete", e);
        }
    }

    private ResourceInterface loadResource(String resourceId) {
        return loadResource(resourceId, null);
    }

    private ResourceInterface loadResource(String resourceId, String correlationCode) {
        try {
            ResourceInterface resource = resourceManager.loadResource(resourceId, correlationCode);

            if (resource == null) {
                throw new ResourceNotFoundException(ERRCODE_RESOURCE_NOT_FOUND, "asset", resourceId);
            }

            return resource;
        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.persistence", e);
        }
    }

    public AssetDto editAsset(String resourceId, MultipartFile file,
            String description, List<String> categories, String folderPath) {
        //-
        return editAsset(resourceId, null, file, description, categories, folderPath);
    }

    public AssetDto editAsset(String resourceId, String correlationCode,
            MultipartFile file, String description, List<String> categories, String folderPath) {
        //-
        try {
            ResourceInterface resource = loadResource(resourceId, correlationCode);

            if (resourceId == null) {
                resourceId = resource.getId();
            }

            BaseResourceDataBean resourceFile = new BaseResourceDataBean();
            resourceFile.setResourceType(resource.getType());
            resourceFile.setResourceId(resourceId);
            resourceFile.setMetadata(resource.getMetadata());
            resourceFile.setOwner(resource.getOwner());
            resourceFile.setFolderPath(resource.getFolderPath());

            if (file != null) {
                validateMimeType(unconvertResourceType(resource.getType()), file.getContentType());

                resourceFile.setInputStream(file.getInputStream());
                resourceFile.setFileSize(file.getBytes().length / 1000);
                resourceFile.setFileName(file.getOriginalFilename());
                resourceFile.setMimeType(file.getContentType());
                resourceFile.setDescr(file.getOriginalFilename());

                if ((description == null || description.trim().length() == 0) && resource.getDescription()
                        .equals(resource.getMasterFileName())) {
                    description = resourceFile.getFileName();
                }
            }

            if (description != null && !description.trim().isEmpty()) {
                resourceFile.setDescr(description.trim());
            } else {
                resourceFile.setDescr(resource.getDescription());
            }

            if (folderPath != null) {
                resourceFile.setFolderPath(folderPath);
            }

            resourceFile.setMainGroup(resource.getMainGroup());
            resourceFile.setCategories(convertCategories(categories));

            resourceManager.updateResource(resourceFile);
            return convertResourceToDto(resourceManager.loadResource(resourceId));
        } catch (EntException e) {
            throw new RestServerError("plugins.jacms.resources.resourceManager.error.persistence", e);
        } catch (IOException e) {
            log.error("Error reading file input stream", e);
            throw new RestServerError("plugins.jacms.resources.image.errorReadingStream", e);
        }
    }

    /****** Auxiliary Methods ******/

    private BaseResourceDataBean createDataBeanFromResource(ResourceInterface resourceInterface) throws EntException {

        AbstractResource resource = (AbstractResource) resourceInterface;

        BaseResourceDataBean resourceFile = new BaseResourceDataBean();
        resourceFile.setResourceType(resource.getType());
        resourceFile.setResourceId(resource.getId());
        resourceFile.setMetadata(resource.getMetadata());
        resourceFile.setOwner(resource.getOwner());
        resourceFile.setFolderPath(resource.getFolderPath());
        resourceFile.setDescr(resource.getDescription());
        resourceFile.setCategories(resource.getCategories());
        resourceFile.setMainGroup(resource.getMainGroup());
        resourceFile.setFileName(resource.getMasterFileName());

        ResourceInstance instance = null;

        if (resource.isMultiInstance()) {
            instance = resource.getDefaultInstance();
        } else {
            instance = ((AbstractMonoInstanceResource) resource).getInstance();
        }

        try {

            boolean isProtected = resource.isProtectedResource();
            String absolutePath = null;
            if (isProtected) {
                absolutePath = resource.getFolder() + resource.getMainGroup() + "/" + instance.getFileName();
            } else {
                absolutePath = resource.getFolder() + instance.getFileName();
            }

            String filePath = resource.getStorageManager().createFullPath(absolutePath, isProtected);

            File file = new File(filePath);
            Path path = file.toPath();
            Long size = Files.size(path) / 1000;

            resourceFile.setInputStream(new FileInputStream(file));
            resourceFile.setFileSize(size.intValue());
            resourceFile.setMimeType(Files.probeContentType(path));
        } catch (IOException | EntException e) {
            throw new EntException("Error reading file input stream", e);
        }

        return resourceFile;
    }

    private List<Category> convertCategories(List<String> categories) {
        return categories.stream().map(code -> Optional.ofNullable(categoryManager.getCategory(code))
                .orElseThrow(() -> {
                    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(code, "resources.category");
                    errors.reject(ERRCODE_CATEGORY_NOT_FOUND, "plugins.jacms.category.error.notFound");
                    return new ValidationGenericException(errors);
                }))
                .collect(Collectors.toList());
    }

    public void validateGroup(UserDetails user, String group) {
        List<Group> groups = authorizationManager.getGroupsByPermission(user, Permission.MANAGE_RESOURCES);
        groups.addAll(authorizationManager.getGroupsByPermission(user, Permission.CONTENT_EDITOR));
        groups.addAll(authorizationManager.getGroupsByPermission(user, Permission.CONTENT_SUPERVISOR));
        groups = groups.stream()
                .distinct()
                .collect(Collectors.toList());

        for (Group g : groups) {
            if (g.getAuthority().equals(group)) {
                return;
            }
        }

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(group, "resources.group");
        errors.reject(ERRCODE_GROUP_NOT_FOUND, "plugins.jacms.group.error.notFound");
        throw new ValidationGenericException(errors);
    }

    public void validateConflict(String correlationCode) throws EntException {
        ResourceInterface existing = resourceManager.loadResource(null, correlationCode);
        if (existing != null) {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(correlationCode,
                    "resources.correlationCode");
            errors.reject(ERRCODE_RESOURCE_CONFLICT, "plugins.jacms.resources.error.conflict");
            throw new ValidationConflictException(errors);
        }
    }

    public void validateMimeType(String resourceType, final String mimeType) {
        String type = Optional.ofNullable(mimeType)
                .map(t -> t.split("/")[1])
                .orElseThrow(() -> {
                    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(mimeType, "resources.file.type");
                    errors.reject(ERRCODE_INVALID_FILE_TYPE, "plugins.jacms.resources.invalidMimeType");
                    return new ValidationGenericException(errors);
                });

        List<String> allowedExtensions;
        if (ImageAssetDto.RESOURCE_TYPE.equals(resourceType)) {
            allowedExtensions = imageAllowedExtensions;
        } else if (FileAssetDto.RESOURCE_TYPE.equals(resourceType)) {
            allowedExtensions = fileAllowedExtensions;
        } else {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(mimeType, "resources.file.type");
            errors.reject(ERRCODE_INVALID_RESOURCE_TYPE, "plugins.jacms.resources.invalidResourceType");
            throw new ValidationGenericException(errors);
        }

        if (!allowedExtensions.contains(type)) {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(mimeType, "resources.file.type");
            errors.reject(ERRCODE_INVALID_FILE_TYPE, "plugins.jacms.resources.invalidMimeType");
            throw new ValidationGenericException(errors);
        }
    }

    private List<ImageResourceDimension> getImageDimensions() {
        Map<Integer, ImageResourceDimension> master = imageDimensionManager.getImageDimensions();
        List<ImageResourceDimension> dimensions = new ArrayList<>(master.values());
        BeanComparator comparator = new BeanComparator("dimx");
        Collections.sort(dimensions, comparator);
        return dimensions;
    }

    private FieldSearchFilter[] createSearchFilters(ListResourceRequest requestList) {
        List<FieldSearchFilter> filters = new ArrayList<>();
        if (requestList.getType() != null) {
            filters.add(
                    new FieldSearchFilter(IResourceManager.RESOURCE_TYPE_FILTER_KEY,
                            convertResourceType(requestList.getType()), false)
            );
        }
        List<FieldSearchFilter> createdFilters = requestList.buildFieldSearchFilters();
        for (int i = 0; i < createdFilters.size(); i++) {
            FieldSearchFilter filter = createdFilters.get(i);
            String key = filter.getKey();
            if (StringUtils.isNotBlank(key)) {
                switch (key) {
                    case "name":
                        filter.setKey(IResourceManager.RESOURCE_FILENAME_FILTER_KEY);
                        break;
                    case "description":
                        filter.setKey(IResourceManager.RESOURCE_DESCR_FILTER_KEY);
                        break;
                    case "createdAt":
                        filter = this.checkDateFilter(filter);
                        filter.setKey(IResourceManager.RESOURCE_CREATION_DATE_FILTER_KEY);
                        break;
                    case "updatedAt":
                        filter = this.checkDateFilter(filter);
                        filter.setKey(IResourceManager.RESOURCE_MODIFY_DATE_FILTER_KEY);
                        break;
                    case "group":
                        filter.setKey(IResourceManager.RESOURCE_MAIN_GROUP_FILTER_KEY);
                        break;
                    case "owner":
                        filter.setKey(IResourceManager.RESOURCE_OWNER_FILTER_KEY);
                        break;
                    case "folderPath":
                        filter.setKey(IResourceManager.RESOURCE_FOLDER_PATH_FILTER_KEY);
                        break;
                    default:
                        log.warn("Invalid filter key: " + key);
                        continue;
                }
                filters.add(filter);
            } else if (filter.getOffset() != null && filter.getLimit() != null) {
                filters.add(filter);
            } else {
                log.warn("Invalid filter: key null");
            }
        }
        return filters.stream().toArray(FieldSearchFilter[]::new);
    }

    private FieldSearchFilter[] createFolderPathSearchFilter(String folderPath) {
        List<FieldSearchFilter> filters = new ArrayList<>();
        if (folderPath != null) {
            filters.add(new FieldSearchFilter(IResourceManager.RESOURCE_FOLDER_PATH_FILTER_KEY, folderPath, true));
        }
        return filters.stream().toArray(FieldSearchFilter[]::new);
    }
    
    private FieldSearchFilter checkDateFilter(FieldSearchFilter original) {
        FieldSearchFilter dateFilter = null;
        Object value = original.getValue();
        Object start = original.getStart();
        Object end = original.getEnd();
        if (null != value) {
            dateFilter = new FieldSearchFilter(original.getKey(), this.checkDate(value, original), false);
            dateFilter.setValueDateDelay(original.getValueDateDelay());
        } else if (null != start || null != end) {
            dateFilter = new FieldSearchFilter(original.getKey(), this.checkDate(start, original), this.checkDate(end, original));
            dateFilter.setStartDateDelay(original.getStartDateDelay());
            dateFilter.setEndDateDelay(original.getEndDateDelay());
        } else {
            dateFilter = new FieldSearchFilter(original.getKey());
        }
        dateFilter.setOrder(original.getOrder());
        dateFilter.setNotOption(original.isNotOption());
        dateFilter.setNullOption(original.isNullOption());
        return dateFilter;
    }
    
    private LocalDateTime checkDate(Object value, FieldSearchFilter<?> filter) {
        if (null == value || value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        try {
            return LocalDateTime.parse(value.toString(), dateFormatter);
        } catch (DateTimeParseException e) {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(filter, "resources.filter");
            errors.reject(ERRCODE_RESOURCE_FILTER_DATE_INVALID, new String[]{value.toString()},
                    "plugins.jacms.resources.invalidDateFilterFormat");
            throw new ValidationGenericException(errors);
        }
    }
    
    private List<String> extractCategoriesFromFilters(RestListRequest requestList) {
        List<String> categories = new ArrayList<>();

        for (Filter filter : Optional.ofNullable(requestList.getFilters()).orElse(new Filter[]{})) {
            if (filter.getAttribute().equals("categories")) {
                categories.add(filter.getValue());
            }
        }

        return categories;
    }

    public AssetDto convertResourceToDto(ResourceInterface resource) {
        String type = unconvertResourceType(resource.getType());

        if (ImageAssetDto.RESOURCE_TYPE.equals(type)) {
            return convertImageResourceToDto((ImageResource) resource);
        } else if (FileAssetDto.RESOURCE_TYPE.equals(type)) {
            return convertFileResourceToDto((AttachResource) resource);
        } else {
            log.error("Resource type not allowed");
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(type, "resources.file.type");
            errors.reject(ERRCODE_INVALID_RESOURCE_TYPE, "plugins.jacms.resources.invalidResourceType");
            throw new ValidationGenericException(errors);
        }
    }

    private ImageAssetDto convertImageResourceToDto(ImageResource resource) {
        ImageAssetDto.ImageAssetDtoBuilder builder = ImageAssetDto.builder()
                .id(resource.getId())
                .correlationCode(resource.getCorrelationCode())
                .name(resource.getMasterFileName())
                .description(resource.getDescription())
                .createdAt(resource.getCreationDate())
                .updatedAt(resource.getLastModified())
                .group(resource.getMainGroup())
                .categories(resource.getCategories().stream()
                        .map(Category::getCode).collect(Collectors.toList()))
                .metadata(resource.getMetadata())
                .version(ImageMetadataDto.builder()
                        .path(resource.getImagePath("0"))
                        .size(resource.getDefaultInstance().getFileLength())
                        .fileName(resource.getDefaultInstance().getFileName())
                        .build())
                .owner(resource.getOwner())
                .fileName(resource.getMasterFileName())
                .folderPath(resource.getFolderPath());

        for (ImageResourceDimension dimensions : getImageDimensions()) {
            ResourceInstance instance = resource.getInstance(dimensions.getIdDim(), null);

            if (instance == null) {
                log.warn("ResourceInstance not found for dimensions id {} and image id {}", dimensions.getIdDim(),
                        resource.getId());
                continue;
            }

            builder.version(ImageMetadataDto.builder()
                    .path(resource.getImagePath(String.valueOf(dimensions.getIdDim())))
                    .size(instance.getFileLength())
                    .fileName(instance.getFileName())
                    .dimensions(String.format("%dx%d px", dimensions.getDimx(), dimensions.getDimy()))
                    .build());
        }

        return builder.build();
    }

    private FileAssetDto convertFileResourceToDto(AttachResource resource) {
        return FileAssetDto.builder()
                .id(resource.getId())
                .correlationCode(resource.getCorrelationCode())
                .name(resource.getMasterFileName())
                .description(resource.getDescription())
                .createdAt(resource.getCreationDate())
                .updatedAt(resource.getLastModified())
                .group(resource.getMainGroup())
                .path(resource.getAttachPath())
                .size(resource.getDefaultInstance().getFileLength())
                .categories(resource.getCategories().stream()
                        .map(Category::getCode).collect(Collectors.toList()))
                .owner(resource.getOwner())
                .folderPath(resource.getFolderPath())
                .fileName(resource.getInstance().getFileName())
                .build();
    }

    public String convertResourceType(String type) {
        if (ImageAssetDto.RESOURCE_TYPE.equals(type)) {
            return "Image";
        } else if (FileAssetDto.RESOURCE_TYPE.equals(type)) {
            return "Attach";
        } else {
            throw new RestServerError(String.format("Invalid resource type: %s", type), null);
        }
    }

    public String unconvertResourceType(String resourceType) {
        if ("Image".equals(resourceType)) {
            return ImageAssetDto.RESOURCE_TYPE;
        } else if ("Attach".equals(resourceType)) {
            return FileAssetDto.RESOURCE_TYPE;
        } else {
            throw new RestServerError(String.format("Invalid resource type: %s", resourceType), null);
        }
    }

    public void setResourceManager(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
    
}
