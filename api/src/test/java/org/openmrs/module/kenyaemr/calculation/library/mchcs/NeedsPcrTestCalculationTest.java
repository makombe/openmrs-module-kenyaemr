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

package org.openmrs.module.kenyaemr.calculation.library.mchcs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.kenyacore.test.TestUtils;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.MchMetadata;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Tests for {@link NeedsPcrTestCalculation}
 */
public class NeedsPcrTestCalculationTest extends BaseModuleContextSensitiveTest {

	@Autowired
	private CommonMetadata commonMetadata;

	@Autowired
	private MchMetadata mchMetadata;

	/**
	 * Setup each test
	 */
	@Before
	public void setup() throws Exception {
		executeDataSet("dataset/test-concepts.xml");

		commonMetadata.install();
		mchMetadata.install();
	}

	/**
	 * @see NeedsPcrTestCalculation#evaluate(java.util.Collection, java.util.Map, org.openmrs.calculation.patient.PatientCalculationContext)
	 * @verifies calculate last recorded pcr test for infant patients
	 */
	@Test
	public void evaluate_shouldCalculatePcrTest() throws Exception {
		//get mchcs program
		Program mchcsProgram = MetadataUtils.existing(Program.class, MchMetadata._Program.MCHCS);
		//set the birth date of patient #6 to be above 6 weeks and below or equal to 9 months
		TestUtils.getPatient(6).setBirthdate(TestUtils.date(2015, 1, 1));

		// Enroll patients #6 and  #7  in the mchcs Program
		TestUtils.enrollInProgram(TestUtils.getPatient(6), mchcsProgram, TestUtils.date(2015, 4, 1));
		TestUtils.enrollInProgram(TestUtils.getPatient(7), mchcsProgram, TestUtils.date(2015, 1, 1));
		TestUtils.enrollInProgram(TestUtils.getPatient(8), mchcsProgram, TestUtils.date(2014, 1, 1));

		//get the HIV status of the infant and the if wheather pcr was done or NOT
		Concept infantHivStatus = Dictionary.getConcept(Dictionary.CHILDS_CURRENT_HIV_STATUS);
		Concept pcrStatus = Dictionary.getConcept(Dictionary.HIV_DNA_POLYMERASE_CHAIN_REACTION);

		//make patient #6 be HEI and has no pcr results
		TestUtils.saveObs(TestUtils.getPatient(6),infantHivStatus,Dictionary.getConcept(Dictionary.EXPOSURE_TO_HIV), TestUtils.date(2015, 4, 1));
		//TestUtils.saveObs(ps.getPatient(6),pcrStatus,Dictionary.getConcept(null),new Date());

		//make patient #7 be HEI and has has pcr results
		TestUtils.saveObs(TestUtils.getPatient(7),infantHivStatus,Dictionary.getConcept(Dictionary.EXPOSURE_TO_HIV), TestUtils.date(2015, 4, 1));
		TestUtils.saveObs(TestUtils.getPatient(7),pcrStatus,Dictionary.getConcept(Dictionary.HIV_DNA_POLYMERASE_CHAIN_REACTION), TestUtils.date(2015, 4, 1));

		Context.flushSession();

		List<Integer> ptIds = Arrays.asList(6, 7,8);
		PatientCalculationService service = Context.getService(PatientCalculationService.class);
		PatientCalculationContext context = service.createCalculationContext();
		context.setNow(TestUtils.date(2015, 4, 30));

		CalculationResultMap resultMap =  new NeedsPcrTestCalculation().evaluate(ptIds, null, context);
		Assert.assertTrue((Boolean) resultMap.get(6).getValue()); // HEI and has null pcr
		Assert.assertFalse((Boolean) resultMap.get(7).getValue()); // in HEI but pcr is done
		Assert.assertFalse((Boolean) resultMap.get(8).getValue()); // Not HEI
	}
}