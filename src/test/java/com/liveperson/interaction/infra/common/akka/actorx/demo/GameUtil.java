package com.liveperson.interaction.infra.common.akka.actorx.demo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Amit Tal
 * @since 10/12/2014
 */
public class GameUtil {

    private static Logger logger = LoggerFactory.getLogger(GameUtil.class);

    public static boolean cheatEnabled() {
        logger.info("Cheat is not an option!");
        return false;
    }
}
