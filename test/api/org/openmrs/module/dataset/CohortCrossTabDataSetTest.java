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
package org.openmrs.module.dataset;

import java.util.Date;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.definition.CohortDefinition;
import org.openmrs.module.cohort.definition.PatientCharacteristicCohortDefinition;
import org.openmrs.module.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.cohort.definition.util.CohortExpressionParser;
import org.openmrs.module.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.dataset.definition.CohortDataSetDefinition;
import org.openmrs.module.evaluation.EvaluationContext;
import org.openmrs.module.evaluation.parameter.Parameter;
import org.openmrs.module.report.ReportData;
import org.openmrs.module.report.ReportSchema;
import org.openmrs.module.report.renderer.CsvReportRenderer;
import org.openmrs.module.report.service.ReportService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

/**
 *
 */
public class CohortCrossTabDataSetTest extends BaseContextSensitiveTest {
	
	/**
	 * TODO Add javadoc What the heck is this for?
	 * @param text
	 * @return
	 */
	public CohortDefinition getStrategy(String text) {
		String query = Context.getService(ReportService.class).applyReportXmlMacros(text);
		return CohortExpressionParser.parse(query);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	@SkipBaseSetup
	public void shouldTest() throws Exception {
		
		CohortDefinitionService cohortDefinitionService = Context.getService(CohortDefinitionService.class);
		ReportService reportService = Context.getService(ReportService.class);
		
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/report/include/ReportTests-patients.xml");
		authenticate();
		
		ReportSchema schema = new ReportSchema();
		schema.setName("Test Report for Table");
		schema.setDescription("A test description");
		
		Parameter dateParam = new Parameter("report.startDate", "Date of report", Date.class, new Date(), false);
		schema.addParameter(dateParam);
		
		PatientCharacteristicCohortDefinition maleDef = new PatientCharacteristicCohortDefinition();
		maleDef.setName("Male");
		maleDef.setGender("M");
		cohortDefinitionService.saveCohortDefinition(maleDef);
		
		PatientCharacteristicCohortDefinition femaleDef = new PatientCharacteristicCohortDefinition();
		femaleDef.setName("Female");
		femaleDef.setGender("F");
		cohortDefinitionService.saveCohortDefinition(femaleDef);
		
		Parameter effDateParam = 
			new Parameter("effectiveDate", "Effective Date", Date.class, null, false);
		
		PatientCharacteristicCohortDefinition adultOnDate = new PatientCharacteristicCohortDefinition();
		adultOnDate.setName("AdultOnDate");
		adultOnDate.setMinAge(15);
		adultOnDate.addParameter(effDateParam);
		cohortDefinitionService.saveCohortDefinition(adultOnDate);
		
		PatientCharacteristicCohortDefinition childOnDate = new PatientCharacteristicCohortDefinition();
		childOnDate.setName("ChildOnDate");
		childOnDate.setMaxAge(14);
		childOnDate.addParameter(effDateParam);
		cohortDefinitionService.saveCohortDefinition(childOnDate);

		CohortDataSetDefinition genderDef = new CohortDataSetDefinition();
		genderDef.setName("gender");
		genderDef.addStrategy("male", maleDef, null);
		genderDef.addStrategy("female", femaleDef, null);
		
		CohortDataSetDefinition ageDef = new CohortDataSetDefinition();
		ageDef.setName("age");
		ageDef.addStrategy("adult", adultOnDate, null);
		ageDef.addStrategy("child", childOnDate, null);
		
		CohortCrossTabDataSetDefinition def = new CohortCrossTabDataSetDefinition();
		def.setName("test");
		def.setRowCohortDataSetDefinition(ageDef, null);
		def.setColumnCohortDataSetDefinition(genderDef, null);
		schema.addDataSetDefinition(def, (String)null);
		

		
		EvaluationContext evalContext = new EvaluationContext();
		evalContext.addParameterValue(dateParam.getName(), new Date());
		
		ReportData data = reportService.evaluate(schema, evalContext);
		System.out.println("Result=");

		new CsvReportRenderer().render(data, null, System.out);
	}
	
}