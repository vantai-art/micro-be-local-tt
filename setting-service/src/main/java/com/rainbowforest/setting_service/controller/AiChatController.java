package com.rainbowforest.setting_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/settings/ai")
@CrossOrigin
public class AiChatController {

    @Value("${groq.api.key:}")
    private String groqApiKeys;

    private final RestTemplate restTemplate = new RestTemplate();
    private int currentKeyIndex = 0;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String userMessage = (String) request.get("message");
        List<Map<String, String>> history = null;
        String dataContext = "";
        try {
            history = (List<Map<String, String>>) request.get("history");
        } catch (Exception ignored) {
        }
        try {
            dataContext = (String) request.getOrDefault("dataContext", "");
        } catch (Exception ignored) {
        }

        Map<String, Object> response = new HashMap<>();

        if (userMessage == null || userMessage.trim().isEmpty()) {
            response.put("reply", "Vui lòng nhập tin nhắn");
            return ResponseEntity.ok(response);
        }

        if (groqApiKeys == null || groqApiKeys.trim().isEmpty()) {
            response.put("reply", getSmartMockReply(userMessage));
            response.put("warning", "Groq API Key chưa cấu hình.");
            return ResponseEntity.ok(response);
        }

        String[] keys = groqApiKeys.split(",");
        for (int i = 0; i < keys.length; i++)
            keys[i] = keys[i].trim();

        try {
            String systemInstruction = "Bạn là Trợ Lý AI tích hợp của hệ thống Mini ERP. Hệ thống gồm: " +
                    "1. HRM (Nhân viên, chấm công, lương, nghỉ phép). " +
                    "2. Inventory (Hàng hóa, nhà cung cấp, kho, tồn kho). " +
                    "3. Accounting (Tài khoản, hóa đơn, chi phí, sổ cái). " +
                    "4. Analytics & AI (Doanh thu, dự báo, KPI). " +
                    "Trả lời ngắn gọn, thân thiện, chuyên nghiệp bằng ngôn ngữ người dùng hỏi. " +
                    "QUAN TRỌNG: Dùng số liệu thật bên dưới để trả lời cụ thể, KHÔNG nói chung chung. " +
                    "Nếu người dùng trả lời ngắn như 'có', 'ok'... dựa vào lịch sử hội thoại để hiểu.";

            if (dataContext != null && !dataContext.trim().isEmpty()) {
                systemInstruction += "\n\n" + dataContext;
            }

            // Xây dựng messages (OpenAI format - Groq tương thích)
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemInstruction));

            if (history != null) {
                for (Map<String, String> msg : history) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    if (role == null || content == null)
                        continue;
                    messages.add(Map.of("role", "assistant".equals(role) ? "assistant" : "user", "content", content));
                }
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            // Request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.3-70b-versatile");
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.7);

            // Gọi Groq API
            String baseUrl = "https://api.groq.com/openai/v1/chat/completions";
            ResponseEntity<Map> groqResponse = null;

            for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
                int actualIdx = (currentKeyIndex + keyIdx) % keys.length;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + keys[actualIdx]);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                for (int attempt = 0; attempt < 2; attempt++) {
                    try {
                        groqResponse = restTemplate.postForEntity(baseUrl, entity, Map.class);
                        currentKeyIndex = actualIdx;
                        break;
                    } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e429) {
                        System.out.println("Groq Key #" + (actualIdx + 1) + " rate limited, "
                                + (attempt == 0 ? "waiting 5s..." : "trying next key..."));
                        if (attempt == 0) {
                            try {
                                Thread.sleep(5000L);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } catch (org.springframework.web.client.HttpClientErrorException e) {
                        System.out.println("Groq error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                        throw e;
                    }
                }
                if (groqResponse != null)
                    break;
            }

            if (groqResponse == null) {
                response.put("reply", getSmartMockReply(userMessage));
                return ResponseEntity.ok(response);
            }

            // Parse response
            if (groqResponse.getStatusCode() == HttpStatus.OK && groqResponse.getBody() != null) {
                List choices = (List) groqResponse.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map message = (Map) ((Map) choices.get(0)).get("message");
                    if (message != null) {
                        response.put("reply", (String) message.get("content"));
                        return ResponseEntity.ok(response);
                    }
                }
            }
            response.put("reply", "Không nhận được phản hồi từ AI.");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("reply", "Lỗi: " + e.getMessage() + "\n\n" + getSmartMockReply(userMessage));
        }
        return ResponseEntity.ok(response);
    }

    private String getSmartMockReply(String query) {
        String lower = query.toLowerCase();
        if (lower.contains("nhân viên") || lower.contains("nhân sự"))
            return "Trợ lý ERP (Mô phỏng): Hệ thống HRM quản lý nhân sự, chấm công và lương.";
        if (lower.contains("tồn kho") || lower.contains("kho") || lower.contains("sản phẩm"))
            return "Trợ lý ERP (Mô phỏng): Phân hệ Kho ghi nhận tồn kho và cảnh báo hết hàng.";
        if (lower.contains("doanh thu") || lower.contains("tiền") || lower.contains("kế toán"))
            return "Trợ lý ERP (Mô phỏng): Phân hệ Kế toán theo dõi hóa đơn và sổ cái.";
        return "Chào bạn! Tôi là Trợ Lý AI của FOOD Mini ERP!";
    }
}