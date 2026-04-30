package com.bear.hospital.controller;

import com.bear.hospital.config.IgnoreAuth;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIChatController {

    private final String DIFY_API_URL = "https://api.dify.ai/v1/chat-messages";
    private final String API_KEY = "Bearer app-bZf82EFL8TD54XH41t5tW349";
    @IgnoreAuth
    @PostMapping("/ask")
    public ResponseEntity<?> askDify(@RequestBody Map<String, Object> params) {
        // 1. 显式设置超时（Infra思维：防止AI响应慢导致连接被强行切断）
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时 5s
        factory.setReadTimeout(60000);    // 读取超时 60s (给AI足够时间思考)

        RestTemplate restTemplate = new RestTemplate(factory);

        // 2. 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", API_KEY);

        // 【新增这一行】伪装成 Chrome 浏览器，解决 Cloudflare 1010 报错
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        // 3. 构造请求体 (确保参数格式完全符合 Dify 官方 Blocking 模式)
        Map<String, Object> body = new HashMap<>();
        body.put("inputs", new HashMap<>());
        body.put("query", params.get("query"));
        body.put("response_mode", "blocking");
        body.put("user", "student_user");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // 4. 发送请求并获取响应
            // 使用 Map.class 接收，这样 Spring 会自动帮你解析 Dify 返回的 JSON
            System.out.println("发送请求到 Dify，Query: " + params.get("query"));
            ResponseEntity<Map> response = restTemplate.postForEntity(DIFY_API_URL, entity, Map.class);

            // 关键调试：打印 Dify 完整的返回数据
            System.out.println("Dify 原始返回: " + response.getBody());

            // 直接将 Dify 返回的整个内容给到前端，你可以看到 answer 等字段
            return ResponseEntity.ok(response.getBody());

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 捕捉 4xx/5xx 错误（如 API Key 错误、模型配置错误）
            String errorBody = e.getResponseBodyAsString();
            System.err.println("Dify 接口报错详情: " + errorBody);
            // 如果 Dify 报错（比如参数不对），这里能抓到最真实的报错 JSON
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        } catch (Exception e) {
            // 如果是网络超时等问题
            System.err.println("系统异常: " + e.getMessage());
            return ResponseEntity.status(500).body("服务器内部错误: " + e.getMessage());
        }
    }
}