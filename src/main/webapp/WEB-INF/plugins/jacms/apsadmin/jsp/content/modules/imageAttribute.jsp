<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="wp" uri="/aps-core" %>
<%@ taglib prefix="wpsa" uri="/apsadmin-core" %>
<%@ taglib prefix="wpsf" uri="/apsadmin-form" %>
<s:set var="currentResource" value="#attribute.resources[#lang.code]" />
<s:set var="defaultResource" value="#attribute.resource" />

<%-- default language --%>
<s:if test="#lang.default">
    <%-- resource filled --%>
    <s:if test="#currentResource != null">
        <s:set var="divClass" value="'no-padding'"/>
        <s:if test="!(#attributeTracer.monoListElement) || ((#attributeTracer.monoListElement) && (#attributeTracer.compositeElement))">
            <div class="panel panel-default margin-small-top">
                <s:set var="divClass" value="''"/>
            </s:if>
            <div class="panel-body ${divClass}">
                <%-- download --%>
                <div class="col-xs-12 col-sm-5 col-md-4 col-lg-3 text-center">
                    <a href="<s:property value="#defaultResource.getImagePath('0')" />"
                       title="<s:text name="label.img.original" />" class="mt-5 mb-20 display-block">
                        <img class="img-thumbnail" src="<s:property value="#defaultResource.getImagePath('1')"/>" alt="<s:property value="#defaultResource.descr"/>" style="height:90px; max-width:130px" />
                    </a>
                </div>
                <%-- label and input --%>
                <div class="col-xs-12 col-sm-7 col-md-8 col-lg-9 form-horizontal margin-large-top">
                    <div class="form-group">
                        <div class="col-xs-12">
                            <p>
                                <strong><s:text name="label.description" />:</strong>&nbsp;
                                <s:property value="#defaultResource.description" />
                                <br />
                                <strong><s:text name="label.filename" />:</strong>&nbsp;
                                <s:property value="#defaultResource.masterFileName" />
                            </p>
                            <label class="col-lg-2 col-md-3 col-sm-4 no-padding pr-10 text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}" />">
                                <span title="<s:text name="label.img.text.long" />"><s:text name="label.img.text.short" /></span>
                            </label>
                            <div class="col-lg-10 col-md-9 col-sm-8 no-padding">
                                <s:include value="/WEB-INF/apsadmin/jsp/entity/modules/textAttribute.jsp" />
                            </div>
                        </div>
                    </div>
                    <!-- alt, description, legend, and title -->
                    <div class="form-group">
                        <div class="col-xs-12">
                            <label class="col-lg-2 col-md-3 col-sm-4 no-padding pr-10 text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_alt" />">
                                <span title="<s:text name="label.img.alt.long" />"><s:text name="label.alt.short" /></span>
                            </label>
                            <div class="col-lg-10 col-md-9 col-sm-8 no-padding">
                                <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_alt"
                                name="%{#attributeTracer.getFormFieldName(#attribute)}_alt" value="%{#attribute.getResourceAltForLang(#lang.code)}"
                                maxlength="254" cssClass="form-control" />
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-xs-12">
                            <label class="col-lg-2 col-md-3 col-sm-4 no-padding pr-10 text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_description" />">
                                <span title="<s:text name="label.img.description.long" />"><s:text name="label.description.short" /></span>
                            </label>
                            <div class="col-lg-10 col-md-9 col-sm-8 no-padding">
                                <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_description"
                                name="%{#attributeTracer.getFormFieldName(#attribute)}_description" value="%{#attribute.getResourceDescriptionForLang(#lang.code)}"
                                maxlength="254" cssClass="form-control" />
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-xs-12">
                            <label class="col-lg-2 col-md-3 col-sm-4 no-padding pr-10 text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_legend" />">
                                <span title="<s:text name="label.img.legend.long" />"><s:text name="label.legend.short" /></span>
                            </label>
                            <div class="col-lg-10 col-md-9 col-sm-8 no-padding">
                                <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_legend"
                                name="%{#attributeTracer.getFormFieldName(#attribute)}_legend" value="%{#attribute.getResourceLegendForLang(#lang.code)}"
                                maxlength="254" cssClass="form-control" />
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-xs-12">
                            <label class="col-lg-2 col-md-3 col-sm-4 no-padding pr-10 text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_title" />">
                                <span title="<s:text name="label.img.title.long" />"><s:text name="label.title.short" /></span>
                            </label>
                            <div class="col-lg-10 col-md-9 col-sm-8 no-padding">
                                <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_legend"
                                name="%{#attributeTracer.getFormFieldName(#attribute)}_title" value="%{#attribute.getResourceTitleForLang(#lang.code)}"
                                maxlength="254" cssClass="form-control" />
                            </div>
                        </div>
                    </div>
                    <s:if test="!(#attributeTracer.monoListElement) || ((#attributeTracer.monoListElement) && (#attributeTracer.compositeElement))">
                        <div class="text-right">
                            <s:include value="/WEB-INF/plugins/jacms/apsadmin/jsp/content/modules/include/removeResourceSubmit.jsp">
                                <s:param name="resourceTypeCode">Image</s:param>
                            </s:include>
                        </div>
                    </s:if>
                </div>
            </div>
            <s:if test="!(#attributeTracer.monoListElement) || ((#attributeTracer.monoListElement) && (#attributeTracer.compositeElement))">
            </div>
        </s:if>
    </s:if>
    <%-- resource empty --%>
    <s:else>
        <s:include value="/WEB-INF/plugins/jacms/apsadmin/jsp/content/modules/include/chooseResourceSubmit.jsp">
            <s:param name="resourceTypeCode">Image</s:param>
        </s:include>
    </s:else>
</s:if>
<%-- Not-default lang --%>
<s:else>
    <%-- empty resource --%>
    <s:if test="#defaultResource == null">
        <span class="form-control-static text-info"><s:text name="note.editContent.doThisInTheDefaultLanguage" />.</span>
    </s:if>
    <s:else>
        <s:set var="currentResourceIsEmptyVar" value="%{false}" />
        <s:set var="langResourceVar" value="#currentResource" />
        <s:if test="#currentResource == null">
            <s:set var="langResourceVar" value="#defaultResource" />
            <s:set var="currentResourceIsEmptyVar" value="%{true}" />
        </s:if>
        <s:set var="attributeIsNestedVar" value="%{
               (#attributeTracer.monoListElement && #attributeTracer.compositeElement)
               ||
               (#attributeTracer.monoListElement==true && #attributeTracer.compositeElement==false)
               ||
               (#attributeTracer.monoListElement==false && #attributeTracer.compositeElement==true)
               }" />
        <%-- attributeIsNestedVar: <s:property value="#attributeIsNestedVar" /><br /> --%>
        <s:if test="!#attributeIsNestedVar">
            <div class="panel panel-default margin-small-top">
            </s:if>
            <div class="row panel-body">
                <%-- download icon + button --%>
                <div class="col-xs-12 col-sm-3 col-lg-2 text-center">
                    <a href="<s:property value="#langResourceVar.getImagePath('0')" />" title="<s:text name="label.img.original" />">
                        <img class="img-thumbnail" src="<s:property value="#langResourceVar.getImagePath('1')"/>" alt="<s:property value="#currentResource.descr"/>" style="height:90px; max-width:130px" />
                    </a>
                </div>
                <%-- text field --%>
                <div class="col-xs-12 col-sm-9 col-lg-10 form-horizontal margin-large-top">
                    <div class="form-group">
                        <label class="col-xs-2 control-label text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}" />">
                            <s:text name="label.text" />
                        </label>
                        <div class="col-xs-10">
                            <s:include value="/WEB-INF/apsadmin/jsp/entity/modules/textAttribute.jsp" />
                        </div>
                    </div>
                    
                    <!-- alt, description, legend, and title -->
                    <div class="form-group">
                        <label class="col-xs-2 control-label text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_alt" />">
                            <span title="<s:text name="label.img.alt.long" />"><s:text name="label.alt.short" /></span>
                        </label>
                        <div class="col-xs-10">
                            <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_alt"
                                            name="%{#attributeTracer.getFormFieldName(#attribute)}_alt" value="%{#attribute.getResourceAltForLang(#lang.code)}"
                                            maxlength="254" cssClass="form-control" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-xs-2 control-label text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_description" />">
                            <span title="<s:text name="label.img.description.long" />"><s:text name="label.description.short" /></span>
                        </label>
                        <div class="col-xs-10">
                            <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_description"
                                            name="%{#attributeTracer.getFormFieldName(#attribute)}_description" value="%{#attribute.getResourceDescriptionForLang(#lang.code)}"
                                            maxlength="254" cssClass="form-control" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-xs-2 control-label text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_legend" />">
                            <span title="<s:text name="label.img.legend.long" />"><s:text name="label.legend.short" /></span>
                        </label>
                        <div class="col-xs-10">
                            <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_legend"
                                            name="%{#attributeTracer.getFormFieldName(#attribute)}_legend" value="%{#attribute.getResourceLegendForLang(#lang.code)}"
                                            maxlength="254" cssClass="form-control" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-xs-2 control-label text-right" for="<s:property value="%{#attributeTracer.getFormFieldName(#attribute)}_title" />">
                            <span title="<s:text name="label.img.title.long" />"><s:text name="label.title.short" /></span>
                        </label>
                        <div class="col-xs-10">
                            <wpsf:textfield id="%{#attributeTracer.getFormFieldName(#attribute)}_legend"
                                            name="%{#attributeTracer.getFormFieldName(#attribute)}_title" value="%{#attribute.getResourceTitleForLang(#lang.code)}"
                                            maxlength="254" cssClass="form-control" />
                        </div>
                    </div>
                    <%-- choose resource button --%>
                    <div class="text-right">
                        <s:if test="#currentResourceIsEmptyVar">
                            <s:include value="/WEB-INF/plugins/jacms/apsadmin/jsp/content/modules/include/chooseResourceSubmit.jsp">
                                <s:param name="resourceTypeCode">Image</s:param>
                                <s:param name="buttonCssClass">btn btn-primary</s:param>
                            </s:include>
                        </s:if>
                        <s:else>
                            <%-- remove resource button --%>
                            <s:include value="/WEB-INF/plugins/jacms/apsadmin/jsp/content/modules/include/removeResourceSubmit.jsp">
                                <s:param name="resourceTypeCode">Image</s:param>
                            </s:include>
                        </s:else>
                    </div>
                </div>
            </div>
            <s:if test="!#attributeIsNestedVar">
            </div>
        </s:if>
    </s:else>
</s:else>
