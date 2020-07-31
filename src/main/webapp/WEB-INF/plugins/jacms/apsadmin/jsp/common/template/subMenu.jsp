<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="wpsa" uri="/apsadmin-core" %>
<%@ taglib prefix="wp" uri="/aps-core" %>

<s:set var="appBuilderIntegrationEnabled" ><wp:info key="systemParam" paramName="appBuilderIntegrationEnabled" /></s:set>
<s:set var="appBuilderBaseURL" ><wp:info key="systemParam" paramName="appBuilderBaseURL" /></s:set>

<wp:ifauthorized permission="superuser" var="isSuperUser" />
<wp:ifauthorized permission="editContents" var="isEditContents" />
<wp:ifauthorized permission="manageResources" var="isManageResources" />
<wp:ifauthorized permission="manageCategories" var="isCategories" />

<c:if test="${isEditContents || isManageResources}">
    <c:if test="${isEditContents}">
        <li class="list-group-item">
            <s:if test="#appBuilderIntegrationEnabled == 'true'">
                <a href='<c:out value="${appBuilderBaseURL}"/>cms/contents'>
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contents" /></span>
                </a>
            </s:if>
            <s:else>
                <a href="<s:url action="list" namespace="/do/jacms/Content" />">
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contents" /></span>
                </a>
            </s:else>
        </li>
    </c:if>
    <c:if test="${isManageResources}">
        <li class="list-group-item">
            <s:if test="#appBuilderIntegrationEnabled == 'true'">
                <a href='<c:out value="${appBuilderBaseURL}"/>cms/assets'>
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.digitalAssets" /></span>
                </a>
            </s:if>
            <s:else>
                <a href="<s:url action="list" namespace="/do/jacms/Resource" ><s:param name="resourceTypeCode" >Image</s:param></s:url>">
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.digitalAssets" /></span>
                </a>
            </s:else>
        </li>
    </c:if>
    <c:if test="${isSuperUser}">
        <li class="list-group-item">
            <s:if test="#appBuilderIntegrationEnabled == 'true'">
                <a href='<c:out value="${appBuilderBaseURL}"/>cms/content-templates'>
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentModels" /></span>
                </a>
            </s:if>
            <s:else>
                <a href="<s:url action="list" namespace="/do/jacms/ContentModel" />">
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentModels" /></span>
                </a>
            </s:else>
        </li>

        <c:if test="${isCategories}">
            <li class="list-group-item">
                <s:if test="#appBuilderIntegrationEnabled == 'true'">
                    <a href='<c:out value="${appBuilderBaseURL}"/>category'>
                        <span class="list-group-item-value"><s:text name="menu.settings.categories" /></span>
                    </a>
                </s:if>
                <s:else>
                    <a href='<s:url action="viewTree" namespace="/do/Category" />'>
                        <span class="list-group-item-value"><s:text name="menu.settings.categories" /></span>
                    </a>
                </s:else>
            </li>
        </c:if>
        <li class="list-group-item">
            <s:if test="#appBuilderIntegrationEnabled == 'true'">
                <a href='<c:out value="${appBuilderBaseURL}"/>cms/versioning'>
                    <span class="list-group-item-value"><s:text name="jpversioning.admin.menu" /></span>
                </a>
            </s:if>
            <s:else>
                <a href="<s:url action="list" namespace="/do/jpversioning/Content/Versioning" />">
                    <span class="list-group-item-value"><s:text name="jpversioning.admin.menu" /></span>
                </a>
            </s:else>
        </li>
        <li class="list-group-item">
            <a href="<s:url action="viewItem" namespace="/do/jpcontentscheduler/config" />">
                <span class="list-group-item-value"><s:text name="jpcontentscheduler.admin.menu" /></span>
            </a>
        </li>

        <li class="list-group-item">
            <s:if test="#appBuilderIntegrationEnabled == 'true'">
                <a href='<c:out value="${appBuilderBaseURL}"/>cms/content-types'>
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentTypes" /></span>
                </a>
            </s:if>
            <s:else>
                <a href="<s:url action="initViewEntityTypes" namespace="/do/Entity"><s:param name="entityManagerName">jacmsContentManager</s:param></s:url>">
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentTypes" /></span>
                </a>
            </s:else>
        </li>
        <li class="list-group-item">
            <s:if test="#appBuilderIntegrationEnabled == 'true'">
                <a href='<c:out value="${appBuilderBaseURL}"/>cms/content-settings'>
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentSettings" /></span>
                </a>
            </s:if>
            <s:else>
                <a href="<s:url action="openIndexProspect" namespace="/do/jacms/Content/Admin" />">
                    <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentSettings" /></span>
                </a>
            </s:else>
        </li>
    </c:if>
</c:if>