package nz.co.mycompany.blog.monitor.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MonitorWorkflow {

    @WorkflowMethod
    String monitor();
}
