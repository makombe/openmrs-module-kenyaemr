package org.openmrs.module.kenyaemr.reporting.data.converter.definition.evaluator.pnc;

import org.openmrs.annotation.Handler;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.pnc.PNCPartnerHIVResultsDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.pnc.PNCPlaceOfDeliveryDataDefinition;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.evaluator.EncounterDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * PNC Place of delivery column
 */
@Handler(supports= PNCPlaceOfDeliveryDataDefinition.class, order=50)
public class PNCPlaceOfDeliveryDataEvaluator implements EncounterDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedEncounterData evaluate(EncounterDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedEncounterData c = new EvaluatedEncounterData(definition, context);

        String qry = "select v.encounter_id,\n" +
                "(case v.place_of_delivery when 1589 then \"Facility\" when 1536 then \"Home\" when 5622 then \"Other\" else \"\" end) as place_of_delivery\n" +
                "from kenyaemr_etl.etl_mch_postnatal_visit v inner join kenyaemr_etl.etl_mch_enrollment e on v.patient_id = e.patient_id and e.date_of_discontinuation IS NULL\n" +
                "GROUP BY v.encounter_i";

        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        queryBuilder.append(qry);
        Map<Integer, Object> data = evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class, context);
        c.setData(data);
        return c;
    }
}
