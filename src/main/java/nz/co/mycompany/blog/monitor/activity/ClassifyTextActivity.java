package nz.co.mycompany.blog.monitor.activity;

import io.temporal.activity.ActivityInterface;
import nz.co.mycompany.blog.monitor.model.Topic;

import java.util.List;

@ActivityInterface
public interface ClassifyTextActivity {

    List<Topic> getTopics(String text);
}
