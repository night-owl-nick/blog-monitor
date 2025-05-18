package nz.co.mycompany.blog.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleMemento {
    String id;
    String publishedDateTime;
    String hash;
}
