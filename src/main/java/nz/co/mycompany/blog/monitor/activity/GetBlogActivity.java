package nz.co.mycompany.blog.monitor.activity;

import io.temporal.activity.ActivityInterface;
import nz.co.mycompany.blog.monitor.model.Article;

import java.io.IOException;
import java.util.List;

@ActivityInterface
public interface GetBlogActivity {
    List<Article> getArticles() throws IOException;
}
