<%
	ui.decorateWith("kenyaui", "panel", [ heading: ui.format(program), frameOnly: true ])
%>
<% if (enrollments) { %>
<div class="ke-panel-content">
	<% enrollments.reverse().each { enrollment -> %>

		<% if (enrollment.dateCompleted) { %>
		<div class="ke-stack-item">
			${ ui.includeFragment("kenyaemr", "program/programCompletion", [ patientProgram: enrollment, showClinicalData: config.showClinicalData ]) }
		</div>
		<% } else if (patientForms) { %>
		<div class="ke-stack-item">
			<% patientForms.each { form -> %>
				${ ui.includeFragment("kenyaui", "widget/button", [
						iconProvider: form.iconProvider,
						icon: form.icon,
						label: form.name,
						extra: "Edit form",
						href: ui.pageLink("kenyaemr", "editProgramForm", [
								appId: currentApp.id,
								patientProgramId: enrollment.id,
								formUuid: form.formUuid,
								returnUrl: ui.thisUrl()
						])
				]) }
			<% } %>
		</div>
		<% } %>

		<div class="ke-stack-item">
			${ ui.includeFragment("kenyaemr", "program/programEnrollment", [ patientProgram: enrollment, showClinicalData: config.showClinicalData ]) }
		</div>
	<% } %>
</div>
<% } %>

<% if (currentEnrollment || patientIsEligible) { %>
<div class="ke-panel-footer">
	<% if (currentEnrollment) { %>

	<% if (visitId) { %>
	<%= ui.includeFragment("kenyaui", "widget/dialogForm", [
			buttonConfig: [ label: "Edit Visit", iconProvider: "kenyaui", icon: "glyphs/edit.png" ],
			dialogConfig: [ heading: "Edit ${ProgramName} enrollment visit", width: 50, height: 30 ],
			fields: [
					[ hiddenInputName: "visitId", value: visitId ],
					[ hiddenInputName: "appId", value: currentApp.id ],
					[ label: "Start Date and Time", formFieldName: "startDatetime", class: java.util.Date, initialValue: startDate, showTime: true ],

					[ label: "End Date and Time", formFieldName: "stopDatetime", class: java.util.Date, initialValue: stopDate, showTime: true ]
			],
			fragmentProvider: "kenyaemr",
			fragment: "registrationUtil",
			action: "editProgramEnrollmentVisit",
			onSuccessCallback: "ui.reloadPage()",
			submitLabel: ui.message("general.submit"),
			cancelLabel: ui.message("general.cancel")
	]) %>
	<% }  %>
	<button type="button" style="padding-right: 10px" onclick="ui.navigate('${ ui.pageLink("kenyaemr", "enterForm", [ patientId: patient.id, formUuid: defaultCompletionForm.targetUuid, appId: currentApp.id, returnUrl: ui.thisUrl() ]) }')">
		<img src="${ ui.resourceLink("kenyaui", "images/glyphs/discontinue.png") }" /> Discontinue
	</button>




	<% } else if (patientIsEligible) { %>

	<button type="button" onclick="ui.navigate('${ ui.pageLink("kenyaemr", "enterForm", [ patientId: patient.id, formUuid: defaultEnrollmentForm.targetUuid, appId: currentApp.id, returnUrl: ui.thisUrl() ]) }')">
		<img src="${ ui.resourceLink("kenyaui", "images/glyphs/enroll.png") }" /> Enroll
	</button>

	<% } %>
</div>
<% } %>
