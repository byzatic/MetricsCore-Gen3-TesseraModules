# SharedResources Project Common

This module provides shared components for the Tessera Workflow system, including a DSL runtime, DTOs for inter-module communication, and local storage abstractions.

## DSL (WorkflowRoutine DSL) [more about...](README_DSL.md)
The `dsl` package defines the ANTLR-generated infrastructure used to parse and interpret the custom DSL for `WorkflowRoutine`, a building block of the pipeline executed within graph nodes in Tessera.

**Classes:**
- `MyDslLexer`: Lexer for the DSL grammar.
- `MyDslParser`: Parser for the DSL grammar.
- `MyDslBaseListener`: Base listener with default behavior used during parsing.
- `MyDslListener`: Interface for creating parse tree listeners.

## DTO (Data Transfer Objects)
The `dto` package defines objects used for communication between `WorkflowRoutine` instances and between `WorkflowRoutine` and internal `Service`'s.

**Classes:**
- `DataItem`: Represents a unit of data transferred between routines or to a service.
- `MetricLabel`: Represents labeled metric metadata.

## Storage (Local Workflow Storage)
The `storage` package implements local key-value storage mechanisms used by both `WorkflowRoutine` components and embedded services within Tessera.

**Classes:**
- `LocalKeyValueStorageInterface`: Abstraction for a local key-value store.
- `LocalKeyValueStorage`: Default implementation of the key-value store using an in-memory or file-backed approach.

---
This module is intended for internal use within the Tessera platform.