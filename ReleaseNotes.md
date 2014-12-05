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
