/*
 * Copyright 2021-Present Entando Inc. (http://www.entando.com) All rights reserved.
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
package org.entando.entando.plugins.jacms.aps.servlet;

import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.services.authorization.IAuthorizationManager;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.aps.system.services.url.IURLManager;
import com.agiletec.aps.system.services.user.IUserManager;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.IContentAuthorizationHelper;
import com.agiletec.plugins.jacms.aps.system.services.content.helper.PublicContentAuthorizationInfo;
import com.agiletec.plugins.jacms.aps.system.services.resource.IResourceManager;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.AbstractMultiInstanceResource;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInstance;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author E.Santoboni
 */
@ExtendWith(MockitoExtension.class)
public class ProtectedResourceProviderTest {
    
    @Mock
    private IUserManager userManager;
    @Mock
    private IAuthorizationManager authorizationManager;
    @Mock
    private IURLManager urlManager;
    @Mock
    private IPageManager pageManager;
    @Mock
    private ILangManager langManager;
    @Mock
    private IResourceManager resourceManager;
    @Mock
    private IContentAuthorizationHelper contentAuthorizationHelper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private HttpServletResponse response;
    @Mock
    private UserDetails user;

    @InjectMocks
    private ProtectedResourceProvider protectedResourceProvider;
    
    @BeforeEach
    public void setUp() throws Exception {
        Mockito.when(this.request.getSession()).thenReturn(this.session);
        Mockito.lenient().when(request.getRequestURL()).thenReturn(new StringBuffer("https://www.entando.com"));
        Mockito.lenient().when(pageManager.getConfig(IPageManager.CONFIG_PARAM_LOGIN_PAGE_CODE)).thenReturn("login");
        Mockito.lenient().when(pageManager.getOnlinePage(Mockito.anyString())).thenReturn(Mockito.mock(IPage.class));
        Mockito.lenient().when(langManager.getDefaultLang()).thenReturn(Mockito.mock(Lang.class));
        Mockito.lenient().when(urlManager.createURL(Mockito.any(IPage.class), Mockito.any(Lang.class), Mockito.any())).thenReturn("https://www.entando.com/en/login.page");
    }
    
    @Test
    public void provideResource_notExistingContent() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/21/0/en/ref/ART123");
        Mockito.when(this.contentAuthorizationHelper.getAuthorizationInfo("ART123")).thenReturn(null);
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(user);
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertTrue(result);
        Mockito.verify(this.userManager, Mockito.never()).getGuestUser();
        Mockito.verify(this.response, Mockito.times(1)).sendRedirect("https://www.entando.com/en/login.page");
        Mockito.verify(this.contentAuthorizationHelper, Mockito.times(1)).getAuthorizationInfo("ART123");
        Mockito.verify(this.response, Mockito.never()).setContentType(Mockito.anyString());
    }
    
    @Test
    public void provideResource_resourceNotReferenced() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/21/0/en/ref/ART1234");
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(user);
        PublicContentAuthorizationInfo publicContentAuthorizationInfo = Mockito.mock(PublicContentAuthorizationInfo.class);
        Mockito.when(this.contentAuthorizationHelper.getAuthorizationInfo("ART1234")).thenReturn(publicContentAuthorizationInfo);
        Mockito.when(publicContentAuthorizationInfo.isProtectedResourceReference("21")).thenReturn(false);
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertTrue(result);
        Mockito.verify(this.userManager, Mockito.never()).getGuestUser();
        Mockito.verify(this.response, Mockito.times(1)).sendRedirect("https://www.entando.com/en/login.page");
        Mockito.verify(this.contentAuthorizationHelper, Mockito.times(1)).getAuthorizationInfo("ART1234");
        Mockito.verify(this.response, Mockito.never()).setContentType(Mockito.anyString());
    }
    
    @Test
    public void provideResource_contentNotAllowed_1() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/21/0/en/ref/ART1234");
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(user);
        PublicContentAuthorizationInfo publicContentAuthorizationInfo = Mockito.mock(PublicContentAuthorizationInfo.class);
        Mockito.when(this.contentAuthorizationHelper.getAuthorizationInfo("ART1234")).thenReturn(publicContentAuthorizationInfo);
        Mockito.when(publicContentAuthorizationInfo.isProtectedResourceReference("21")).thenReturn(true);
        List<Group> groups = new ArrayList<>();
        Mockito.when(authorizationManager.getUserGroups(user)).thenReturn(groups);
        Mockito.when(publicContentAuthorizationInfo.isUserAllowed(groups)).thenReturn(false);
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertTrue(result);
        Mockito.verify(this.userManager, Mockito.never()).getGuestUser();
        Mockito.verify(this.response, Mockito.times(1)).sendRedirect("https://www.entando.com/en/login.page");
        Mockito.verify(this.contentAuthorizationHelper, Mockito.times(1)).getAuthorizationInfo("ART1234");
        Mockito.verify(this.response, Mockito.never()).setContentType(Mockito.anyString());
    }
    
    @Test
    public void provideResource_contentNotAllowed_2() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/21/0/en/ref/ART1234");
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(null);
        UserDetails guestUser = Mockito.mock(UserDetails.class);
        Mockito.when(this.userManager.getGuestUser()).thenReturn(guestUser);
        PublicContentAuthorizationInfo publicContentAuthorizationInfo = Mockito.mock(PublicContentAuthorizationInfo.class);
        Mockito.when(this.contentAuthorizationHelper.getAuthorizationInfo("ART1234")).thenReturn(publicContentAuthorizationInfo);
        Mockito.when(publicContentAuthorizationInfo.isProtectedResourceReference("21")).thenReturn(true);
        List<Group> groups = new ArrayList<>();
        Mockito.when(authorizationManager.getUserGroups(guestUser)).thenReturn(groups);
        Mockito.when(publicContentAuthorizationInfo.isUserAllowed(groups)).thenReturn(false);
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertTrue(result);
        Mockito.verify(this.userManager, Mockito.times(1)).getGuestUser();
        Mockito.verify(this.response, Mockito.times(1)).sendRedirect("https://www.entando.com/en/login.page");
        Mockito.verify(this.contentAuthorizationHelper, Mockito.times(1)).getAuthorizationInfo("ART1234");
        Mockito.verify(this.response, Mockito.never()).setContentType(Mockito.anyString());
    }
    
    @Test
    public void provideResource_contentNotAllowed_nullResource_1() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/22/0/en/ref/ART12345");
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(user);
        PublicContentAuthorizationInfo publicContentAuthorizationInfo = Mockito.mock(PublicContentAuthorizationInfo.class);
        Mockito.when(this.contentAuthorizationHelper.getAuthorizationInfo("ART12345")).thenReturn(publicContentAuthorizationInfo);
        Mockito.when(publicContentAuthorizationInfo.isProtectedResourceReference("22")).thenReturn(true);
        List<Group> groups = new ArrayList<>();
        Mockito.when(authorizationManager.getUserGroups(user)).thenReturn(groups);
        Mockito.when(publicContentAuthorizationInfo.isUserAllowed(groups)).thenReturn(true);
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertFalse(result);
        Mockito.verify(this.userManager, Mockito.never()).getGuestUser();
        Mockito.verify(this.response, Mockito.times(0)).sendRedirect(Mockito.anyString());
        Mockito.verify(this.contentAuthorizationHelper, Mockito.times(1)).getAuthorizationInfo("ART12345");
        Mockito.verify(this.resourceManager, Mockito.times(1)).loadResource("22");
        Mockito.verify(this.response, Mockito.never()).setContentType(Mockito.anyString());
    }
    
    @Test
    public void provideResource_contentNotAllowed_nullResource_2() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/22X/0/en");
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(user);
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertFalse(result);
        Mockito.verify(this.response, Mockito.times(0)).sendRedirect(Mockito.anyString());
        Mockito.verify(this.contentAuthorizationHelper, Mockito.never()).getAuthorizationInfo(Mockito.anyString());
        Mockito.verifyZeroInteractions(contentAuthorizationHelper, authorizationManager);
        Mockito.verify(this.resourceManager, Mockito.times(1)).loadResource("22X");
        Mockito.verify(this.response, Mockito.never()).setContentType(Mockito.anyString());
    }
    
    @Test
    public void provideResource_contentNotAllowed_notNullResource_1() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/24X/x/en");
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(user);
        ResourceInterface resource = Mockito.mock(ResourceInterface.class);
        Mockito.when(this.resourceManager.loadResource("24X")).thenReturn(resource);
        Mockito.when(resource.getMainGroup()).thenReturn("main");
        Mockito.when(resource.isMultiInstance()).thenReturn(true);
        Mockito.when(this.authorizationManager.isAuthOnGroup(user, "main")).thenReturn(false);
        Mockito.when(this.authorizationManager.isAuthOnGroup(user, Group.ADMINS_GROUP_NAME)).thenReturn(true);
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertFalse(result);
        Mockito.verify(this.response, Mockito.times(0)).sendRedirect(Mockito.anyString());
        Mockito.verify(this.contentAuthorizationHelper, Mockito.never()).getAuthorizationInfo(Mockito.anyString());
        Mockito.verifyZeroInteractions(contentAuthorizationHelper, authorizationManager);
        Mockito.verify(this.resourceManager, Mockito.times(1)).loadResource("24X");
        Mockito.verify(this.response, Mockito.never()).setContentType(Mockito.anyString());
    }
    
    @Test
    public void provideResource_contentNotAllowed_notNullResource_2() throws Exception {
        Mockito.when(this.request.getRequestURI()).thenReturn("/24X/0/en");
        Mockito.when(this.session.getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER)).thenReturn(user);
        AbstractMultiInstanceResource resource = Mockito.mock(AbstractMultiInstanceResource.class);
        Mockito.when(this.resourceManager.loadResource("24X")).thenReturn(resource);
        Mockito.when(resource.isMultiInstance()).thenReturn(true);
        Mockito.when(resource.getMainGroup()).thenReturn("default");
        Mockito.when(this.authorizationManager.isAuthOnGroup(user, "default")).thenReturn(true);
        ResourceInstance instance = Mockito.mock(ResourceInstance.class);
        Mockito.when(resource.getInstance(0, "en")).thenReturn(instance);
        
        Mockito.when(response.getOutputStream()).thenReturn(Mockito.mock(ServletOutputStream.class));
        InputStream is = Mockito.mock(InputStream.class);
        Mockito.when(resource.getResourceStream(instance)).thenReturn(is);
        Mockito.when(instance.getMimeType()).thenReturn("text/html");
        Mockito.when(is.read(Mockito.any())).thenReturn(0).thenReturn(-1);
        
        boolean result = this.protectedResourceProvider.provideProtectedResource(this.request, this.response);
        Assertions.assertTrue(result);
        Mockito.verify(this.response, Mockito.times(0)).sendRedirect(Mockito.anyString());
        Mockito.verify(this.contentAuthorizationHelper, Mockito.never()).getAuthorizationInfo(Mockito.anyString());
        Mockito.verifyZeroInteractions(contentAuthorizationHelper, authorizationManager);
        Mockito.verify(this.resourceManager, Mockito.times(1)).loadResource("24X");
        Mockito.verify(this.response, Mockito.times(1)).setContentType(Mockito.anyString());
    }
    
}
