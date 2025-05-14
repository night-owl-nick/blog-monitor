package nz.co.mycompany.blog.monitor.activity;

import io.temporal.spring.boot.ActivityImpl;
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

@Component
@ActivityImpl(taskQueues = Scheduler.TASK_QUEUE)
public class SummariseTextActivityImpl implements SummariseTextActivity {

    private static final Logger log = LoggerFactory.getLogger(SummariseTextActivityImpl.class);

    @Override
    public String getSummary(String text) {

        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.AP_SOUTHEAST_2)
                .build();

        String modelId = "amazon.titan-text-express-v1";

        String nativeRequestTemplate = "{ \"inputText\": \"{{prompt}}\" }";
        String prompt = "Summarise the following text: " + text;
        log.info("prompt = {}", prompt);
        String nativeRequest = nativeRequestTemplate.replace("{{prompt}}", prompt);
        log.info("nativeRequest = {}", nativeRequest);

        try {
            InvokeModelResponse response = client.invokeModel(request -> request
                    .body(SdkBytes.fromUtf8String(nativeRequest))
                    .modelId(modelId)
            );
            JSONObject responseBody = new JSONObject(response.body().asUtf8String());
            String summary = new JSONPointer("/results/0/outputText").queryFrom(responseBody).toString();
            log.info("summary = {}", summary);
            return summary;

        } catch (SdkClientException e) {
            log.error("ERROR: Can't invoke '{}'. Reason: {}", modelId, e.getMessage(), e);
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", modelId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
