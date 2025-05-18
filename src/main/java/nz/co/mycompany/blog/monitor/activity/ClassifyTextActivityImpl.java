package nz.co.mycompany.blog.monitor.activity;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import nz.co.mycompany.blog.monitor.model.Topic;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ActivityImpl(taskQueues = Scheduler.TASK_QUEUE)
public class ClassifyTextActivityImpl implements ClassifyTextActivity {

    @Override
    public List<Topic> getTopics(String text) {
        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.AP_SOUTHEAST_2)
                .build();

        String modelId = "amazon.titan-text-express-v1";

        String nativeRequestTemplate = "{ \"inputText\": \"{{prompt}}\" }";
        String prompt = "The following is text from a blog post \\\"" + text + "\\\" What is the blog post about? Classify it as one or more of the following and respond only with the classification labels: security-related, API-related, community-related, dev-tooling-related, or other";
        log.info("prompt = {}", prompt);
        String nativeRequest = nativeRequestTemplate.replace("{{prompt}}", prompt);
        log.info("nativeRequest = {}", nativeRequest);

        try {
            InvokeModelResponse response = client.invokeModel(request -> request
                    .body(SdkBytes.fromUtf8String(nativeRequest))
                    .modelId(modelId)
            );
            JSONObject responseBody = new JSONObject(response.body().asUtf8String());
            String classification = new JSONPointer("/results/0/outputText").queryFrom(responseBody).toString();
            if (classification != null) classification = classification.trim().toLowerCase();
            log.info("classification = {}", classification);
            List<Topic> topics = new ArrayList<>();
            if (classification != null) {
                if (classification.contains("security-related")) {
                    topics.add(Topic.SECURITY);
                }
                if (classification.contains("api-related")) {
                    topics.add(Topic.API);
                }
                if (classification.contains("community-related")) {
                    topics.add(Topic.COMMUNITY);
                }
                if (classification.contains("dev-tooling-related")) {
                    topics.add(Topic.DEVELOPER_TOOLING);
                }
            }
            return topics;
        } catch (SdkClientException e) {
            log.error("ERROR: Can't invoke '{}'. Reason: {}", modelId, e.getMessage(), e);
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", modelId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
