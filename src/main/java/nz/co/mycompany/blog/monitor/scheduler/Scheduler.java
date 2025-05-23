package nz.co.mycompany.blog.monitor.scheduler;

import io.temporal.api.enums.v1.ScheduleOverlapPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.mycompany.blog.monitor.workflow.MonitorWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
public class Scheduler {

    public static final String TASK_QUEUE = "MonitorTaskQueue";

    static final String WORKFLOW_ID = "MonitorWorkflow";

    static final String SCHEDULE_ID = "MonitorSchedule";

    @Autowired
    WorkflowClient client;

    @Autowired
    private ScheduleClient scheduleClient;

    @EventListener(ApplicationReadyEvent.class)
    public void run() throws InterruptedException {
        log.info("inside run");
        ScheduleHandle handle = null;
        boolean isScheduleExist = scheduleClient.listSchedules().anyMatch(scheduleListDescription -> {
            return SCHEDULE_ID.equals(scheduleListDescription.getScheduleId());
        });

        if (isScheduleExist) {
            handle = scheduleClient.getHandle(SCHEDULE_ID);
        } else {

            WorkflowOptions workflowOptions =
                    WorkflowOptions.newBuilder()
                            .setWorkflowId(WORKFLOW_ID)
                            .setTaskQueue(TASK_QUEUE)
                            .setWorkflowRunTimeout(Duration.ofSeconds(30))
                            .build();

            ScheduleActionStartWorkflow action =
                    ScheduleActionStartWorkflow.newBuilder()
                            .setWorkflowType(MonitorWorkflow.class)
                            .setOptions(workflowOptions)
                            .build();

            Schedule schedule =
                    Schedule.newBuilder().setAction(action).setSpec(ScheduleSpec.newBuilder().build()).build();

            handle =
                    scheduleClient.createSchedule(SCHEDULE_ID, schedule, ScheduleOptions.newBuilder().build());

//        handle.trigger(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_ALLOW_ALL);
            handle.trigger(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP);

            handle.update(
                    (ScheduleUpdateInput input) -> {
                        Schedule.Builder builder = Schedule.newBuilder(input.getDescription().getSchedule());

                        builder.setSpec(
                                ScheduleSpec.newBuilder()
                                        // Run the schedule at 5pm on Friday
//                                    .setCalendars(
//                                            Collections.singletonList(
//                                                    ScheduleCalendarSpec.newBuilder()
//                                                            .setHour(Collections.singletonList(new ScheduleRange(17)))
//                                                            .setDayOfWeek(Collections.singletonList(new ScheduleRange(5)))
//                                                            .build()))
                                        // Run the schedule every 60s
                                        .setIntervals(
                                                Collections.singletonList(new ScheduleIntervalSpec(Duration.ofSeconds(60))))
                                        .build());
                        // Make the schedule paused to demonstrate how to unpause a schedule
                        builder.setState(
                                ScheduleState.newBuilder()
                                        .setPaused(true)
                                        .setLimitedAction(true)
                                        .setRemainingActions(3) // MonitorWorkflow is executed 4 times
                                        .build());
                        return new ScheduleUpdate(builder.build());
                    });

            handle.unpause();
        }

        while (true) {
            ScheduleState state = handle.describe().getSchedule().getState();
            if (state.getRemainingActions() == 0) {
                break;
            }
            Thread.sleep(60000);
        }
        // Delete the schedule once the sample is done
        handle.delete();
        System.exit(0);
    }

}
