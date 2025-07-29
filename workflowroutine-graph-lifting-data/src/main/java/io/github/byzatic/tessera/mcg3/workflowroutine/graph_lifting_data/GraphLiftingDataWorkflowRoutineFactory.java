package io.github.byzatic.tessera.mcg3.workflowroutine.graph_lifting_data;

import com.google.auto.service.AutoService;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineFactoryInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineOperationIncompleteException;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;

@AutoService(WorkflowRoutineFactoryInterface.class)
public class GraphLiftingDataWorkflowRoutineFactory implements WorkflowRoutineFactoryInterface {
    @Override
    public WorkflowRoutineInterface create(MCg3WorkflowRoutineApiInterface mCg3WorkflowRoutineApi, HealthFlagProxy healthFlagProxy) throws WorkflowRoutineOperationIncompleteException {
        try {
            return new GraphLiftingDataWorkflowRoutine(mCg3WorkflowRoutineApi, healthFlagProxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
