<%
	def startFieldName = config.formFieldName ?: "param[county]";
	def endFieldName = config.endFieldName ?: "param[endDate]";

%>

<script type="text/javascript">
	jQuery(function() {
		var select = jQuery('#${ config.id }');

		select.change(function() {
			var period = jQuery(this).val();
			console.log(">>>>>>>>>>>>>",jQuery('#county_value').val(period));
			jQuery('#county_value').val(period);
		});

		select.change();

	});
</script>

<div id="${ config.id }-container">
	<select name="${ startFieldName }" id="${ config.id }" >
		<option></option>
		<%countyList.each { %>
		<option  value="${it}">${it}</option>
		<%}%>
	</select>
	<input type="hidden" id="county_value" />

</div>
