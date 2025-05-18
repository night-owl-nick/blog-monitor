package nz.co.mycompany.blog.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Promotion {
    private String articleId;
    private String summary;
    private List<Topic> topics;
}
