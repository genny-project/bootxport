package life.genny.bootxport.utils;

import org.jboss.logging.Logger;

public class CommonUtils {

    private static final Logger log = Logger.getLogger(CommonUtils.class);
    
    /**
     * A method to retrieve a system environment variable, and optionally log it if it is missing (default, do log)
     * @param env Env to retrieve
     * @param alert whether or not to throw an excpetion or just log if it is missing or not (default: true)
     * @return the value of the environment variable, or null if it cannot be found
     */
    public static String getSystemEnv(String env, String fallback) {
        String result = System.getenv(env);
        
        if(result == null && fallback == null) {
            throw new RuntimeException("Missing Environment Variable: " + env);
        } else if (result == null) {
            log.warn("Could not find System Environment Variable: " + env);
            log.warn("Defaulting " + env + " to " + fallback);
            return fallback;
        }

        return result;
    }

    /**
     * A method to retrieve a system environment variable, and optionally log it if it is missing (default, do log)
     * @param env Env to retrieve
     * @return the value of the environment variable, or null if it cannot be found
     */
    public static String getSystemEnv(String env) {
        return getSystemEnv(env, null);
    }
}
