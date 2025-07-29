# Graph Lifting Data WorkflowRoutine

`GraphLiftingDataWorkflowRoutine` is a powerful DSL-enabled routine in the Tessera pipeline engine that not only operates on local pipeline data, but also lifts data from storages of other graph nodes.

## Purpose
This routine enables cross-graph inspection and analysis by accessing storage objects from different nodes' execution contexts. It is particularly useful for routines that need to perform validation, aggregation, or diagnostic logic across the DAG.

## DSL Function Support
The routine supports the following `PROCESS FUNCTION` operations:

### 1. `LiftDataFromNode`
- **Description:** Lifts a data object from another node's pipeline execution storage.
- **Arguments:**
  - `NodeId` — ID of the target graph node.
  - `StorageId` — Logical storage name.
  - `DataId` — Key of the data item in the remote storage.
  - `Alias` — Local name for referencing the lifted data.
- **Effect:**
  - Loads data into the current routine’s local context from an external node’s storage.

### 2. `LiftBatch`
- **Description:** Performs batch lifting of multiple data items from various graph nodes.
- **Arguments:**
  - `BatchLiftingDescriptor` — Path to a descriptor file (YAML/JSON) defining a lifting matrix.
- **Effect:**
  - Automates lifting of multiple items based on external configuration.

### 3. `AnnotateDataWithSource`
- **Description:** Adds metadata or labels to indicate the origin of lifted data.
- **Arguments:**
  - `DataId` — The local alias of the lifted data.
  - `SourceNodeId` — The originating node’s ID.
- **Effect:**
  - Embeds traceability into the lifted data by tagging it with its source.

## Factory
### `GraphLiftingDataWorkflowRoutineFactory`
- Registers the routine within Tessera.
- Enables configuration-based and dynamic instantiation.

## DSL Example
```dsl
PROCESS FUNCTION LiftDataFromNode("NodeId=validate-temperature", "StorageId=metrics", "DataId=temp_avg", "Alias=temp") RETURN lifted;
PROCESS FUNCTION AnnotateDataWithSource("DataId=lifted", "SourceNodeId=validate-temperature") RETURN tagged;
```

## GET and PUT Command Support

`GraphLiftingDataWorkflowRoutine` supports the full spectrum of DSL storage operations defined by the `GET` and `PUT` commands, allowing it to manipulate data within and across execution contexts.

### `GET FROM SELF`
```dsl
GET FROM SELF STORAGE localStorage MODIFIER local BY DATA ID inputId AS alias;
```
- **Purpose:** Reads a data item from the routine's own pipeline storage.
- **Use Case:** Used when lifting or transforming local data within the same node.

### `GET FROM CHILD`
```dsl
GET FROM CHILD childNodeId STORAGE localStorage BY DATA ID inputId;
```
- **Purpose:** Lifts data from the storage of a *child node* in the DAG.
- **Effect:** Accesses runtime outputs from other graph nodes as part of a lifting operation.
- **Use Case:** Enables cross-node inspection or aggregation — for example, gathering results from multiple validation nodes.

### `PUT DATA ... MODIFIER global`
```dsl
PUT DATA resultId TO STORAGE localStorage MODIFIER global BY DATA ID outputId;
```
- **Purpose:** Stores the result in the *global* storage scope of the current node.
- **Use Case:** Typically used to pass data to services within the Tessera engine that consume global-scope results.

### `PUT DATA ... MODIFIER local`
```dsl
PUT DATA resultId TO STORAGE localStorage MODIFIER local BY DATA ID outputId;
```
- **Purpose:** Stores the result in the *local* pipeline scope of the current node.
- **Use Case:** Suitable for passing data between routines within the same node's execution graph.

---

This routine expands the DSL with cross-node visibility, enabling rich coordination and analysis in Tessera workflows.