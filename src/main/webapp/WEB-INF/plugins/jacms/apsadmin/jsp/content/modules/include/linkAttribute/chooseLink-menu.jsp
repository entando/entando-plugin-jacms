<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="wp" uri="/aps-core" %>
<%@ taglib prefix="wpsf" uri="/apsadmin-form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>

<!-- Errors -->
<s:if test="hasFieldErrors()">
    <div class="alert alert-danger alert-dismissable fade in mt-20">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
          <span class="pficon pficon-close"></span>
        </button>
        <span class="pficon pficon-error-circle-o"></span>
        <h2 class="h4 no-mt"><s:text name="message.title.FieldErrors" /></h2>
        <ul class="margin-none margin-base-top">
            <s:iterator value="fieldErrors">
                <s:iterator value="value">
	                <li><s:property escapeHtml="false" /></li>
                </s:iterator>
            </s:iterator>
        </ul>
    </div>
</s:if>

<c:set var="linkTypeVar" scope="page"><e:forHtml value="${param.linkTypeVar}" /></c:set>

<!-- Tab Menu -->
<ul class="nav nav-tabs tab-togglers mt-20" id="tab-togglers">
	<li <c:if test="${linkTypeVar eq 1}">class="active"</c:if>>
	   <a href="<s:url action="configLink">
			<s:param name="linkType" value="1" />
			<s:param name="contentOnSessionMarker" value="%{#attr.contentOnSessionMarker}" /></s:url>">
			<span class="icon fa fa-globe"></span>&#32;
			<s:text name="note.URLLinkTo" />
	   </a>
	</li>
	<li <c:if test="${linkTypeVar eq 2}">class="active"</c:if>>
	   <a href="<s:url action="configLink">
			<s:param name="linkType" value="2" />
			<s:param name="contentOnSessionMarker" value="%{#attr.contentOnSessionMarker}" /></s:url>">
			<span class="icon fa fa-folder"></span>&#32;
			<s:text name="note.pageLinkTo" />
	   </a>
	</li>
	<li <c:if test="${linkTypeVar eq 3}">class="active"</c:if>>
		<a href="<s:url action="configLink">
			<s:param name="linkType" value="3" />
			<s:param name="contentOnSessionMarker" value="%{#attr.contentOnSessionMarker}" /></s:url>">
			<span class="icon fa fa-file-text-o"></span>&#32;
			<s:text name="note.contentLinkTo" />
		</a>
	</li>
</ul>
