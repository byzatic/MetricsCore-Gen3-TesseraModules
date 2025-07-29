package io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data;
// composition package

import com.google.auto.service.AutoService;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineFactoryInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;

@AutoService(WorkflowRoutineFactoryInterface.class)
public class PrometheusGetDataWorkflowRoutineFactory implements WorkflowRoutineFactoryInterface {
    @Override
    public PrometheusGetDataWorkflowRoutine create(MCg3WorkflowRoutineApiInterface api, HealthFlagProxy healthFlagProxy) {
        try {
            return new PrometheusGetDataWorkflowRoutine(api, healthFlagProxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
