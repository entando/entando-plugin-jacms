<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="wpsa" uri="/apsadmin-core"%>
<%@ taglib prefix="wpsf" uri="/apsadmin-form"%>
<%@ taglib prefix="jacmsapsadmin" uri="/jacms-apsadmin-core"%>

<s:form action="entandoSearch" cssClass="form-horizontal" role="search">
    <s:include value="/WEB-INF/plugins/jacms/apsadmin/jsp/content/modules/include/hypertextAttribute/info-prev-value.jsp" />

    <p class="sr-only">
        <s:text name="note.chooseContentToLink" />
    </p>
    <p class="sr-only">
        <wpsf:hidden name="lastGroupBy" />
        <wpsf:hidden name="lastOrder" />
        <wpsf:hidden name="activeTab" value="2" />
        <wpsf:hidden name="internalContentActionName" value="entandoContentSearch" />
        <wpsf:hidden name="contentOnSessionMarker" />
        <wpsf:hidden name="linkTypeVar" value="3" />
        <wpsf:hidden name="prevCode" value="%{#prevContentVoVar.id}" />
    </p>
    <div class="col-xs-12">
        <div class="well">
            <p class="search-label">
                <s:text name="label.search.label" />
            </p>
            <!-- Description -->
            <div class="form-group">
                <label for="text" class="control-label col-sm-2"> <s:text name="label.description" /></label>
                <div class="col-sm-9">
                    <wpsf:textfield name="text" id="text" cssClass="form-control"
                                    placeholder="%{getText('label.description')}"
                                    title="%{getText('label.search.by')+' '+getText('label.description')}" />
                </div>
            </div>
            <!-- Content Id -->
            <div class="form-group">
                <label class="control-label col-sm-2 text-right"
                       for="contentIdToken"> <s:text name="label.code" />
                </label>
                <div class="col-sm-9">
                    <wpsf:textfield name="contentIdToken" id="contentIdToken"
                                    placeholder="%{getText('label.contentId')}" cssClass="form-control" />
                </div>
            </div>
            <!-- Content Type -->
            <div class="form-group">
                <label class="control-label col-sm-2 text-right" for="contentType">
                    <s:text name="label.type" />
                </label>
                <div class="col-sm-9">
                    <wpsf:select name="contentType" id="contentType"
                                 list="contentTypes" listKey="code" listValue="description"
                                 headerKey="" headerValue="%{getText('note.choose')}"
                                 cssClass="form-control" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-9">
                    <wpsf:submit type="button" cssClass="btn btn-primary pull-right">
                        <s:text name="label.search" />
                    </wpsf:submit>
                </div>
            </div>
        </div>
    </div>
</s:form>
<s:form action="entandoSearch">
    <div class="col-xs-12">
        <p class="sr-only">
            <wpsf:hidden name="text" />
            <wpsf:hidden name="contentType" />
            <wpsf:hidden name="state" />
            <wpsf:hidden name="categoryCode" />
            <wpsf:hidden name="viewTypeDescr" />
            <wpsf:hidden name="viewGroup" />
            <wpsf:hidden name="viewCode" />
            <wpsf:hidden name="viewStatus" />
            <wpsf:hidden name="viewCreationDate" />
            <wpsf:hidden name="lastGroupBy" />
            <wpsf:hidden name="lastOrder" />
            <wpsf:hidden name="contentIdToken" />
            <wpsf:hidden name="activeTab" value="2" />
            <wpsf:hidden name="internalContentActionName" value="entandoContentSearch" />
            <wpsf:hidden name="contentOnSessionMarker" />
            <wpsf:hidden name="linkTypeVar" value="3" />
        </p>
        <s:set var="maxSizeVar" value="10" />
        <s:set var="paginatedContentIdsVar" value="%{getPaginatedContentsId(#maxSizeVar)}" />
        <s:set var="contentIdsVar" value="#paginatedContentIdsVar.list" />
        
        <jacmsapsadmin:cmssubset pagerId="%{getPagerId()}" total="%{#paginatedContentIdsVar.count}" maxSize="#maxSizeVar" objectName="groupContent" offset="5" >
            <s:set var="group" value="#groupContent" />
            <s:if test="%{#contentIdsVar.size() > 0}">
                <div class="table-responsive no-mb">
                    <table class="table table-striped table-bordered table-hover no-mb" id="contentListTable">
                        <thead>
                            <tr>
                                <th class="text-center w2perc"><input type="radio" disabled /></th>
                                <!-- description -->
                                <th><s:text name="label.description" /></a></th>
                                <!-- key -->
                                <th class="text-left"><s:text name="label.code" /></th>
                                <!-- creation date -->
                                <th class="text-center"><s:text name="label.creationDate" /></th>
                                <!-- last edit date -->
                                <th class="text-center"><s:text name="label.lastEdit" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <s:iterator var="contentId" value="#contentIdsVar" >
                                <s:set var="contentVar" value="%{getContentVo(#contentId)}" />
                                <tr>
                                    <td class="text-center">
                                        <input type="radio" name="contentId"
                                               id="contentId_<s:property value="#contentVar.id"/>"
                                               value="<s:property value="#contentVar.id"/>" />
                                    </td>
                                    <td><s:property value="#contentVar.descr" /></td>
                                    <td class="text-left"><s:property value="#contentVar.id" /></td>
                                    <td class="text-center">
                                        <abbr title="<s:property value="%{#contentVar.create}" />">
                                            <s:property value="%{#contentVar.create}" />
                                        </abbr>
                                    </td>
                                    <td class="text-center">
                                        <abbr title="<s:property value="%{#contentVar.create}" />">
                                            <s:property value="%{#contentVar.create}" />
                                        </abbr>
                                    </td>
                                </tr>
                            </s:iterator>
                        </tbody>
                    </table>
                </div>
                <div class="content-view-pf-pagination table-view-pf-pagination clearfix">
                    <div class="form-group">
                        <span><s:include value="/WEB-INF/apsadmin/jsp/common/inc/pagerInfo.jsp" /></span>
                        <div class="mt-5">
                            <s:include value="/WEB-INF/apsadmin/jsp/common/inc/pager_formTable.jsp" />
                        </div>
                    </div>
                </div>

                <!-- Link attributes -->
                <s:include value="/WEB-INF/plugins/jacms/apsadmin/jsp/content/modules/include/entando-link-attributes.jsp" />

                <div class="form-group mt-20">
                    <button type="submit" id="button_contentLink" name="button_contentLink" 
                            class="btn btn-primary pull-right">
                        <s:text name="label.save" />
                    </button>
                </div>
            </s:if>
            <s:else>
                <div class="alert alert-info">
                    <span class="pficon pficon-info"></span>
                    <strong><s:text name="label.listEmpty" /></strong>&#32;
                    <s:text name="label.noContentFound" />
                </div>
            </s:else>
        </jacmsapsadmin:cmssubset>
    </div>
</s:form>
