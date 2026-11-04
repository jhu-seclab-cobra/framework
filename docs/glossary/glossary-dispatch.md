# Glossary — Dispatch

Task dispatch: the units of work, the functions that perform them, the classes that group those functions, and the registry that routes one to the other.

---

## Tasks and Workers

### Task
- **Definition:** A typed unit of work whose identifier, a license name and a set of property strings, selects the workers that handle it.
- **Source:** `ITask.kt` `ITask`, `ITask.ID`
- **Reference:** none

### Worker
- **Definition:** A function that performs one task synchronously and returns its result.
- **Source:** `IWorker.kt` `IWorker`
- **Reference:** none

## Workshops and Dispatch

### Workshop
- **Definition:** A class that groups related workers as annotated properties, sharing resources through its instance, and registers them with a dispatcher by reflective discovery of their licenses.
- **Source:** `AbcWorkshop.kt` `AbcWorkshop`; `WorkLicense.kt` `WorkLicense`
- **Reference:** none

### Dispatcher
- **Definition:** The registry that maps each task identifier to the workers registered for it and, on a second registration of one identifier, replaces the worker or attaches the new one so both run in registration order.
- **Source:** `IDispatcher.kt` `IDispatcher`, `RegisterMode`; `AbcDispatcher.kt` `AbcDispatcher`
- **Reference:** none
