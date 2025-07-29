# Tessera MCg3 Workflow DSL

This DSL (Domain-Specific Language) is used to define execution logic for `WorkflowRoutine` components in the Tessera data flow engine. It enables routines to fetch data, process it, and store results using a concise and readable syntax.

## DSL Command Use Cases

### 1. Get Data from Storage
**Purpose:** Retrieve data from the local node or from a child node.

**Syntax:**
```dsl
GET FROM SELF STORAGE localStorage MODIFIER local BY DATA ID inputId AS alias;
GET FROM CHILD childNodeId STORAGE localStorage BY DATA ID inputId;
```
**Explanation:**
- `SELF` or `CHILD <id>` indicates the data source.
- `STORAGE` specifies the logical storage to access.
- `MODIFIER` can be `local` (visible only within routine) or `global`.
- `BY DATA ID` gives the key.
- `AS` provides a local alias for use in processing.

### 2. Process Data via Function
**Purpose:** Apply a named function to previously fetched or static data.

**Syntax:**
```dsl
PROCESS FUNCTION functionName("arg1", "arg2") RETURN resultId;
PROCESS FUNCTION noop RETURN result;
```
**Explanation:**
- Calls a function named `functionName` with optional string arguments.
- The result is stored under the identifier `resultId`.

### 3. Put Data into Storage
**Purpose:** Save data back into the workflow node’s storage.

**Syntax:**
```dsl
PUT DATA resultId TO STORAGE localStorage MODIFIER global BY DATA ID outputId;
```
**Explanation:**
- Saves the data identified by `resultId` into the specified storage.
- `MODIFIER` determines the visibility of the saved data.
- The data is saved under `outputId`.

---
This DSL is part of the execution layer in Tessera and is designed to be embedded inside workflow nodes (WorkflowRoutine).