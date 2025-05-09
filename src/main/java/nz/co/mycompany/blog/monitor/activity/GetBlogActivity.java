package nz.co.mycompany.blog.monitor.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface GetBlogActivity {
    String getText();
}
