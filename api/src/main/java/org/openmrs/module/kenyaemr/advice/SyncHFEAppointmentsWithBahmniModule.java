/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.springframework.aop.AfterReturningAdvice;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;


import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Synchronizes appointments documented in HTML forms with Bahmni appointments module
 * Invoked after saving HFE forms
 */
public class SyncHFEAppointmentsWithBahmniModule implements AfterReturningAdvice {

    private Log log = LogFactory.getLog(this.getClass());

    public static final String NEXT_CLINICAL_APPOINTMENT_CONCEPT_UUID = "5096AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String NEXT_DRUG_REFILL_APPOINTMENT_CONCEPT_UUID = "162549AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    public static final String HIV_FOLLOWUP_SERVICE = "885b4ad3-fd4c-4a16-8ed3-08813e6b01fa";

    public static final String DRUG_REFILL_SERVICE = "a96921a1-b89e-4dd2-b6b4-7310f13bbabe";
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

        String deliveryOutcomeGroupingConcept = "162588AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        AppointmentsService appointmentsService = Context.getService(AppointmentsService.class);
        ObsService obsService = Context.getObsService();
        ConceptService conceptService = Context.getConceptService();
        PersonService personService = Context.getPersonService();
        if (method.getName().equals("saveEncounter")) { // handles both create and edit
            Encounter enc = (Encounter) args[0];

            if(enc != null && enc.getForm() != null &&
                    (enc.getForm().getUuid().equals(HivMetadata._Form.HIV_GREEN_CARD) || enc.getForm().getUuid().equals(HivMetadata._Form.MOH_257_VISIT_SUMMARY))) {      // pick HIV followup forms
                boolean errorOccured = false;
                Person parent = personService.getPerson(enc.getPatient().getPersonId());
                // Get appointment obs
                List<Obs> obs = obsService.getObservations(
                        Arrays.asList(personService.getPerson(enc.getPatient().getPersonId())),
                        Arrays.asList(enc),
                        Arrays.asList(
                                conceptService.getConceptByUuid(NEXT_CLINICAL_APPOINTMENT_CONCEPT_UUID),
                                conceptService.getConceptByUuid(NEXT_DRUG_REFILL_APPOINTMENT_CONCEPT_UUID)
                                ),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false
                );

                for(Obs o: obs) { // Loop through the obs and compose Appointment object for Bahmni
                    Date appointmentDate = null;
                    AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();

                    if (o.getConcept().getUuid().equals(NEXT_CLINICAL_APPOINTMENT_CONCEPT_UUID)) { // HIV follow-up appointment
                        appointmentDate = o.getValueDatetime();
                        appointmentServiceDefinition.setAppointmentServiceId(Context.getService(AppointmentServiceDefinitionService.class).getAppointmentServiceByUuid(HIV_FOLLOWUP_SERVICE).getId());
                    } else if (o.getConcept().getUuid().equals(NEXT_DRUG_REFILL_APPOINTMENT_CONCEPT_UUID)) { // Drug refill appointment
                        appointmentDate = o.getValueDatetime();
                        appointmentServiceDefinition.setAppointmentServiceId(Context.getService(AppointmentServiceDefinitionService.class).getAppointmentServiceByUuid(DRUG_REFILL_SERVICE).getId());
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Appointment appointment = new Appointment();
                    appointment.setPatient(enc.getPatient());
                    appointment.setService(appointmentServiceDefinition);
                    Date startDateTime = DateUtil.convertToDate(dateFormat.format(appointmentDate).concat("T06:00:00.0Z"), DateUtil.DateFormatType.UTC);
                    Date endDateTime = DateUtil.convertToDate(dateFormat.format(appointmentDate).concat("T20:00:00.0Z"), DateUtil.DateFormatType.UTC);
                    appointment.setStartDateTime(startDateTime);
                    appointment.setEndDateTime(endDateTime);
                    appointment.setAppointmentKind(AppointmentKind.Scheduled);
                    Appointment app = appointmentsService.validateAndSave(appointment);

                }

            }
        }

   }
}




