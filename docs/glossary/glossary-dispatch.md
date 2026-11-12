# Glossary — Dispatch

Task dispatch: the units of work, the functions that perform them, the classes that group those functions, and the registry that routes one to the other.

---

## Tasks and Workers

### Task
- **Definition:** A typed unit of work whose dispatch key consists of a license name and a set of property strings.
- **Example:** `ITask.ID("greet", emptySet())` with a Task payload → one Task selecting registrations for that key.
- **Source:** `ITask.kt` `ITask` `ID`
- **Reference:** none

### Worker
- **Definition:** A function that performs one Task synchronously and returns its result.
- **Example:** A Task with key greet → a Worker that returns true when it processes that Task.
- **Source:** `IWorker.kt` `IWorker`
- **Reference:** none

## Workshops and Dispatch

### Workshop
- **Definition:** A class that groups related Workers as annotated properties, shares resources through its instance and supplies registrations through reflective license discovery.
- **Example:** A class with one licensed Worker property → a Workshop exposing that Worker to registration.
- **Source:** `AbcWorkshop.kt` `AbcWorkshop`; `WorkLicense.kt` `WorkLicense`
- **Reference:** none

### Dispatcher
- **Definition:** The registry that maps each Task dispatch key to the Workers registered for it and, on a second registration of one identifier, replaces the Worker or attaches the new one so both run in registration order.
- **Example:** Register Worker A and Worker B for one key with ATTACH → one Dispatcher returning a Worker that invokes A and then B.
- **Source:** `IDispatcher.kt` `IDispatcher`, `RegisterMode`; `AbcDispatcher.kt` `AbcDispatcher`
- **Reference:** none

### Registration Mode
- **Definition:** The rule a Dispatcher applies when a key is registered again: replace its Worker or execute the registered Workers in registration order.
- **Example:** Register Worker A and then Worker B for one key with ATTACH → A executes before B, and B supplies the result when both calls return.
- **Source:** `IDispatcher.kt` `RegisterMode`; `AbcDispatcher.kt` `register`
