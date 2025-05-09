package nz.co.mycompany.blog.monitor.activity;

import io.temporal.spring.boot.ActivityImpl;
import nz.co.mycompany.blog.monitor.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = Scheduler.TASK_QUEUE)
public class GetBlogActivityImpl implements GetBlogActivity {

    private static final Logger log = LoggerFactory.getLogger(GetBlogActivityImpl.class);

    @Override
    public String getText() {
        log.info("inside getText");
        return "hello";
    }
}
