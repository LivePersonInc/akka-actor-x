package com.liveperson.infra.akka.actorx.extension;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Amit Tal
 * @since 10/19/2014
 */

// TODO Be static or an instance of the extension (will be fetched via extension)?
public class ActorXConfig {

    private static Logger logger = LoggerFactory.getLogger(ActorXConfig.class);

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String TAB = "\t";

    private static final String ACTOR_X = "actor-x";
    private static final String ENHANCED_PACKAGES_INCLUDE = "enhanced-packages";
    private static final String ENHANCED_PACKAGES_EXCLUDE = "enhanced-packages-exclude";

    private static final String ROLES = "roles";
    private static final String ROLES_ACTIVE = "active";
    private static final String ROLES_AKKA_SOURCE_MDC = "akka-source-mdc";
    private static final String ROLES_CORRELATION = "correlation";
    private static final String ROLES_CORRELATION_CREATE_NEW_REQUEST = "create-new-request";
    private static final String ROLES_CORRELATION_CREATE_NEW_REQUEST_HEADER_NAME = "create-new-header-name";
    private static final String ROLES_MESSAGE_TRAIL = "message-trail";
    private static final String ROLES_MESSAGE_TRAIL_MAX_HISTORY = "max-history";
    private static final String ROLES_MESSAGE_TRAIL_TRACE_LOGGING = "trace-logging";
    private static final String ROLES_MESSAGE_TRAIL_TRACE_LOGGING_INCLUDE = "packages-include";
    private static final String ROLES_MESSAGE_TRAIL_TRACE_LOGGING_EXCLUDE = "packages-exclude";
    private static final String ROLES_MESSAGE_TRAIL_TRACE_LOGGING_MESSAGE_INCLUDE = "message-include";
    private static final String ROLES_MESSAGE_TRAIL_TRACE_LOGGING_MESSAGE_EXCLUDE = "message-exclude";
    private static final String ROLES_CAST_TRACE = "cast-trace";

    private static final String WILDCARD_ALL = "*";
    private static final String AKKA_PACKAGE = "akka";
    private static final String ACTOR_X_PACKAGE = "com.liveperson.infra.akka.actorx";

    // Include / Exclude packages
    private static Set<String> enhancedPackagesInclude = new HashSet<>(Arrays.asList(WILDCARD_ALL));
    private static Set<String> enhancedPackagesExclude = new HashSet<>(Arrays.asList(AKKA_PACKAGE, ACTOR_X_PACKAGE));

    // Role Correlation
    private static boolean roleCorrelationActive = false;
    private static boolean roleCorrelationCreateNewRequest = false;
    private static String roleCorrelationCreateNewRequestHeaderName = "REQUEST_ID";

    // Role Message Trail
    private static boolean roleAkkaSourceMdcActive = false;
    private static boolean roleMessageTrailActive = false;
    private static boolean roleMessageTrailTraceLogging = false;
    private static int roleMessageTrailMaxHistory = 15;
    private static Set<String> roleMessageTrailPackagesInclude = new HashSet<>(Arrays.asList(WILDCARD_ALL));
    private static Set<String> roleMessageTrailPackagesExclude = new HashSet<>(Arrays.asList(AKKA_PACKAGE, ACTOR_X_PACKAGE));
    private static Set<String> roleMessageTrailMessagesInclude = new HashSet<>(Arrays.asList(WILDCARD_ALL));
    private static Set<String> roleMessageTrailMessagesExclude = new HashSet<>(Arrays.asList(AKKA_PACKAGE, ACTOR_X_PACKAGE));
    private static boolean castTraceActive = false;


    public static Set<String> getEnhancedPackagesInclude() {
        return enhancedPackagesInclude;
    }

    public static Set<String> getEnhancedPackagesExclude() {
        return enhancedPackagesExclude;
    }

    public static boolean isRoleAkkaSourceMdcActive() {
        return roleAkkaSourceMdcActive;
    }

    public static boolean isRoleCorrelationActive() {
        return roleCorrelationActive;
    }

    public static boolean isRoleCorrelationCreateNewRequest() {
        return roleCorrelationCreateNewRequest;
    }

    public static String getRoleCorrelationCreateNewRequestHeaderName() {
        return roleCorrelationCreateNewRequestHeaderName;
    }

    public static boolean isRoleMessageTrailActive() {
        return roleMessageTrailActive;
    }

    public static int getRoleMessageTrailMaxHistory() {
        return roleMessageTrailMaxHistory;
    }

    public static boolean isRoleMessageTrailTraceLogging() {
        return roleMessageTrailTraceLogging;
    }

    public static Set<String> getRoleMessageTrailPackagesInclude() {
        return roleMessageTrailPackagesInclude;
    }

    public static Set<String> getRoleMessageTrailPackagesExclude() {
        return roleMessageTrailPackagesExclude;
    }

    public static Set<String> getRoleMessageTrailMessagesInclude() {
        return roleMessageTrailMessagesInclude;
    }

    public static Set<String> getRoleMessageTrailMessagesExclude() {
        return roleMessageTrailMessagesExclude;
    }

    public static boolean isCastTraceActive() {
        return castTraceActive;
    }

    /**
     *
     * @param akkaConfig
     */
    static void configure(Config akkaConfig) {

        if (akkaConfig.hasPath(ACTOR_X)) {

            // Actor X Configuration
            Config actorXConfig = akkaConfig.getConfig(ACTOR_X);

            // Enhanced packages include
            if (actorXConfig.hasPath(ENHANCED_PACKAGES_INCLUDE)) {
                List<String> packages = actorXConfig.getStringList(ENHANCED_PACKAGES_INCLUDE);
                enhancedPackagesInclude = new HashSet<>(packages);
            }

            // Enhanced packages exclude
            if (actorXConfig.hasPath(ENHANCED_PACKAGES_EXCLUDE)) {
                List<String> packages = actorXConfig.getStringList(ENHANCED_PACKAGES_EXCLUDE);
                enhancedPackagesExclude.addAll(packages);
            }

            // Configure Roles
            configureRoles(actorXConfig);
        }

        logger.debug("Actor-X configuration processed");
    }

    private static void configureRoles(Config actorXConfig) {

        // Roles
        if (actorXConfig.hasPath(ROLES)) {

            // Roles Configuration
            Config rolesConfig = actorXConfig.getConfig(ROLES);

            // Configure all roles
            configureRoleAkkaSourceMdc(rolesConfig);
            configureRoleCorrelation(rolesConfig);
            configureRoleMessageTrail(rolesConfig);
            configureRoleCastTrace(rolesConfig);
        }
    }

    private static void configureRoleAkkaSourceMdc(Config rolesConfig) {

        // Correlation role
        if (rolesConfig.hasPath(ROLES_AKKA_SOURCE_MDC)) {

            // Correlation Configuration
            Config akkaSourceMdcConfig = rolesConfig.getConfig(ROLES_AKKA_SOURCE_MDC);

            // Active
            if (akkaSourceMdcConfig.hasPath(ROLES_ACTIVE)) {
                roleAkkaSourceMdcActive = akkaSourceMdcConfig.getBoolean(ROLES_ACTIVE);
            }
        }
    }

    private static void configureRoleCorrelation(Config rolesConfig) {

        // Correlation role
        if (rolesConfig.hasPath(ROLES_CORRELATION)) {

            // Correlation Configuration
            Config correlationConfig = rolesConfig.getConfig(ROLES_CORRELATION);

            // Active
            if (correlationConfig.hasPath(ROLES_ACTIVE)) {
                roleCorrelationActive = correlationConfig.getBoolean(ROLES_ACTIVE);
            }

            // Create New Request
            if (correlationConfig.hasPath(ROLES_CORRELATION_CREATE_NEW_REQUEST)) {
                roleCorrelationCreateNewRequest = correlationConfig.getBoolean(ROLES_CORRELATION_CREATE_NEW_REQUEST);

                // New request header name
                if (correlationConfig.hasPath(ROLES_CORRELATION_CREATE_NEW_REQUEST_HEADER_NAME)) {
                    roleCorrelationCreateNewRequestHeaderName = correlationConfig.getString(ROLES_CORRELATION_CREATE_NEW_REQUEST_HEADER_NAME);
                }
            }
        }
    }


    private static void configureRoleMessageTrail(Config rolesConfig) {

        // Message Trail role
        if (rolesConfig.hasPath(ROLES_MESSAGE_TRAIL)) {

            // Message Trail Configuration
            Config messageTrailConfig = rolesConfig.getConfig(ROLES_MESSAGE_TRAIL);

            // Active
            if (messageTrailConfig.hasPath(ROLES_ACTIVE)) {
                roleMessageTrailActive = messageTrailConfig.getBoolean(ROLES_ACTIVE);
            }

            // Max Trail History
            if (messageTrailConfig.hasPath(ROLES_MESSAGE_TRAIL_MAX_HISTORY)) {
                roleMessageTrailMaxHistory = messageTrailConfig.getInt(ROLES_MESSAGE_TRAIL_MAX_HISTORY);
            }

            // Trace Logging
            if (messageTrailConfig.hasPath(ROLES_MESSAGE_TRAIL_TRACE_LOGGING)) {

                Config traceLogging = messageTrailConfig.getConfig(ROLES_MESSAGE_TRAIL_TRACE_LOGGING);

                // Active
                if (traceLogging.hasPath(ROLES_ACTIVE)) {
                    roleMessageTrailTraceLogging = traceLogging.getBoolean(ROLES_ACTIVE);
                }

                // Include Packages
                if (traceLogging.hasPath(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_INCLUDE)) {
                    List<String> packages = traceLogging.getStringList(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_INCLUDE);
                    roleMessageTrailPackagesInclude = new HashSet<>(packages) ;
                }

                // Exclude Packages
                if (traceLogging.hasPath(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_EXCLUDE)) {
                    List<String> packages = traceLogging.getStringList(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_EXCLUDE);
                    roleMessageTrailPackagesExclude = new HashSet<>(Arrays.asList(AKKA_PACKAGE, ACTOR_X_PACKAGE)) ;
                    roleMessageTrailPackagesExclude.addAll(packages) ;
                }

                // Include Messages
                if (traceLogging.hasPath(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_MESSAGE_INCLUDE)) {
                    List<String> packages = traceLogging.getStringList(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_MESSAGE_INCLUDE);
                    roleMessageTrailMessagesInclude = new HashSet<>(packages);
                }

                // Exclude Messages
                if (traceLogging.hasPath(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_MESSAGE_EXCLUDE)) {
                    List<String> packages = traceLogging.getStringList(ROLES_MESSAGE_TRAIL_TRACE_LOGGING_MESSAGE_EXCLUDE);
                    roleMessageTrailMessagesExclude.addAll(packages) ;
                }
            }
        }
    }

    private static void configureRoleCastTrace(Config rolesConfig) {

        // Correlation role
        if (rolesConfig.hasPath(ROLES_CAST_TRACE)) {

            // Correlation Configuration
            Config castTraceConfig = rolesConfig.getConfig(ROLES_CAST_TRACE);

            // Active
            if (castTraceConfig.hasPath(ROLES_ACTIVE)) {
                castTraceActive = castTraceConfig.getBoolean(ROLES_ACTIVE);
            }
        }
    }


    // TODO CHECK LOGIC
    // TODO ADD CACHING?
    public static boolean included(String packageName, Set<String> includePackages, Set<String> excludePackages) {

        String includeBestMatch = includedByPackage(packageName, includePackages);
        String excludeBestMatch = includedByPackage(packageName, excludePackages);

        boolean included = includeBestMatch != null;
        if (included && excludeBestMatch != null) {
            if (excludeBestMatch.length() > includeBestMatch.length()) {
                included = false;
            }
        }

        return included;
    }

    public static String includedByPackage(String packageName, Set<String> packages) {

        // False check
        if (packageName == null || packages == null || packages.isEmpty()) {
            return null;
        }

        String bestMatch = null;
        for (String pkg : packages) {
            if (packageName.startsWith(pkg)) {
                if (bestMatch == null || pkg.length() > bestMatch.length()) {
                    bestMatch = pkg;
                }
            }
        }

        if (bestMatch == null && packages.contains(WILDCARD_ALL)) {
            bestMatch = WILDCARD_ALL;
        }

        return bestMatch;
    }

}
