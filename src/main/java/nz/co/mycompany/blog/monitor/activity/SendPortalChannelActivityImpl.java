package nz.co.mycompany.blog.monitor.activity;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = Scheduler.TASK_QUEUE)
public class SendPortalChannelActivityImpl implements SendPortalChannelActivity {
    @Override
    public String send(String text) {
        log.info("Promotion sent to Internal Portal Channel: {}", text);
        return text;
    }
}
