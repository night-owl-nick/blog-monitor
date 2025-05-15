package nz.co.mycompany.blog.monitor.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import nz.co.mycompany.blog.monitor.model.CompletionResult;

import java.io.IOException;

@WorkflowInterface
public interface MonitorWorkflow {

    @WorkflowMethod
    CompletionResult monitor() throws IOException;
}
