package nz.co.mycompany.blog.monitor.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import nz.co.mycompany.blog.monitor.activity.ClassifyTextActivity;
import nz.co.mycompany.blog.monitor.model.Topic;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;
import nz.co.mycompany.blog.monitor.activity.GetBlogActivity;
import nz.co.mycompany.blog.monitor.activity.SummariseTextActivity;
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

    private SummariseTextActivity summariseTextActivity =
            Workflow.newActivityStub(
                    SummariseTextActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(30)).build());

    private ClassifyTextActivity classifyTextActivity =
            Workflow.newActivityStub(
                    ClassifyTextActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(30)).build());

    @Override
    public void monitor() {
        log.info("inside monitor");
        String text = getBlogActivity.getText();
        String sanitisedText = text.replace("\n", " ");
        String summary = summariseTextActivity.getSummary(sanitisedText);
        Topic topic = classifyTextActivity.getTopic(sanitisedText);
        log.info("monitor successful");
    }
}
