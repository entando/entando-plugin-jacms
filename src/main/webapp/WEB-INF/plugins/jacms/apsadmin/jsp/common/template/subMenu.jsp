<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="wpsa" uri="/apsadmin-core" %>
<%@ taglib prefix="wp" uri="/aps-core" %>

<wp:ifauthorized permission="superuser" var="isSuperUser" />
<wp:ifauthorized permission="editContents" var="isEditContents" />
<wp:ifauthorized permission="validateContents" var="isValidateContents" />
<wp:ifauthorized permission="manageResources" var="isManageResources" />
<wp:ifauthorized permission="manageCategories" var="isCategories" />

<ul class="list-group">
    <c:if test="${isSuperUser || isEditContents || isValidateContents}">
        <li class="list-group-item">
            <a href="<s:url action="list" namespace="/do/jacms/Content" />">
                <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contents" /></span>
            </a>
        </li>
    </c:if>
    <c:if test="${isSuperUser || isEditContents || isValidateContents || isManageResources}">
        <li class="list-group-item">
            <a href="<s:url action="list" namespace="/do/jacms/Resource" ><s:param name="resourceTypeCode" >Image</s:param></s:url>">
                <span class="list-group-item-value"><s:text name="menu.APPS.CMS.digitalAssets" /></span>
            </a>
        </li>
    </c:if>
    <c:if test="${isSuperUser || isValidateContents}">
        <li class="list-group-item">
            <a href="<s:url action="list" namespace="/do/jacms/ContentModel" />">
                <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentModels" /></span>
            </a>
        </li>
    </c:if>
    <c:if test="${isSuperUser || isCategories}">
        <li class="list-group-item">
            <a href='<s:url action="viewTree" namespace="/do/Category" />'>
                <span class="list-group-item-value"><s:text name="menu.settings.categories" /></span>
            </a>
        </li>
    </c:if>
    <c:if test="${isSuperUser || isEditContents || isValidateContents || isManageResources}">
        <li class="list-group-item">
            <a href="<s:url action="list" namespace="/do/jpversioning/Content/Versioning" />">
                <span class="list-group-item-value"><s:text name="jpversioning.admin.menu" /></span>
            </a>
        </li>
    </c:if>
    <c:if test="${isSuperUser}">
        <li class="list-group-item">
            <a href="<s:url action="initViewEntityTypes" namespace="/do/Entity"><s:param name="entityManagerName">jacmsContentManager</s:param></s:url>">
                <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentTypes" /></span>
            </a>
        </li>
        <li class="list-group-item">
            <a href="<s:url action="openIndexProspect" namespace="/do/jacms/Content/Admin" />">
                <span class="list-group-item-value"><s:text name="menu.APPS.CMS.contentSettings" /></span>
            </a>
        </li>
    </c:if>
</ul>
