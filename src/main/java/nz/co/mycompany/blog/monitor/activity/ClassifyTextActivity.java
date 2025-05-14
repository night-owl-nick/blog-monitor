package nz.co.mycompany.blog.monitor.activity;

import io.temporal.activity.ActivityInterface;
import nz.co.mycompany.blog.monitor.model.Topic;

@ActivityInterface
public interface ClassifyTextActivity {

    Topic getTopic(String text);
}
