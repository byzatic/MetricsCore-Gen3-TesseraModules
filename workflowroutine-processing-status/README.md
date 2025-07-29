# Processing Status WorkflowRoutine

`ProcessingStatusWorkflowRoutine` is a DSL-enabled routine designed to finalize and annotate `DataItem` objects with processing status and context labels in a Tessera pipeline.

## Purpose
This routine is typically used at the end of a DAG to assign terminal status values and attach traceable reason labels to data metrics. It enhances interpretability and enables services or downstream routines to make decisions based on processing outcome.

## DSL Function Support
The following `PROCESS FUNCTION` operations are supported:

### 1. `ProcessStatus`
- **Description:** Attaches a numeric status value and a human-readable reason to a `DataItem`.
- **Arguments:**
  - `DataId` — Local alias of the `DataItem` to update.
  - `Value` — Integer value representing status (e.g., `0` = OK, `1` = Warning, `2` = Alarm, `3` = Prometheus Empty).
  - `Reason` — Optional label with human-readable explanation for traceability.
- **Effect:**
  - Sets the value of the metric and adds a `reason` label.
  - The `reason` content includes the current graph path and the supplied message.

#### Example
```dsl
PROCESS FUNCTION ProcessStatus("DataId=input", "Value=1", "Reason=Threshold breached") RETURN status_ready;
```

## Factory
### `ProcessingStatusWorkflowRoutineFactory`
- Registers the routine for use in Tessera DAG execution.
- Enables dynamic instantiation through configuration.

---
This routine is ideal for finalizing data before export, signaling end-of-processing semantics, and improving operational visibility in pipeline diagnostics.