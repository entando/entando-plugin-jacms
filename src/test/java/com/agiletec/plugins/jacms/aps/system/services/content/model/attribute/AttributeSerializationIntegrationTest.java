package com.agiletec.plugins.jacms.aps.system.services.content.model.attribute;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.services.baseconfig.ConfigInterface;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.parse.attribute.ResourceAttributeHandler;
import com.agiletec.plugins.jacms.aps.system.services.linkresolver.ILinkResolverManager;
import com.agiletec.plugins.jacms.aps.system.services.resource.IResourceManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class AttributeSerializationIntegrationTest extends BaseTestCase {

    private ConfigInterface configManager;
    private ILangManager langManager;
    private IResourceManager resourceManager;
    private IPageManager pageManager;
    private IContentManager contentManager;
    private ILinkResolverManager linkResolverManager;

    @BeforeEach
    public void init() {
        this.configManager = (ConfigInterface) this.getService(SystemConstants.BASE_CONFIG_MANAGER);
        this.langManager = (ILangManager) this.getService(SystemConstants.LANGUAGE_MANAGER);
        this.resourceManager = (IResourceManager) this.getService(JacmsSystemConstants.RESOURCE_MANAGER);
        this.pageManager = (IPageManager) this.getService(SystemConstants.PAGE_MANAGER);
        this.contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
        this.linkResolverManager = (ILinkResolverManager) this.getService(JacmsSystemConstants.LINK_RESOLVER_MANAGER);
    }

    @Test
    void testSerializeAttachAttribute() throws Exception {
        AttachAttribute attribute = new AttachAttribute();
        attribute.setResourceManager(resourceManager);
        attribute.setConfigManager(configManager);
        attribute = testSerializeAndDeserialize(attribute);
        Assertions.assertNotNull(attribute.getResourceManager());
        Assertions.assertNotNull(attribute.getConfigManager());
    }

    @Test
    void testSerializeCmsHypertextAttribute() throws Exception {
        CmsHypertextAttribute attribute = new CmsHypertextAttribute();
        attribute.setContentManager(contentManager);
        attribute.setPageManager(pageManager);
        attribute.setResourceManager(resourceManager);
        attribute.setLangManager(langManager);
        attribute = testSerializeAndDeserialize(attribute);
        Assertions.assertNotNull(attribute.getContentManager());
        Assertions.assertNotNull(attribute.getPageManager());
        Assertions.assertNotNull(attribute.getResourceManager());
        Assertions.assertNotNull(ReflectionTestUtils.invokeGetterMethod(attribute, "langManager"));
    }

    @Test
    void testSerializeLinkAttributeAttribute() throws Exception {
        LinkAttribute attribute = new LinkAttribute();
        attribute.setContentManager(contentManager);
        attribute.setPageManager(pageManager);
        attribute.setResourceManager(resourceManager);
        attribute.setLinkResolverManager(linkResolverManager);
        attribute.setLangManager(langManager);
        attribute = testSerializeAndDeserialize(attribute);
        Assertions.assertNotNull(attribute.getContentManager());
        Assertions.assertNotNull(attribute.getPageManager());
        Assertions.assertNotNull(attribute.getResourceManager());
        Assertions.assertNotNull(attribute.getLinkResolverManager());
        Assertions.assertNotNull(ReflectionTestUtils.invokeGetterMethod(attribute, "langManager"));
    }

    @Test
    void testSerializeResourceAttributeHandler() throws Exception {
        ResourceAttributeHandler attributeHandler = new ResourceAttributeHandler();
        attributeHandler.setResourceManager(resourceManager);
        attributeHandler = testSerializeAndDeserialize(attributeHandler);
        Assertions.assertNotNull(ReflectionTestUtils.invokeGetterMethod(attributeHandler, "resourceManager"));
    }

    private <T> T testSerializeAndDeserialize(T object) throws Exception {

        byte[] data;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(os)) {
            objectOutputStream.writeObject(object);
            data = os.toByteArray();
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(data);
                ObjectInputStream objectInputStream = new ObjectInputStream(is)) {
            return (T) objectInputStream.readObject();
        }
    }
}
