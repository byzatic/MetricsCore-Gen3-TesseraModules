# Data Enrichment WorkflowRoutine

`DataEnrichmentWorkflowRoutine` is a DSL-enabled routine designed to enhance or modify existing `DataItem` metrics in a Tessera pipeline. It provides configurable transformations such as adding labels, modifying values, or attaching graph path metadata.

## Purpose
This routine allows controlled enrichment of metric data flowing through the DAG. It can tag, reclassify, or override values before passing them on, making it useful for post-processing or preparing metrics for service integration.

## DSL Function Support
The following `PROCESS FUNCTION` operations are supported:

### 1. `AddGraphPath`
- **Description:** Appends the current graph execution path to the metric as a label.
- **Arguments:**
  - `DataId` — Local alias of the data to be updated.
- **Effect:**
  - Adds a label like `graph_path=...` based on the current routine's graph path.

### 2. `AddLabel`
- **Description:** Adds a custom label to a metric.
- **Arguments:**
  - `DataId` — The target data item.
  - `LabelKey` — Label name.
  - `LabelValue` — Label value.
- **Effect:**
  - Appends a new `MetricLabel` to the given data item.

### 3. `ModifyMetric`
- **Description:** Changes the metric value based on supplied input.
- **Arguments:**
  - `DataId` — The metric to modify.
  - `NewValue` — New metric value (as string).
- **Effect:**
  - Replaces the numeric value of the `DataItem`.

## Factory
### `DataEnrichmentWorkflowRoutineFactory`
- Registers the routine under a symbolic name in Tessera.
- Supports dynamic instantiation and configuration.

## Example DSL Usage
```dsl
PROCESS FUNCTION AddLabel("DataId=my_metric", "LabelKey=source", "LabelValue=generated") RETURN tagged_metric;
PROCESS FUNCTION ModifyMetric("DataId=tagged_metric", "NewValue=42") RETURN updated_metric;
PROCESS FUNCTION AddGraphPath("DataId=updated_metric") RETURN enriched_metric;
```

---
This routine enables lightweight and flexible enrichment of metrics directly within Tessera DAG pipelines.