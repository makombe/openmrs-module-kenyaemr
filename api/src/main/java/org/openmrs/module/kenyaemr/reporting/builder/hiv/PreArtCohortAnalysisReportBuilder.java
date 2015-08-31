/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyaemr.reporting.builder.hiv;

import org.openmrs.Program;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.kenyacore.report.CohortReportDescriptor;
import org.openmrs.module.kenyacore.report.HybridReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractHybridReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyacore.report.data.patient.definition.CalculationDataDefinition;
import org.openmrs.module.kenyaemr.calculation.TimelyLinkageCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.AgeAtProgramEnrollmentCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.DateOfDiagnosisCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.DateOfEnrollmentHivCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.InitialCd4CountCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.InitialCd4PercentCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.IsTransferInAndHasDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.IsTransferOutAndHasDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.LastReturnVisitDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateAndReasonFirstMedicallyEligibleForArtCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.DateLastSeenCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.EligibleForArtCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.IsBirthDateApproximatedCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.IsTransferInCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.IsTransferOutCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.PatientPreArtOutComeCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.TransferInDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.TransferOutDateCalculation;
import org.openmrs.module.kenyaemr.calculation.library.rdqa.DateOfDeathCalculation;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.MedicallyEligibleConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.TimelyLinkageDataConverter;
import org.openmrs.module.kenyaemr.reporting.calculation.converter.TransferInAndDateConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.Cd4ValueAndDateConverter;
import org.openmrs.module.kenyaemr.reporting.data.converter.IdentifierConverter;
import org.openmrs.module.kenyaemr.reporting.library.shared.common.CommonCohortLibrary;
import org.openmrs.module.kenyaemr.reporting.library.shared.hiv.HivCohortLibrary;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({"kenyaemr.hiv.report.cohort.analysis.preArt.6","kenyaemr.hiv.report.cohort.analysis.preArt.12","kenyaemr.hiv.report.cohort.analysis.preArt.24","kenyaemr.hiv.report.cohort.analysis.preArt.36","kenyaemr.hiv.report.cohort.analysis.preArt.48","kenyaemr.hiv.report.cohort.analysis.preArt.60"})
public class PreArtCohortAnalysisReportBuilder extends AbstractHybridReportBuilder {

	@Autowired
	private HivCohortLibrary commonCohortLibrary;

	/**
	 *
	 * @see org.openmrs.module.kenyacore.report.builder.AbstractCohortReportBuilder#getParameters(ReportDescriptor)
	 */
	@Override
	protected List<Parameter> getParameters(ReportDescriptor descriptor) {
		return Arrays.asList(
				new Parameter("startDate", "Start Date", Date.class),
				new Parameter("endDate", "End Date", Date.class)
		);
	}

	/**
	 *
	 * @see org.openmrs.module.kenyacore.report.builder.AbstractCohortReportBuilder#addColumns(CohortReportDescriptor, PatientDataSetDefinition)
	 */
	@Override
	protected void addColumns(HybridReportDescriptor report, PatientDataSetDefinition dsd) {

		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class, HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(upn.getName(), upn), new IdentifierConverter());
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);

		dsd.setName("preArtCohortAnalysis");
		dsd.addColumn("id", new PatientIdDataDefinition(), "");
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("UPN", identifierDef, "");
		dsd.addColumn("Enrollment into care date", hivProgramEnrollment(), "onDate=${endDate}", new CalculationResultConverter());
		dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter());
		dsd.addColumn("DOB approx", new CalculationDataDefinition("DOB approx", new IsBirthDateApproximatedCalculation()), "", new CalculationResultConverter());
		dsd.addColumn("Age at HIV enrollment", ageAtHivProgramEnrollment(), "onDate=${endDate}", new CalculationResultConverter());
		dsd.addColumn("Sex", new GenderDataDefinition(), "");
		dsd.addColumn("Date of Diagnosis", dateOfDiagnosis(), "onDate=${endDate}", new CalculationResultConverter());
		dsd.addColumn("Days from HIV Diagnosis to Enrollment", timelyLinkage(), "", new CalculationResultConverter());
		dsd.addColumn("Timely linkage", timelyLinkage(), "", new TimelyLinkageDataConverter());
		dsd.addColumn("Transfer in", ti(), "onDate=${endDate}", new TransferInAndDateConverter("state"));
		dsd.addColumn("Date Transfer in", ti(), "onDate=${endDate}", new TransferInAndDateConverter("date"));
		dsd.addColumn("Transfer out", to(report), "onDate=${endDate}", new TransferInAndDateConverter("state"));
		dsd.addColumn("Date Transferred out", to(report), "onDate=${endDate}", new TransferInAndDateConverter("date"));
		dsd.addColumn("Initial CD4 count", new CalculationDataDefinition("Initial CD4 count", new InitialCd4CountCalculation()), "", new Cd4ValueAndDateConverter("value"));
		dsd.addColumn("Date of initial CD4 count", new CalculationDataDefinition("Date of initial CD4 count", new InitialCd4CountCalculation()), "", new Cd4ValueAndDateConverter("date"));
		dsd.addColumn("Initial CD4 percent", new CalculationDataDefinition("Initial CD4 percent", new InitialCd4PercentCalculation()), "", new Cd4ValueAndDateConverter("value"));
		dsd.addColumn("Date of initial CD4 percent", new CalculationDataDefinition("Date of initial CD4 percent", new InitialCd4PercentCalculation()), "", new Cd4ValueAndDateConverter("date"));
		dsd.addColumn("Date first medically eligible for ART", new CalculationDataDefinition("Date first medically eligible for ART", new DateAndReasonFirstMedicallyEligibleForArtCalculation()), "", new MedicallyEligibleConverter("date"));
		dsd.addColumn("Reason first medically eligible For ART", new CalculationDataDefinition("Reason first medically eligible For ART", new DateAndReasonFirstMedicallyEligibleForArtCalculation()), "", new MedicallyEligibleConverter("reason"));
		dsd.addColumn("Medically eligible but not enrolled on ART", new CalculationDataDefinition("Medically eligible but not enrolled on ART", new EligibleForArtCalculation()), "", new CalculationResultConverter());
		dsd.addColumn("Date of Last visit", new CalculationDataDefinition("Date of Last visit", new DateLastSeenCalculation()), "", new CalculationResultConverter());
		dsd.addColumn("Date of expected next visit", new CalculationDataDefinition("Date of expected next visit", new LastReturnVisitDateCalculation()), "", new CalculationResultConverter());
		dsd.addColumn("Date of death", death(), "onDate=${endDate}", new CalculationResultConverter());
		dsd.addColumn("OutComes", patientOutComes(report), "onDate=${endDate}", new CalculationResultConverter());
	}

	private DataDefinition to(HybridReportDescriptor descriptor) {
		CalculationDataDefinition cd = new CalculationDataDefinition("to", new IsTransferOutAndHasDateCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		cd.addCalculationParameter("outcomePeriod",Integer.parseInt(descriptor.getId().split("\\.")[6]));
		return cd;
	}

	private DataDefinition ti() {
		CalculationDataDefinition cd = new CalculationDataDefinition("tiAndDate", new IsTransferInAndHasDateCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;
	}
	private DataDefinition death() {
		CalculationDataDefinition cd = new CalculationDataDefinition("death", new DateOfDeathCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;
	}
	@Override
	protected Mapped<CohortDefinition> buildCohort(HybridReportDescriptor descriptor, PatientDataSetDefinition dsd) {
		CohortDefinition cd = commonCohortLibrary.firstProgramEnrollment();
		return ReportUtils.map(cd, "onOrAfter=${startDate},onOrBefore=${endDate}");
	}

	private DataDefinition hivProgramEnrollment() {
		CalculationDataDefinition cd = new CalculationDataDefinition("careEnrollment", new DateOfEnrollmentHivCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;
	}

	private DataDefinition dateOfDiagnosis() {
		CalculationDataDefinition cd = new CalculationDataDefinition("diagnosisDate", new DateOfDiagnosisCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;
	}

	private DataDefinition ageAtHivProgramEnrollment() {
		CalculationDataDefinition cd = new CalculationDataDefinition("ageAtEnrollment", new AgeAtProgramEnrollmentCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;

	}
	private DataDefinition timelyLinkage() {
		CalculationDataDefinition cd = new CalculationDataDefinition("timelyLinkage", new TimelyLinkageCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;

	}

	private DataDefinition patientOutComes(HybridReportDescriptor descriptor) {
		int months = Integer.parseInt(descriptor.getId().split("\\.")[6]);
		CalculationDataDefinition cd = new CalculationDataDefinition("outcomes", new PatientPreArtOutComeCalculation());
		cd.addCalculationParameter("months" , months);
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;

	}
}