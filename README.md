akka-actor-x
============

Akka extension providing features that help you trace, debug and monitor your actor system. Actor X is written in java, it is extendable and currently provides features like: 

- Slf4j MDC: Automatically adds actor path to MDC (under akkaSource key)
- Message trail (history): Makes message trail (history) available during debugging (break-point) and/or logging
- Correlation id: Passes (maintains) correlation ids throught the actor system.
- Actor system graph (network): Discovers all of the actors and messages passed around.


<br>Actor-X leverages AspectJ and thus you do **NOT** have to change anything inside your code.
<br>In order to run a project with actor-x you must do the following steps:
<br>
###Add maven dependency  
```xml
<dependency>  
  <groupId>com.liveperson</groupId>  
  <artifactId>akka-actor-x</artifactId>  
  <version>0.1.1</version>  
</dependency>  
```  
###Enable aspectj load time weaver
Add to VM options the path to aspectjweaver jar, for example:
 
```java
  -javaagent:~\.m2\repository\org\aspectj\aspectjweaver\1.8.1\aspectjweaver-1.8.1.jar
```

###Add Actor-X as akka extension and configure it
Inside akka configuration file define actor-x as akka extension:

```java
  extensions = ["com.liveperson.infra.akka.actorx.extension.ActorXExtensionProvider"]
```

###You can enable/disable actor-x roles by editing akka configuration file
Following is an example of akka configuration file containing actor-x:
<br>(You can also see a demo inside test package (<a href="src/test/java/com/liveperson/infra/common/akka/actorx/demo/BlackJack.java">BlackJack</a>) and play with the test configuration (<a href="src/test/resources/application.conf">application.conf</a>))

```yml
akka {

  loglevel = "DEBUG"
  loggers = ["akka.event.slf4j.Slf4jLogger"]
  event-handlers = ["akka.event.slf4j.Slf4jEventHandler"]
  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"

  # Actor-X extension
  # Without this, configuration will NOT be read
  extensions = ["com.liveperson.infra.akka.actorx.extension.ActorXExtensionProvider"]
}


#############################
#   Actor-X Configuration   #
#############################
actor-x {

  # String list of packages
  # These packages and sub packages will be candidated for enhancement with actor-x capabilities
  # If parameter is emitted or value is "*" then all packages in classpath are candidates for enhancement
  #enhanced-packages = ["*"]

  # String list of packages
  # These packages and sub packages will be excluded from enhancement with actor-x capabilities
  # By default, "akka" package added to this list
  #enhanced-packages-exclude

  # List of roles to that take part in the actor-x enhancement
  roles {

    # Akka Source MDC Role
    # Adds actor self path to MDC under "akkaSource" key
    # Default is false (not enabled)
    akka-source-mdc {
      active = true
    }

    # Correlation Role
    # In charge of delegating correlation ids throughout the akka system
    # Default is false (not enabled)
    correlation {
      active = true

      # New request feature
      # Adds a new random correlation id with specified name, if such correlation does not already exist
      # Default is false (not enabled)
      create-new-request = true
      create-new-header-name = "GAME"
    }

    # Message Trail Role
    # Default is false (not enabled)
    message-trail {
      active = true

      # Max message trail history
      # Default is 15
      max-history = 15

      # Trace Logging
      # Prints to log file, in trace level, the message trail when a message is received
      # You can include/exclude packages/messages in order to fine grain where the message trail is automatically printed
      # Notice: package "com.liveperson.infra.akka.actorx.role.MessageTrailRole" needs to be configured to TRACE level in your logging configuration in order to see logging
      # Default is false (not enabled)
      trace-logging {
        active = true

        # String list of packages
        # If parameter is emitted or value is "*" then trail is printed for all packages in classpath
        packages-include = ["*"]

        # String list of packages
        # By default, "akka" package is added to this list
        packages-exclude = ["akka"]

        # String list of packages
        # If parameter is emitted or value is "*" then trail is printed for all messages
        #message-include

        # String list of packages
        # By default, "akka" package is added to this list
        #message-exclude
      }
    }

    # Cast Tracing
    # Enables to trace the akka system network graph and to print it to log
    # Default is false (not enabled)
    cast-trace {
      active = true
    }
  }
}
```
