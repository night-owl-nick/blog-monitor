package nz.co.mycompany.blog.monitor.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SendPortalChannelActivity {
    @ActivityMethod(name = "SendPortalChannel")
    String send(String text);
}
