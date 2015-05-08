## 0.3.1

- Move user-provided protocol impls after generic
  This allows the user to provide their own versions of start, stop, or
  status for systems.

- Update api-builder and build tool versions

- Bump prismatic schema to version 0.4.0

## 0.3.0

- Add type extenders for component interop
  Adds extend-leaven and extend-component for converting component types to
  Leaven and Components, respectively.

- Add :depends option
  Adds an option to specify dependencies of a system sub-component using a
  declarative map.  The map is used to ensure dependencies are updated when
  a system starts or stops.

- Ensure deepest exception is propagated
  When an exception occurs on a system operation, ensure the returned system
  map is from the deepest exception (and has the most correct state).

- Add options to defsystem
  Allow passing of per sub-component on-start and on-stop functions to
  defsystem.  This enables, for example, propogation of the started
  component to other sub-components.

  Adds an update-components function that can be used to update dependent
  components and can be used as an on-start function.

## 0.2.1

- Use symbols for components in defsystem
  Makes defsystem consistent with defrecord and deftype.

  Closes #3

## 0.2.0

- Break ILifecycle into Startable and Stoppable
  Allow components to implement `start` and `stop` individually as required.

  This is a breaking change.

  Closes #1

## 0.1.2

- Allow defsystem to take a body
  The body is forwarded to the defrecord's body, in order to allow you to
  implement other protocols on your system.

## 0.1.1

- Try and include some files in the jar this time!

## 0.1.0

- Initial release
