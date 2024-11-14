package priv.ethan.vector.db.demo;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class RetryHelper {

    public static void retryInMemory(int retryNumber, long intervalSeconds, Runnable runnable) {
        retryInMemory(retryNumber, intervalSeconds, runnableToSupplier(runnable));
    }

    public static <T> T retryInMemory(int retryNumber, long intervalSeconds, Supplier<T> function) {
        Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
            .retryIfException()
            .withStopStrategy(StopStrategies.stopAfterAttempt(retryNumber))
            .withWaitStrategy(WaitStrategies.fixedWait(intervalSeconds, TimeUnit.SECONDS))
            .build();
        try {
            return retryer.call(function::get);
        } catch (ExecutionException | RetryException e) {
            throw new RuntimeException("retry in memory failed", e);
        }
    }

    public static <T> Supplier<T> runnableToSupplier(Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }

}
