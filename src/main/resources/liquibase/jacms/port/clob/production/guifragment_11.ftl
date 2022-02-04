<#assign wp=JspTaglibs["/aps-core"]>

<#assign currentLangVar ><@wp.info key="currentLang" /></#assign>

<#assign js_for_datepicker="jQuery(function($){
	$.datepicker.regional['it'] = {
		closeText: 'Chiudi',
		prevText: '&#x3c;Prec',
		nextText: 'Succ&#x3e;',
		currentText: 'Oggi',
		monthNames: ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
			'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'],
		monthNamesShort: ['Gen','Feb','Mar','Apr','Mag','Giu',
			'Lug','Ago','Set','Ott','Nov','Dic'],
		dayNames: ['Domenica','Luned&#236','Marted&#236','Mercoled&#236','Gioved&#236','Venerd&#236','Sabato'],
		dayNamesShort: ['Dom','Lun','Mar','Mer','Gio','Ven','Sab'],
		dayNamesMin: ['Do','Lu','Ma','Me','Gi','Ve','Sa'],
		weekHeader: 'Sm',
		dateFormat: 'yy-mm-dd',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
});

jQuery(function($){
	if (Modernizr.touch && Modernizr.inputtypes.date) {
		$.each(	$('input[data-isdate=true]'), function(index, item) {
			item.type = 'date';
		});
	} else {
		$.datepicker.setDefaults( $.datepicker.regional['${currentLangVar}'] );
		$('input[data-isdate=true]').datepicker({
      			changeMonth: true,
      			changeYear: true,
      			dateFormat: 'yyyy-mm-dd'
    		});
	}
});" >

<@wp.headInfo type="JS" info="entando-misc-html5-essentials/modernizr-2.5.3-full.js" />
<@wp.headInfo type="JS_EXT" info="http://code.jquery.com/ui/1.10.0/jquery-ui.min.js" />
<@wp.headInfo type="CSS_EXT" info="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.min.css" />
<@wp.headInfo type="JS_RAW" info="${js_for_datepicker}" />
<fieldset>
<legend>
<#assign i18n_Attribute_Key = userFilterOptionVar.attribute.name >
<@wp.i18n key="${i18n_Attribute_Key}" />
</legend>
<div class="control-group">
	<#assign formFieldStartNameVar = userFilterOptionVar.formFieldNames[0] >
	<#assign formFieldStartValueVar = userFilterOptionVar.getFormFieldValue(formFieldStartNameVar) >
	<label for="${formFieldStartNameVar}" class="control-label">
		<@wp.i18n key="DATE_FROM" />
	</label>
	<div class="controls">
		<input id="${formFieldStartNameVar}" name="${formFieldStartNameVar}" value="${formFieldStartValueVar?default("")}" type="text" data-isdate="true" class="input-xlarge" placeholder="YYYY-MM-DD" />
	</div>
</div>
<div class="control-group">
	<#assign formFieldEndNameVar = userFilterOptionVar.formFieldNames[1] >
	<#assign formFieldEndValueVar = userFilterOptionVar.getFormFieldValue(formFieldEndNameVar) >
	<label for="${formFieldEndNameVar}" class="control-label">
		<@wp.i18n key="DATE_TO" />
	</label>
	<div class="controls">
		<input id="${formFieldEndNameVar}" name="${formFieldEndNameVar}" value="${formFieldEndValueVar?default("")}" type="text" data-isdate="true" class="input-xlarge" placeholder="YYYY-MM-DD" />
	</div>
</div>
</fieldset>