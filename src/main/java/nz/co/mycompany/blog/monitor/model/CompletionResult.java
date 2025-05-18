package nz.co.mycompany.blog.monitor.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A scheduled Workflow Execution may complete with a result up to the maximum blob size (2 MiB by default).
 * However, due to internal limitations, results that are within 1 KiB of this limit cannot be passed to the next execution.
 * So, for example, a Workflow Execution that returns a result of size 2,096,640 bytes (which is above 2MiB - 1KiB limit)
 * will be allowed to compete successfully, but that value will not be available as a last completion result.
 * This limitation may be lifted in the future.
 */
@Data
public class CompletionResult {
    private int count = 0;
    private Map<String, ArticleMemento> articleMementos = new HashMap<>();
    private List<Promotion> promotions = new ArrayList<>();
}
