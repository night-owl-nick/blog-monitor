package nz.co.mycompany.blog.monitor.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import nz.co.mycompany.blog.monitor.Scheduler;
import nz.co.mycompany.blog.monitor.activity.GetBlogActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@WorkflowImpl(taskQueues = Scheduler.TASK_QUEUE)
public class MonitorWorkflowImpl implements MonitorWorkflow {

    private static final Logger log = LoggerFactory.getLogger(MonitorWorkflowImpl.class);

    private GetBlogActivity getBlogActivity =
            Workflow.newActivityStub(
                GetBlogActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String monitor() {
        log.info("inside monitor");
        return getBlogActivity.getText();
    }
}
