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
package org.openmrs.module.dataset.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.dataset.CohortDataSet;
import org.openmrs.module.dataset.DataSet;
import org.openmrs.module.dataset.MapDataSet;
import org.openmrs.module.dataset.column.DataSetColumn;
import org.openmrs.module.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.dataset.definition.CohortDataSetDefinition;
import org.openmrs.module.dataset.definition.DataSetDefinition;
import org.openmrs.module.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.evaluation.EvaluationContext;

/**
 * The logic that evaluates a {@link CohortDataSetDefinition} and produces a {@link CohortDataSet}
 * 
 * @see CohortDataSetDefinition
 * @see CohortDataSet
 */
@Handler(supports={CohortCrossTabDataSetDefinition.class})
public class CohortCrossTabDataSetEvaluator implements DataSetEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Default Constructor
	 */
	public CohortCrossTabDataSetEvaluator() { }
	
	/**
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 */
	public DataSet<?> evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) {
		
		if (context == null) {
			context = new EvaluationContext();
		}
		
		MapDataSet<Cohort> data = new MapDataSet<Cohort>();
		data.setDataSetDefinition(dataSetDefinition);
		data.setEvaluationContext(context);
		data.setName(dataSetDefinition.getName());

		CohortCrossTabDataSetDefinition crossTabDef = (CohortCrossTabDataSetDefinition) dataSetDefinition;
		
		DataSetDefinitionService dds = Context.getService(DataSetDefinitionService.class);
		
		@SuppressWarnings("unchecked")
		MapDataSet<Cohort> rowData = (MapDataSet<Cohort>) dds.evaluate(crossTabDef.getRowCohortDataSetDefinition(), context);
		@SuppressWarnings("unchecked")
		MapDataSet<Cohort> colData = (MapDataSet<Cohort>) dds.evaluate(crossTabDef.getColumnCohortDataSetDefinition(), context);
		
		for (DataSetColumn rowDataCol : rowData.getDataSetDefinition().getColumns()) {
			for (DataSetColumn colDataCol : colData.getDataSetDefinition().getColumns()) {
				Cohort rowCohort = rowData.getData().get(rowDataCol);
				Cohort colCohort = colData.getData().get(colDataCol);
				String key = rowDataCol.getKey() + crossTabDef.getRowColumnDelimiter() + colDataCol.getKey();
				data.addData(key, Cohort.intersect(rowCohort, colCohort));
			}
		}

		return data;
	}
}