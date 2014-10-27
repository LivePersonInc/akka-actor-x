akka-actor-x
============

Akka extension providing features that help you trace, debug and monitor your actor system. Actor X is written in java, it is extendable and currently provides features like: 

- Slf4j MDC: Automatically adds actor path to MDC (under akkaSource key)
- Message trail (history): Makes message trail (history) available during debugging (break-point) and/or logging
- Correlation id: Passes (maintains) correlation ids throught the actor system.
- Actor system graph (network): Discovers all of the actors and messages passed around.


<br>Actor-X is leverages AspectJ and thus you do **NOT** have to change anything inside your code.
<br>Just add aspectj weaver as VM options, for example:
<br>-javaagent:"%USER_HOME%\.m2\repository\org\aspectj\aspectjweaver\1.8.1\aspectjweaver-1.8.1.jar"
