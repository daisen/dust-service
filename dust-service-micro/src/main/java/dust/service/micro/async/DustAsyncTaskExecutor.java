package dust.service.micro.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 异步任务执行器，用于执行异步操作，通常根据
 * 所有任务的执行异常都会记录日志
 * @author huangshengtao
 */
public class DustAsyncTaskExecutor implements AsyncTaskExecutor,
        InitializingBean, DisposableBean {

    private final Logger log = LoggerFactory.getLogger(DustAsyncTaskExecutor.class);

    //推荐初始化成Pool
    private final AsyncTaskExecutor executor;

    public DustAsyncTaskExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(createWrappedRunnable(task));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        executor.execute(createWrappedRunnable(task), startTimeout);
    }

    public void execute(TaskInfo taskInfo) {
        executor.execute(createWrappedRunnable(taskInfo), taskInfo.getStartTimeout());
    }

    private Runnable createWrappedRunnable(final TaskInfo task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                handle(e);
            }
        };
    }

    /**
     * 带返回值的Task处理
     * @param task
     * @param <T>
     * @return
     */
    private <T> Callable<T> createCallable(final Callable<T> task) {
        return () -> {
            try {
                return task.call();
            } catch (Exception e) {
                handle(e);
                throw e;
            }
        };
    }

    /**
     * 不带返回值的Task操作
     * @param task
     * @return
     */
    private Runnable createWrappedRunnable(final Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                handle(e);
            }
        };
    }

    protected void handle(Exception e) {
        log.error("Caught async exception", e);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(createWrappedRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(createCallable(task));
    }

    @Override
    public void destroy() throws Exception {
        if (executor instanceof DisposableBean) {
            DisposableBean bean = (DisposableBean) executor;
            bean.destroy();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (executor instanceof InitializingBean) {
            InitializingBean bean = (InitializingBean) executor;
            bean.afterPropertiesSet();
        }
    }

    public static abstract class TaskInfo<T> implements Runnable {

        public void run() {

        }

        public T getResult() {
            return null;
        }

        public long getStartTimeout() {
            return 0;
        }
    }
}