package com.locusintellect

import org.apache.log4j.Logger

import java.util.concurrent.Callable

public class DelayedRetryExecutionWrapper<R> {

    private static final Logger LOGGER = Logger.getLogger(DelayedRetryExecutionWrapper.class)

    protected static final int RETRY_COUNT = 3

    public R execute(Callable<R> callable) throws Exception {
        int count = RETRY_COUNT
        boolean executeFlag = false
        while (count > 0 && !executeFlag) {
            try {
                R retVal = callable.call()
                executeFlag = true
                return retVal
            } catch (SocketTimeoutException ex) {
                count--
                if (count == 0) {
                    LOGGER.error("Failed to execute operation.", ex)
                    throw ex
                }
                LOGGER.info("Failed to execute operation. Retries remaining ${count}")
                // Delay on timeouts to avoid load on servers
                // todo duration need to be configurable.
                Thread.sleep(1000)
            }
        }
        return null
    }
}
