package org.entando.entando.plugins.jacms.aps.system.services.resource;

import com.agiletec.aps.system.services.authorization.IAuthorizationManager;
import org.entando.entando.plugins.jacms.aps.system.services.util.TestHelper;
import org.entando.entando.web.common.exceptions.ValidationGenericException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import com.agiletec.aps.system.services.role.Permission;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.entando.entando.TestEntandoJndiUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

/*
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
*/
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
    "classpath*:spring/testpropertyPlaceholder.xml",
    "classpath*:spring/baseSystemConfig.xml",
    "classpath*:spring/aps/**/**.xml",
    "classpath*:spring/apsadmin/**/**.xml",
    "classpath*:spring/plugins/**/aps/**/**.xml",
    "classpath*:spring/plugins/**/apsadmin/**/**.xml",
    "classpath*:spring/web/**.xml"
})
@WebAppConfiguration(value = "")
class ResourcesServiceUnitTest {

    @Autowired
    private MessageSource messageSource;

    private IAuthorizationManager authorizationManager;
    private ResourcesService resourcesService;

    @BeforeAll
    public static void init() throws Exception {
        TestEntandoJndiUtils.setupJndi();
    }

    @BeforeEach
    public void setup() {
        authorizationManager = mock(IAuthorizationManager.class);
        resourcesService = new ResourcesService();

        when(authorizationManager.getGroupsByPermission(any(), matches(Permission.MANAGE_RESOURCES)))
                .thenReturn(TestHelper.createGroups());

        resourcesService.setAuthorizationManager(authorizationManager);
        resourcesService.setFileAllowedExtensions(Arrays.stream(new String[] { "pdf","txt" }).collect(Collectors.toList()));
        resourcesService.setImageAllowedExtensions(Arrays.stream(new String[] { "jpeg","png" }).collect(Collectors.toList()));
    }

    @Test
    void testInvalidGroup() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            resourcesService.validateGroup(null, "invalidGroup");
        });
    }

    @Test
    void testGroupValidation() {
        resourcesService.validateGroup(null, "free");
        resourcesService.validateGroup(null, "admin");
    }

    @Test
    void testMimeTypeValidation() {
        resourcesService.validateMimeType("image", "application/jpeg");
        resourcesService.validateMimeType("image", "application/png");
        resourcesService.validateMimeType("file", "application/pdf");
        resourcesService.validateMimeType("file", "application/txt");
    }

    @Test
    void testInvalidImageMimeType() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            resourcesService.validateMimeType("image", "application/pdf");
        });
    }

    @Test
    void testInvalidAttachMimeType() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            resourcesService.validateMimeType("attach", "application/jpeg");
        });
    }

    @Test
    void testInvalidResourceType() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            resourcesService.validateMimeType("image", "application/txt");
        });
    }

    private String resolveLocalizedMessage(String code, Object... args) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, currentLocale);
    }

}
