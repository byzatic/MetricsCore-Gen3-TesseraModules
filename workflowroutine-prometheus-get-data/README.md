# Prometheus Get Data WorkflowRoutine

This `WorkflowRoutine` implementation connects to Prometheus via the PQletta interface and enables DSL-driven querying and transformation of metrics. It is part of the Tessera DAG execution graph.

## Purpose
`PrometheusGetDataWorkflowRoutine` allows fetching and transforming Prometheus data using `PROCESS FUNCTION` statements in the DSL. It provides users a way to retrieve and pre-process metric values directly within the workflow pipeline.

## DSL Function Support
The routine supports the following `PROCESS FUNCTION` operations:

### 1. `GetData`
- **Description:** Executes a PromQL query via PQletta using a query ID.
- **Arguments:**
  - `PQletaQueryId` — Identifier of a predefined PromQL template in the PQletta config.
- **Effect:**
  - Queries Prometheus and stores result as a `DataItem`.
  - Handles success and failure responses, attaches a `reason` label on failure.

### 2. `RemoveServiceLabel`
- **Description:** Cleans up technical/internal labels from the metric.
- **Arguments:**
  - `FromDataId` — Local data key where the original metric is stored.
- **Effect:**
  - Removes metric labels with keys or values matching `__.*__`.

### 3. `ProcessReason`
- **Description:** Attaches a human-readable reason to the metric based on its value.
- **Arguments:**
  - `DataId` — The ID of the `DataItem` to annotate.
  - `PasteReasonWhenOk` — Whether to add a reason for OK (0) state.
  - `PrometheusEmptyData` — Message for empty Prometheus response (3).
  - `IgnoreExistsReason` — If false, will throw if `reason` already present.
  - `OkReasonMessage`, `WarningReasonMessage`, `AlarmReasonMessage`, `GlobalReasonMessage` — Custom messages for various states.
- **Effect:**
  - Adds a `reason` label based on metric value: 0 = OK, 1 = Warning, 2 = Alarm, 3 = Empty.

### 4. `PruneLabelsExceptBy`
- **Description:** Keeps only a selected set of labels.
- **Arguments:**
  - `DataId` — ID of the metric to process.
  - `ExceptedLabel` (multi-value) — Keys of labels to retain.
- **Effect:**
  - Removes all labels except those explicitly allowed.

## Factory
### `PrometheusGetDataWorkflowRoutineFactory`
- Registers the routine in Tessera and enables its instantiation based on configuration.

## Example DSL Usage
```dsl
PROCESS FUNCTION GetData("PQletaQueryId=cpu_usage") RETURN cpu_metric;
PROCESS FUNCTION RemoveServiceLabel("FromDataId=cpu_metric") RETURN cleaned;
PROCESS FUNCTION ProcessReason("DataId=cleaned", "PasteReasonWhenOk=true", "OkReasonMessage=OK") RETURN annotated;
PROCESS FUNCTION PruneLabelsExceptBy("DataId=annotated", "ExceptedLabel=instance") RETURN final;
```

---
This routine is a powerful bridge between DSL scripts and Prometheus query capabilities, enabling clean, semantic, and annotated metrics to flow through the Tessera graph.