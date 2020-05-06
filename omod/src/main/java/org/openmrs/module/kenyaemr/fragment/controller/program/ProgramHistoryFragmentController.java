/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.fragment.controller.program;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.kenyacore.form.FormDescriptor;
import org.openmrs.module.kenyacore.form.FormManager;
import org.openmrs.module.kenyacore.program.ProgramDescriptor;
import org.openmrs.module.kenyacore.program.ProgramManager;
import org.openmrs.module.kenyaemr.EmrActivator;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.metadata.IPTMetadata;
import org.openmrs.module.kenyaemr.metadata.TbMetadata;
import org.openmrs.module.kenyaemr.util.EmrUtils;
import org.openmrs.module.kenyaemr.util.HtsConstants;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Patient program history fragment
 */
public class ProgramHistoryFragmentController {
	protected static final Log log = LogFactory.getLog(EmrActivator.class);

	public void controller(FragmentModel model,
						   @FragmentParam("patient") Patient patient,
						   @FragmentParam("program") Program program,
						   @FragmentParam("showClinicalData") boolean showClinicalData,
						   UiUtils ui,
						   PageRequest pageRequest,
						   @SpringBean ProgramManager programManager,
						   @SpringBean FormManager formManager,
						   @SpringBean KenyaUiUtils kenyaUi) {

		AppDescriptor currentApp = kenyaUi.getCurrentApp(pageRequest);

		ProgramDescriptor descriptor = programManager.getProgramDescriptor(program);
		boolean patientIsEligible = programManager.isPatientEligibleFor(patient, program);
		PatientProgram currentEnrollment = null;
		String ProgramName = null;
		Integer visitId = null;
		Date visitStopDate = null;
		Date visitStartDate = null;
		FormService formService = Context.getFormService();
		EncounterService encounterService = Context.getEncounterService();
		EncounterType et = encounterService.getEncounterTypeByUuid(HivMetadata._EncounterType.HIV_ENROLLMENT);
		Form pform = formService.getFormByUuid(HivMetadata._Form.HIV_ENROLLMENT);
		EncounterType iptEnrollmentEncounter = encounterService.getEncounterTypeByUuid(IPTMetadata._EncounterType.IPT_INITIATION);
		Form iptEnrollmentForm = formService.getFormByUuid(IPTMetadata._Form.IPT_INITIATION);

		EncounterType tbEnrollmentEncounter = encounterService.getEncounterTypeByUuid(TbMetadata._EncounterType.TB_ENROLLMENT);
		Form tbEnrollmentForm = formService.getFormByUuid(TbMetadata._Form.TB_ENROLLMENT);

		// Gather all program enrollments for this patient and program
		List<PatientProgram> enrollments = programManager.getPatientEnrollments(patient, program);
		for (PatientProgram enrollment : enrollments) {
			if (enrollment.getActive()) {
				currentEnrollment = enrollment;
				if(currentEnrollment.getProgram().getUuid().equalsIgnoreCase(HivMetadata._Program.HIV)){
					ProgramName = currentEnrollment.getProgram().getName();
					Encounter lastHivEnrollmentEnc = EmrUtils.lastEncounter(patient,et ,pform );
					if(lastHivEnrollmentEnc != null && lastHivEnrollmentEnc.getVisit() !=null){
						visitId = lastHivEnrollmentEnc.getVisit().getVisitId();
						visitStopDate = lastHivEnrollmentEnc.getVisit().getStopDatetime();
						visitStartDate = lastHivEnrollmentEnc.getVisit().getStartDatetime();
					}
				}
				 if(currentEnrollment.getProgram().getUuid().equalsIgnoreCase(IPTMetadata._Program.IPT)){
					ProgramName = currentEnrollment.getProgram().getName();
					Encounter lastIPTEnrollmentEnc = EmrUtils.lastEncounter(patient,iptEnrollmentEncounter ,iptEnrollmentForm );
					if(lastIPTEnrollmentEnc != null && lastIPTEnrollmentEnc.getVisit() !=null){
						visitId = lastIPTEnrollmentEnc.getVisit().getVisitId();
						visitStopDate = lastIPTEnrollmentEnc.getVisit().getStopDatetime();
						visitStartDate= lastIPTEnrollmentEnc.getVisit().getStartDatetime();
					}
				}

				if(currentEnrollment.getProgram().getUuid().equalsIgnoreCase(TbMetadata._Program.TB)){
					ProgramName = currentEnrollment.getProgram().getName();
					Encounter lastTbEnrollmentEnc = EmrUtils.lastEncounter(patient,tbEnrollmentEncounter ,tbEnrollmentForm );
					if(lastTbEnrollmentEnc != null && lastTbEnrollmentEnc.getVisit() !=null){
						visitId = lastTbEnrollmentEnc.getVisit().getVisitId();
						visitStopDate = lastTbEnrollmentEnc.getVisit().getStopDatetime();
						visitStartDate= lastTbEnrollmentEnc.getVisit().getStartDatetime();
					}
				}

				if(currentEnrollment.getProgram().getUuid().equalsIgnoreCase(TbMetadata._Program.TB)){
					ProgramName = currentEnrollment.getProgram().getName();
					Encounter lastTbEnrollmentEnc = EmrUtils.lastEncounter(patient,tbEnrollmentEncounter ,tbEnrollmentForm );
					if(lastTbEnrollmentEnc != null && lastTbEnrollmentEnc.getVisit() !=null){
						visitId = lastTbEnrollmentEnc.getVisit().getVisitId();
						visitStopDate = lastTbEnrollmentEnc.getVisit().getStopDatetime();
						visitStartDate= lastTbEnrollmentEnc.getVisit().getStartDatetime();
					}
				}

			}
		}

		// Per-patient forms need simplified if there are any
		List<SimpleObject> patientForms = new ArrayList<SimpleObject>();
		if (descriptor.getPatientForms() != null) {
			for (FormDescriptor form : formManager.getProgramFormsForPatient(currentApp, program, patient)) {
				patientForms.add(ui.simplifyObject(form.getTarget()));
			}
		}




		model.addAttribute("visitId",visitId );
		model.addAttribute("stopDate",visitStopDate );
		model.addAttribute("startDate", visitStartDate);
		model.addAttribute("ProgramName", ProgramName);


		model.addAttribute("patient", patient);
		model.addAttribute("program", program);
		model.addAttribute("defaultEnrollmentForm", descriptor.getDefaultEnrollmentForm());
		model.addAttribute("defaultCompletionForm", descriptor.getDefaultCompletionForm());
		model.addAttribute("patientForms", patientForms);
		model.addAttribute("showClinicalData", showClinicalData);
		model.addAttribute("patientIsEligible", patientIsEligible);
		model.addAttribute("currentEnrollment", currentEnrollment);
		model.addAttribute("enrollments", enrollments);
	}
}
