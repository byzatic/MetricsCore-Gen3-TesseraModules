# Issue: Add `GET` and `PUT` command documentation to all WorkflowRoutine READMEs

## Description

Several `WorkflowRoutine` components support DSL commands such as:

```dsl
GET FROM SELF STORAGE ...
GET FROM CHILD ...
PUT DATA ... MODIFIER local/global ...
```

However, the corresponding `README.md` files currently focus only on `PROCESS FUNCTION` implementations and omit explanations of `GET` and `PUT` command behavior, visibility scope, and typical use cases.

## Tasks

- [ ] For each routine (`PrometheusGetData`, `GraphLiftingData`, `DataEnrichment`, `ProcessingStatus`, etc.), update the README to include:
  - Description of how `GET FROM SELF` and `GET FROM CHILD` operate in context
  - Description of `PUT DATA` with `MODIFIER local/global` and their intended scope
  - Example DSL usage
  - Clarification of how storage visibility and graph interaction work

## Motivation

This will help users understand not just the functions provided, but also the data flow mechanics and context interaction capabilities of each routine.
