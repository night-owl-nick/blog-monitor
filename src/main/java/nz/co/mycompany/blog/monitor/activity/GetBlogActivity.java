package nz.co.mycompany.blog.monitor.activity;

import io.temporal.activity.ActivityInterface;

import java.io.IOException;

@ActivityInterface
public interface GetBlogActivity {
    String getText() throws IOException;
}
