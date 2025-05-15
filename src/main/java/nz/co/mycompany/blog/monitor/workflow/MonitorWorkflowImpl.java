package nz.co.mycompany.blog.monitor.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import nz.co.mycompany.blog.monitor.activity.ClassifyTextActivity;
import nz.co.mycompany.blog.monitor.model.CompletionResult;
import nz.co.mycompany.blog.monitor.model.Topic;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;
import nz.co.mycompany.blog.monitor.activity.GetBlogActivity;
import nz.co.mycompany.blog.monitor.activity.SummariseTextActivity;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@WorkflowImpl(taskQueues = Scheduler.TASK_QUEUE)
public class MonitorWorkflowImpl implements MonitorWorkflow {

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
    public CompletionResult monitor() throws IOException {
        log.info("inside monitor");
        CompletionResult lastCompletionResult = Workflow.getLastCompletionResult(CompletionResult.class);
        if (lastCompletionResult == null) {
            lastCompletionResult = new CompletionResult();
        }
        Integer count = lastCompletionResult.getCount();
        log.info("count = {}", count);
        String text = getBlogActivity.getText();
        String sanitisedText = text.replace("\n", " ");
        String summary = summariseTextActivity.getSummary(sanitisedText);
        Topic topic = classifyTextActivity.getTopic(sanitisedText);
        CompletionResult completionResult = new CompletionResult();
        int newCountValue = count + 1;
        log.info("newCountValue = {}", newCountValue);
        completionResult.setCount(newCountValue);
        log.info("monitor successful");
        return completionResult;
    }
}
