package nz.co.mycompany.blog.monitor.activity;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import nz.co.mycompany.blog.monitor.scheduler.Scheduler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ActivityImpl(taskQueues = Scheduler.TASK_QUEUE)
public class GetBlogActivityImpl implements GetBlogActivity {

    private static final String mockText = """
            Android fixes 47 vulnerabilities, including one zero-day. Update as soon as you can! \
            Posted: May 6, 2025 by Pieter Arntz \
            Google has patched 47 vulnerabilities in Android, including one actively exploited zero-day vulnerability in its May 2025 Android Security Bulletin. \
            Zero-days are vulnerabilities that are exploited before vendors have a chance to patch them—often before they even know about them. \
            The May updates are available for Android 13, 14, and 15. Android vendors are notified of all issues at least a month before publication, however, this doesn’t always mean that the patches are available for all devices immediately. \
            You can find your device’s Android version number, security update level, and Google Play system level in your Settings app. You’ll get notifications when updates are available for you, but you can also check for them yourself. \
            For most phones it works like this: Under About phone or About device you can tap on Software updates to check if there are new updates available for your device, although there may be slight differences based on the brand, type, and Android version of your device. \
            If your Android phone shows patch level 2025-05-05 or later then you can consider the issues as fixed. The difference with patch level 2025-05-01 is that the higher level provides all the fixes from the first batch and security patches for closed-source third-party and kernel subcomponents, which may not necessarily apply to all Android devices. \
            Keeping your device as up to date as possible protects you from known vulnerabilities and helps you to stay safe.\
            The zero-day \
            The actively exploited zero-day patched with this update was flagged by Facebook in March and found in the FreeType library. FreeType is an open-source software library that Android devices use to display text by rendering fonts. In essence, it turns font files into the letters and characters that you see on your screen. It is designed to be small, fast, and flexible, supporting many font formats and used widely across billions of devices and applications. \
            The vulnerability, tracked as CVE-2025-27363, allows attackers to execute remote code (RCE) by exploiting how FreeType processes certain TrueType GX and variable font files. Because FreeType mishandles values in the device’s memory, it creates an out-of-bounds write vulnerability. When a program accesses memory outside its allocated area—either by reading or writing beyond the bounds—it can cause crashes, run arbitrary code, or expose sensitive information. \
            This happens when the data size exceeds the allocated memory, when data writes target incorrect memory locations, or when the program miscalculates data size or position. \
            FreeType versions newer than 2.13.0 fix this vulnerability. Since FreeType operates as a native library embedded within system components that render fonts, typical Android users cannot easily check which version their device uses. Therefore, the best defense is to install the latest system updates and run active anti-malware protection. \
            Facebook warned that attackers “may have exploited the vulnerability in the wild,” and Google confirmed the vulnerability “may be under limited, targeted exploitation,” though neither disclosed further details. \
            It’s reasonable to assume that simply opening a document or app containing a malicious font could compromise your device—without requiring any additional user action or permissions.
            There is a community dance on this Saturday. \
            The dance is a celebration of the Android 13 release, which is expected to be released on May 6, 2025.
            """;

    @Override
    public String getText() throws IOException {
        log.info("inside getText");

        // Integrate with writefreely running locally on port 8282
//        WebClient client = WebClient.create("http://localhost:8282/");
//        Mono<String> stringMono = client.get().retrieve().bodyToMono(String.class);
//        String body = stringMono.block();
//        log.info("body = {}", body);
        Map<String, Object> blogs = new HashMap<>();
        String blogUrl = "http://localhost:8282/";
        Document doc = Jsoup.connect(blogUrl).get();
        Elements articles = doc.select("article");
        for (Element article: articles) {
            String id = article.attr("id");
            log.info("id = {}", id);
            String publishedDateTime = article.select("h2 time").attr("datetime");
            log.info("publishedDateTime = {}", publishedDateTime);
            String content = article.select("div.e-content").text();
            log.info("content = {}", content);
            String hash = DigestUtils.sha256Hex(content);
            log.info("hash = {}", hash);
            Map<String, String> blog = new HashMap<>();
            blogs.put(id, blogUrl);
        }
        return mockText;
    }
}
