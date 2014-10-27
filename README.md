akka-actor-x
============

Akka extension providing features that help you trace, debug and monitor your actor system. Actor X is extendable and currently provides features like: 

- Slf4j MDC: Automatically adds actor path to MDC (under akkaSource key)
- Message trail (history): Makes message trail (history) available during debugging (break-point) and/or logging
- Correlation id: Passes (maintains) correlation ids throught the actor system.
- Actor system graph (network): Discovers all of the actors and messages passed around.

