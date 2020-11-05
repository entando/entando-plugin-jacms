<%@ taglib prefix="jacms" uri="/jacms-aps-core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wp" uri="/aps-core"%>

<jacms:contentInfo param="authToEdit" var="canEditThis" />
<jacms:contentInfo param="contentId" var="myContentId" />

<c:if test="${canEditThis}">
	<style>
		.bar-content-edit {
				position: absolute;
				transform: translate(4px, 20px);
		}

		.bar-content-edit .btn {
				color: white;
				padding: 12px 32px;
				background: #33B5E5;
				text-transform: uppercase;
				text-decoration: none;
				border-radius: 2px;
				box-shadow: 4px 4px 8px 1px rgba(0,0,0,0.5);
		}

		.bar-content-edit-container:not(:hover) .bar-content-edit {
				display: none;
		}
	</style>
	<div class="bar-content-edit-container">
		<div class="bar-content-edit">
			<a href="<wp:info key="systemParam" paramName="applicationBaseURL" />do/jacms/Content/edit.action?contentId=<jacms:contentInfo param="contentId" />" class="btn btn-info">
				<wp:i18n key="EDIT_THIS_CONTENT" /> <i class="icon-edit icon-white" aria-hidden="true"></i></a>
		</div>
		<jacms:content publishExtraTitle="true" />
	</div>
</c:if>

<c:if test="${!canEditThis}">
	<jacms:content publishExtraTitle="true" />
</c:if>
