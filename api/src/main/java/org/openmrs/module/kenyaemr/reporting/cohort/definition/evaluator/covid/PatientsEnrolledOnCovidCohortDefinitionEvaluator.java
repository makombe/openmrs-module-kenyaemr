/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.reporting.cohort.definition.evaluator.covid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.OTZRegisterCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.covid.PatientsEnrolledOnCovidCohortDefinition;
import org.openmrs.module.kenyaemr.util.EmrUtils;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Evaluator for Covid
 */
@Handler(supports = {PatientsEnrolledOnCovidCohortDefinition.class})
public class PatientsEnrolledOnCovidCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    private final Log log = LogFactory.getLog(this.getClass());
    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {

        PatientsEnrolledOnCovidCohortDefinition definition = (PatientsEnrolledOnCovidCohortDefinition) cohortDefinition;

        if (definition == null)
            return null;

        Cohort newCohort = new Cohort();

        context = ObjectUtil.nvl(context, new EvaluationContext());

        String qry = "";
        SqlQueryBuilder builder = new SqlQueryBuilder();

        qry = "Select e.patient_id from kenyaemr_etl.etl_covid_19_enrolment e\n" +
                "group by e.patient_id having date(max(e.visit_date)) between date(:startDate) and date(:endDate);";

        /*if (EmrUtils.getUserCounty() != null) {
            qry = "Select e.patient_id from kenyaemr_etl.etl_covid_19_enrolment e where e.county=:userCounty\n" +
                    "group by e.patient_id having date(max(e.visit_date)) between date(:startDate) and date(:endDate);";
            builder.addParameter("userCounty", EmrUtils.getUserCounty().trim());

        } else {
            qry = "Select e.patient_id from kenyaemr_etl.etl_covid_19_enrolment e\n" +
                    "group by e.patient_id having date(max(e.visit_date)) between date(:startDate) and date(:endDate);";
        }*/
        builder.append(qry);
        Date startDate = (Date) context.getParameterValue("startDate");
        Date endDate = (Date) context.getParameterValue("endDate");
        String county = (String) context.getParameterValue("county");
        System.out.println("county=============================="+county);
        System.out.println("endDate=============================="+endDate);
        System.out.println("startDate=============================="+startDate);
        builder.addParameter("endDate", endDate);
        builder.addParameter("startDate", startDate);

        List<Integer> ptIds = evaluationService.evaluateToList(builder, Integer.class, context);
        newCohort.setMemberIds(new HashSet<Integer>(ptIds));

        return new EvaluatedCohort(newCohort, definition, context);
    }

}
