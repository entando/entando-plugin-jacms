<#assign wp=JspTaglibs["/aps-core"]>
<#assign formFieldNameVar = userFilterOptionVar.formFieldNames[0] >
<#assign formFieldValue = userFilterOptionVar.getFormFieldValue(formFieldNameVar) >
<#assign i18n_Attribute_Key = userFilterOptionVar.attribute.name >
<div class="control-group">
	<label for="${formFieldNameVar}" class="control-label"><@wp.i18n key="${i18n_Attribute_Key}" /></label>
	<div class="controls">
		<select name="${formFieldNameVar}" id="${formFieldNameVar}" class="input-xlarge">
			<option value=""><@wp.i18n key="ALL" /></option>
			<#list userFilterOptionVar.attribute.mapItems as enumeratorMapItemVar>
			<option value="${enumeratorMapItemVar.key}" <#if (formFieldValue??) && (enumeratorMapItemVar.key == formFieldValue)>selected="selected"</#if> >${enumeratorMapItemVar.value}</option>
			</#list>
		</select>
	</div>
</div>