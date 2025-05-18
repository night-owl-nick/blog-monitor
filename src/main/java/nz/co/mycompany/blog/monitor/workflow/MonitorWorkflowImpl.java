package nz.co.mycompany.blog.monitor.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import nz.co.mycompany.blog.monitor.activity.*;
import nz.co.mycompany.blog.monitor.model.*;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nicholas Kum
 */
@Slf4j
@WorkflowImpl(taskQueues = Scheduler.TASK_QUEUE)
public class MonitorWorkflowImpl implements MonitorWorkflow {

    private final GetBlogActivity getBlogActivity =
            Workflow.newActivityStub(
                GetBlogActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    private final SummariseTextActivity summariseTextActivity =
            Workflow.newActivityStub(
                    SummariseTextActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(30)).build());

    private final ClassifyTextActivity classifyTextActivity =
            Workflow.newActivityStub(
                    ClassifyTextActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(30)).build());

    private final SendAlertsChannelActivity sendAlertsChannelActivity =
            Workflow.newActivityStub(
                    SendAlertsChannelActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    private final SendGitHubChannelActivity sendGitHubChannelActivity =
            Workflow.newActivityStub(
                    SendGitHubChannelActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());

    private final SendPortalChannelActivity sendPortalChannelActivity =
            Workflow.newActivityStub(
                    SendPortalChannelActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public CompletionResult monitor() throws IOException {
        log.info("inside monitor");
        CompletionResult lastCompletionResult = Workflow.getLastCompletionResult(CompletionResult.class);
        if (lastCompletionResult == null) {
            lastCompletionResult = new CompletionResult();
        }
        int count = lastCompletionResult.getCount();
        log.info("count = {}", count);
        Map<String, ArticleMemento> mementos = lastCompletionResult.getArticleMementos();
        List<Article> articles = getBlogActivity.getArticles();
        List<Article> differentArticles = articles.stream()
                .filter(article -> !article.getHash().equals(mementos.get(article.getId()) != null ? mementos.get(article.getId()).getHash() : null))
                .toList();
        List<Promotion> promotions = findPromotions(differentArticles);
        for (Promotion promotion: promotions) {
            log.info("promotion = {}", promotion);
            if (promotion.getTopics().contains(Topic.SECURITY)) {
                sendAlertsChannelActivity.send(promotion.getSummary());
            }
            if (promotion.getTopics().contains(Topic.API)) {
                sendGitHubChannelActivity.send(promotion.getSummary());
            }
            if (promotion.getTopics().contains(Topic.COMMUNITY) || promotion.getTopics().contains(Topic.DEVELOPER_TOOLING)) {
                sendPortalChannelActivity.send(promotion.getSummary());
            }
        }

        CompletionResult completionResult = new CompletionResult();
        int newCountValue = count + 1;
        log.info("newCountValue = {}", newCountValue);
        completionResult.setCount(newCountValue);
        completionResult.setArticleMementos(
                articles.stream()
                        .map(article -> new ArticleMemento(article.getId(), article.getPublishedDateTime(), article.getHash()))
                        .collect(Collectors.toMap(ArticleMemento::getId, Function.identity())));
        completionResult.setPromotions(promotions);
        log.info("monitor successful");
        return completionResult;
    }

    private List<Promotion> findPromotions(List<Article> differentArticles) {
        List<Promotion> promotions = new ArrayList<>();
        for (Article article: differentArticles) {
            log.info("article = {}", article);
            String content = article.getContent();
            String sanitisedText = content.replace("\n", " ");
            String summary = summariseTextActivity.getSummary(sanitisedText);
            List<Topic> topics = classifyTextActivity.getTopics(sanitisedText);
            if (!topics.isEmpty()) {
                Promotion promotion = new Promotion(article.getId(), summary, topics);
                promotions.add(promotion);
            } else {
                log.info("No topics found for article Id = {}", article.getId());
            }
        }
        return promotions;
    }

}
