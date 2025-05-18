package nz.co.mycompany.blog.monitor.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SendAlertsChannelActivity {
    @ActivityMethod(name = "SendAlertsChannel")
    String send(String text);
}
