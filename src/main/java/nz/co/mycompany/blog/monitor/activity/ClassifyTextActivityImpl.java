package nz.co.mycompany.blog.monitor.activity;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import nz.co.mycompany.blog.monitor.model.Topic;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Slf4j
@Component
@ActivityImpl(taskQueues = Scheduler.TASK_QUEUE)
public class ClassifyTextActivityImpl implements ClassifyTextActivity {

    @Override
    public Topic getTopic(String text) {
        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.AP_SOUTHEAST_2)
                .build();

        String modelId = "amazon.titan-text-express-v1";

        String nativeRequestTemplate = "{ \"inputText\": \"{{prompt}}\" }";
        String prompt = "The following is text from a blog post \\\"" + text + "\\\" What is the blog post about? Classify it as one of the following and respond only with the classification labels: security-related, API-related, community-related, dev-tooling-related, or other";
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
            if (classification != null) classification = classification.trim();
            log.info("classification = {}", classification);
            if (classification != null) {
                if (classification.contains("security-related")) {
                    return Topic.SECURITY;
                } else if (classification.equals("API-related")) {
                    return Topic.API;
                } else if (classification.equals("community-related")) {
                    return Topic.COMMUNITY;
                } else if (classification.equals("dev-tooling-related")) {
                    return Topic.DEVELOPER_TOOLING;
                }
            }
            return Topic.OTHER;
        } catch (SdkClientException e) {
            log.error("ERROR: Can't invoke '{}'. Reason: {}", modelId, e.getMessage(), e);
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", modelId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
