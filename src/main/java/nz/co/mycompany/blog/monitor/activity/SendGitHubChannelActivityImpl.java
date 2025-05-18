package nz.co.mycompany.blog.monitor.activity;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import java.util.function.Consumer;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@ActivityImpl(taskQueues = Scheduler.TASK_QUEUE)
public class SendGitHubChannelActivityImpl implements SendGitHubChannelActivity {

    private static final String TOKEN = "<YOUR_GithubPersonalAccessToken_HERE>";

    @Override
    public String send(String text) {
        WebClient client = WebClient.builder()
                .baseUrl("https://api.github.com/graphql")
                .defaultHeaders(httpHeaders(TOKEN))
                .build();
        Mono<String> mono = client
                .post()
                .bodyValue(requestBody(text))
                .retrieve()
                .bodyToMono(String.class);
        String responseBody = mono.block();
        log.info("responseBody = {}", responseBody);
        log.info("Promotion sent to GitHub Discussions Channel: {}", text);
        return text;
    }

    private Consumer<HttpHeaders> httpHeaders(String token) {
        return headers -> {
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        };
    }

    private String requestBody(String text) {
        String sanitizedText = text.replace("\n", "\\n");
        return "{\n" +
                "  \"query\": \"mutation {\\n  createDiscussion(input: {\\n    repositoryId: \\\"YOUR_REPOSITORY_ID\\\"\\n    body: \\\"" + sanitizedText + "\\\",\\n    categoryId: \\\"YOUR_CATEGORY_ID\\\"\\n    title: \\\"My announcement\\\"\\n  }) {\\n    discussion {\\n      id\\n    }\\n  }\\n}\",\n" +
                "  \"variables\": \"{}\"\n" +
                "}";
    }

}
