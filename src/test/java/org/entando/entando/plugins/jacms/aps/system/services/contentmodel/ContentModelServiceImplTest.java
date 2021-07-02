package org.entando.entando.plugins.jacms.aps.system.services.contentmodel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.common.entity.model.SmallEntityType;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SmallContentType;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.IContentModelManager;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.dictionary.ContentModelDictionaryProvider;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentModelDto;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentModelReference;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.IEntityModelDictionary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.assertj.core.api.Condition;
import org.entando.entando.aps.system.exception.ResourceNotFoundException;
import org.entando.entando.plugins.jacms.aps.system.services.ContentModelServiceImpl;
import org.entando.entando.plugins.jacms.web.contentmodel.model.ContentModelReferenceDTO;
import org.entando.entando.plugins.jacms.web.contentmodel.validator.ContentModelValidator;
import org.entando.entando.web.common.exceptions.ValidationConflictException;
import org.entando.entando.web.common.model.Filter;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.common.model.RestListRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.ObjectError;

@ExtendWith(MockitoExtension.class)
class ContentModelServiceImplTest {

    @Mock
    private IContentManager contentManager;

    @Mock
    private IContentModelManager contentModelManager;

    @Spy
    private ContentModelDictionaryProvider dictionaryProvider;

    @InjectMocks
    private ContentModelServiceImpl contentModelService;

    private Map<Long, ContentModel> mockedContentModels;
    private Map<String, SmallContentType> mockedContentTypes;
    private List<SmallEntityType> mockedEntityTypes;

    @BeforeEach
    public void setUp() throws Exception {
        fillMockedContentModelsMap();
        fillMockedContentTypesMap();
        fillMockedEntityTypes();
        Mockito.lenient().when(contentModelManager.getContentModel(anyLong()))
                .thenAnswer(invocation -> mockedContentModels.get(invocation.getArgument(0)));
        Mockito.lenient().when(contentModelManager.getContentModels()).thenReturn(new ArrayList<>(mockedContentModels.values()));

        Mockito.lenient().when(contentManager.getSmallContentTypesMap()).thenReturn(mockedContentTypes);

        Mockito.lenient().when(contentModelManager.getContentModelReferences(1L, true))
                .thenReturn(Collections.singletonList(new ContentModelReference()));

        Mockito.lenient().when(contentModelManager.getContentModelReferences(1L, false))
                .thenReturn(Collections.singletonList(new ContentModelReference()));

        dictionaryProvider.setContentMap(new ArrayList<>());
        dictionaryProvider.setI18nMap(new ArrayList<>());
        dictionaryProvider.setInfoMap(new ArrayList<>());
        dictionaryProvider.setCommonMap(new ArrayList<>());
        dictionaryProvider.setAllowedPublicAttributeMethods(new Properties());
    }

    @Test
    public void findManyShouldFindAll() {
        RestListRequest request = new RestListRequest();
        PagedMetadata<ContentModelDto> result = contentModelService.findMany(request);
        assertThat(result.getBody()).isNotNull().hasSize(3);
    }
    
    @Test
    public void findManyShouldFilter() {
        RestListRequest request = new RestListRequest();
        String contentType = "AAA";
        Filter filter = new Filter("contentType", contentType);
        request.addFilter(filter);
        PagedMetadata<ContentModelDto> result = contentModelService.findMany(request);
        assertThat(result.getBody()).isNotNull().hasSize(1);
        assertThat(result.getBody().get(0).getContentType()).isEqualTo(contentType);
    }

    @Test
    public void findManyShouldSort() {
        RestListRequest req = new RestListRequest();
        req.setSort("contentType");
        req.setDirection(FieldSearchFilter.DESC_ORDER);
        PagedMetadata<ContentModelDto> res = contentModelService.findMany(req);
        assertThat(res.getBody()).isNotNull().hasSize(3);
        assertThat(res.getBody().stream().map(ContentModelDto::getContentType))
                .containsExactly("CCC", "BBB", "AAA");
    }

    @Test
    public void shouldFindOne() {
        assertThat(contentModelService.getContentModel(1L)).isNotNull();
    }

    @Test
    public void shouldFailWithNotFound() {
        ResourceNotFoundException ex = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            contentModelService.getContentModel(20L);
        });
        assertThat(ex.getErrorCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_NOT_FOUND);
    }

    @Test
    public void shouldFindOneUsingOptional() {
        long id = 1L;
        Optional<ContentModelDto> maybeResult = contentModelService.findById(id);
        assertThat(maybeResult).isPresent();

        Condition<ContentModelDto> hasIdExpected = new Condition<>(
                model -> model.getId() == id, "id equal"
        );

        assertThat(maybeResult).hasValueSatisfying(hasIdExpected);
    }

    @Test
    public void shouldFindNothingUsingOptional() {
        Optional<ContentModelDto> result = contentModelService.findById(20L);
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldCreateContentModel() {
        String expectedShape = "<script nonce=\"$content.nonce\">my_js_script</script>";

        ContentModelDto contentModelToCreate = new ContentModelDto();
        contentModelToCreate.setContentType("AAA");
        contentModelToCreate.setId(4L);
        contentModelToCreate.setContentShape("<script>my_js_script</script>");

        ContentModelDto result = contentModelService.create(contentModelToCreate);
        assertThat(result.getId()).isEqualTo(contentModelToCreate.getId());
        assertThat(result.getContentType()).isEqualTo(contentModelToCreate.getContentType());
        assertThat(result.getContentShape()).isEqualTo(expectedShape);
    }

    @Test
    public void shouldCreateContentModelNonceAlreadyAdded() {
        String expectedShape = "<script nonce=\"$content.nonce\">my_js_script</script>";

        ContentModelDto contentModelToCreate = new ContentModelDto();
        contentModelToCreate.setContentType("AAA");
        contentModelToCreate.setId(4L);
        contentModelToCreate.setContentShape(expectedShape);

        ContentModelDto result = contentModelService.create(contentModelToCreate);
        assertThat(result.getId()).isEqualTo(contentModelToCreate.getId());
        assertThat(result.getContentType()).isEqualTo(contentModelToCreate.getContentType());

        //Nothing changed
        assertThat(result.getContentShape()).isEqualTo(expectedShape);
    }

    @Test
    public void shouldFailCreatingContentModel() {
        ContentModelDto contentModelToCreate = new ContentModelDto();
        // Existing content model id
        long id = 1L;
        // Content type not found
        contentModelToCreate.setContentType("XXX");
        contentModelToCreate.setId(id);
        // Wrong utilizer
        SmallContentType utilizer = new SmallContentType();
        utilizer.setCode("DEF");
        when(contentModelManager.getDefaultUtilizer(id)).thenReturn(utilizer);
        ValidationConflictException ex = Assertions.assertThrows(ValidationConflictException.class, () -> {
            contentModelService.create(contentModelToCreate);
        });
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        assertThat(errors).isNotNull().hasSize(3);
        assertThat(errors.stream().map(e -> e.getCode()))
                .containsExactlyInAnyOrder(
                        ContentModelValidator.ERRCODE_CONTENTMODEL_ALREADY_EXISTS,
                        ContentModelValidator.ERRCODE_CONTENTMODEL_TYPECODE_NOT_FOUND,
                        ContentModelValidator.ERRCODE_CONTENTMODEL_WRONG_UTILIZER
                );
    }

    @Test
    public void shouldUpdateContentModel() {
        String expectedShape = "<script nonce=\"$content.nonce\">my_js_script</script>";

        long id = 1L;
        ContentModelDto contentModelToUpdate = new ContentModelDto();
        contentModelToUpdate.setId(id);
        contentModelToUpdate.setContentType("AAA");
        contentModelToUpdate.setContentShape("<script>my_js_script</script>");

        String updatedDescription = "test description";
        String updatedContentType = "BBB";
        contentModelToUpdate.setDescr(updatedDescription);
        contentModelToUpdate.setContentType(updatedContentType);

        ContentModel contentModel = this.mockedContentModels.get(id);
        contentModel.setDescription(updatedDescription);
        contentModel.setContentType(updatedContentType);

        ContentModelDto result = contentModelService.update(contentModelToUpdate);
        assertThat(result.getDescr()).isEqualTo(updatedDescription);
        assertThat(result.getContentType()).isEqualTo(updatedContentType);
        assertThat(result.getContentShape()).isEqualTo(expectedShape);
    }

    @Test
    public void shouldFailUpdatingContentModelBecauseNotFound() {
        long id = 20L; // inexistent content model
        ContentModelDto contentModelToUpdate = new ContentModelDto();
        contentModelToUpdate.setId(id);
        ResourceNotFoundException ex = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            contentModelService.update(contentModelToUpdate);
        });
        assertThat(ex.getErrorCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_NOT_FOUND);
    }

    @Test
    public void shouldFailUpdatingContentModelBecauseContentTypeNotFound() {
        long id = 3L;
        ContentModelDto contentModelToUpdate = new ContentModelDto();
        contentModelToUpdate.setId(id);
        contentModelToUpdate.setContentType("CCC");
        ValidationConflictException ex = Assertions.assertThrows(ValidationConflictException.class, () -> {
            contentModelService.update(contentModelToUpdate);
        });
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        assertThat(errors).isNotNull().hasSize(1);
        assertThat(errors.get(0).getCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_TYPECODE_NOT_FOUND);
    }

    @Test
    public void shoudlDeleteContentModel() {
        contentModelService.delete(2L);
    }

    @Test
    public void shoudlFailDeletingContentModel() {
        ValidationConflictException ex = Assertions.assertThrows(ValidationConflictException.class, () -> {
            contentModelService.delete(1L);
        });
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        assertThat(errors).isNotNull().hasSize(1);
        assertThat(errors.get(0).getCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_REFERENCES);
    }

    @Test
    public void shoudlFailDeletingContentModelWithDefaultModelTemplate() {
        Mockito.lenient().when(contentManager.getSmallEntityTypes()).thenReturn(mockedEntityTypes);
        Mockito.lenient().when(contentManager.getDefaultModel("BBB")).thenReturn("2");
        ValidationConflictException ex = Assertions.assertThrows(ValidationConflictException.class, () -> {
            contentModelService.delete(2L);
        });
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        assertThat(errors).isNotNull().hasSize(1);
        assertThat(errors.get(0).getCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_METADATA_REFERENCES);
    }

    @Test
    public void shoudlFailDeletingContentModelWithDefaultModelListTemplate() {
        Mockito.lenient().when(contentManager.getSmallEntityTypes()).thenReturn(mockedEntityTypes);
        Mockito.lenient().when(contentManager.getListModel("BBB")).thenReturn("2");
        ValidationConflictException ex = Assertions.assertThrows(ValidationConflictException.class, () -> {
            contentModelService.delete(2L);
        });
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        assertThat(errors).isNotNull().hasSize(1);
        assertThat(errors.get(0).getCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_METADATA_REFERENCES);
    }

    @Test
    public void shouldReturnReferences() {
        RestListRequest restListRequest =  new RestListRequest();
        final PagedMetadata<ContentModelReferenceDTO> contentModelReferences = contentModelService
                .getContentModelReferences(1L, restListRequest);
        assertThat(contentModelReferences).isNotNull();
        assertThat(contentModelReferences.getTotalItems()).isEqualTo(1);
    }

    @Test
    public void shouldFailReturningReferences() {
        RestListRequest restListRequest = new RestListRequest();
        ResourceNotFoundException ex = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            contentModelService.getContentModelReferences(20L, restListRequest);
        });
        assertThat(ex.getErrorCode()).isEqualTo(ContentModelValidator.ERRCODE_CONTENTMODEL_NOT_FOUND);
    }

    @Test
    public void shouldReturnDictionary() {
        Content contentPrototype = new Content();
        contentPrototype.setTypeCode("AAA");
        when(this.contentManager.getEntityPrototype("AAA")).thenReturn(contentPrototype);
        IEntityModelDictionary dictionary = contentModelService.getContentModelDictionary("AAA");
        assertThat(dictionary).isNotNull();
    }

    @Test
    public void getContentModelUsageForNonExistingCodeShouldReturnZero() {

        int componentUsage = contentModelService.getComponentUsage(5000L).getUsage();
        assertEquals(0, componentUsage);
    }
    
    private void fillMockedContentModelsMap() {
        this.mockedContentModels = new HashMap<>();
        addMockedContentModel(1L, "AAA");
        addMockedContentModel(2L, "BBB");
        addMockedContentModel(3L, "CCC");
    }

    private void addMockedContentModel(long id, String contentType) {
        ContentModel contentModel = new ContentModel();
        contentModel.setId(id);
        contentModel.setContentType(contentType);
        contentModel.setDescription("description");
        this.mockedContentModels.put(id, contentModel);
    }

    private void fillMockedContentTypesMap() {
        this.mockedContentTypes = new HashMap<>();
        addMockedContentType("AAA");
        addMockedContentType("BBB");
        // CCC needs to be missing for testing validation
    }

    private void fillMockedEntityTypes() {
        this.mockedEntityTypes = new ArrayList<>();
        addMockedEntityType("AAA");
        addMockedEntityType("BBB");
    }

    private void addMockedContentType(String code) {
        SmallContentType contentType = new SmallContentType();
        contentType.setCode(code);
        this.mockedContentTypes.put(code, contentType);
    }

    private void addMockedEntityType(String code) {
        SmallEntityType contentType = new SmallEntityType();
        contentType.setCode(code);
        this.mockedEntityTypes.add(contentType);
    }
}
