package com.liveperson.infra.akka.actorx.staff;

/**
 * @author Amit Tal
 * @since 10/26/2014
 */
public class AssistantUtils {

    public static boolean isInternalStaffMessage(Object message) {
        return message instanceof StaffMessage;
    }
}
