package org.app.courseapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.response.DevelopmentVerdict;
import org.app.courseapp.model.RegistrationAnswer;
import org.app.courseapp.model.RegistrationQuestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DevelopmentAnalysisService {

    @Value("${groq.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    public DevelopmentVerdict analyze(List<RegistrationAnswer> answers) {
        try {
            String prompt = buildPrompt(answers);
            String response = callGroqApi(prompt);
            return parseVerdict(response, answers);
        } catch (Exception e) {
            log.error("Failed to analyze development", e);
            return buildFallbackVerdict(answers);
        }
    }

    public DevelopmentVerdict analyzeTest() {
        return analyze(createTestAnswers());
    }

    private List<RegistrationAnswer> createTestAnswers() {
        String[][] data = {
                {"Была ли гипоксия при родах?",                         "Роды и беременность", "false"},
                {"Были ли родовые травмы или инфекции в первый месяц?", "Роды и беременность", "false"},
                {"Протекала ли беременность с серьезными осложнениями?","Роды и беременность", "false"},
                {"Ребенок уверенно держит голову и переворачивается?",  "Моторика",            "true"},
                {"Умеет ли ребенок самостоятельно сидеть и ползать?",   "Моторика",            "true"},
                {"Может ли ребенок захватывать мелкие предметы?",       "Моторика",            "false"},
                {"Реагирует ли ребенок на свое имя?",                   "Речь",                "true"},
                {"Понимает ли ребенок простые инструкции?",             "Речь",                "true"},
                {"Есть ли указательные жесты или первые слова?",        "Речь",                "false"},
                {"Устанавливает ли ребенок зрительный контакт?",        "Социализация",        "true"},
                {"Проявляет ли интерес к играм с другими детьми?",      "Социализация",        "true"},
                {"Есть ли комплекс оживления (улыбка в ответ)?",        "Социализация",        "true"},
                {"Проявляет ли интерес к самостоятельному приему пищи?","Самообслуживание",    "true"},
                {"Дает ли знать о физиологических нуждах?",             "Самообслуживание",    "true"},
                {"Пытается ли помогать при одевании?",                  "Самообслуживание",    "false"},
        };

        return Arrays.stream(data).map(row -> {
            RegistrationQuestion question = new RegistrationQuestion();
            question.setQuestion(row[0]);
            question.setTopic(row[1]);

            RegistrationAnswer answer = new RegistrationAnswer();
            answer.setQuestion(question);
            answer.setAnswer(Boolean.parseBoolean(row[2]));
            return answer;
        }).toList();
    }

    private String buildPrompt(List<RegistrationAnswer> answers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ты — детский нейропсихолог. Проанализируй ответы родителя на вопросы о развитии ребёнка.\n\n");
        sb.append("Ответы на вопросы:\n");

        Map<String, List<RegistrationAnswer>> byTopic = answers.stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getTopic()));

        byTopic.forEach((topic, topicAnswers) -> {
            sb.append("\n📌 ").append(topic).append(":\n");
            topicAnswers.forEach(a -> {
                String answerStr = a.getAnswer() ? "Да ✓" : "Нет ✗";
                sb.append("  - ").append(a.getQuestion().getQuestion())
                        .append(" → ").append(answerStr).append("\n");
            });
        });

        sb.append("""
                
                Дай ответ СТРОГО в формате JSON (без markdown, только чистый JSON):
                {
                  "verdict": "НОРМА" | "ТРЕБУЕТ ВНИМАНИЯ" | "ЗОНА РИСКА",
                  "analysis": "2-3 предложения об анализе по блокам развития",
                  "recommendation": "конкретные рекомендации специалистов",
                  "conclusion": "итоговая фраза-резюме"
                }
                
                Правила вердикта:
                - НОРМА: большинство ответов "Да", единичные "Нет" не критичны
                - ТРЕБУЕТ ВНИМАНИЯ: несколько "Нет" в разных блоках
                - ЗОНА РИСКА: много "Нет" или критичные "Нет" (речь + социализация)
                """);

        return sb.toString();
    }

    private String callGroqApi(String prompt) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body)
                ))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        log.debug("Groq response: {}", response.body());

        if (response.statusCode() != 200) {
            log.error("Groq API error: {}", response.body());
            throw new RuntimeException("Groq API error: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").get(0)
                .path("message").path("content").asText();
    }

    private DevelopmentVerdict parseVerdict(String jsonText, List<RegistrationAnswer> answers) {
        try {
            // Gemini иногда оборачивает в ```json ... ```
            String clean = jsonText.replaceAll("(?s)```json\\s*|```", "").trim();
            JsonNode node = objectMapper.readTree(clean);

            long positive = answers.stream().filter(RegistrationAnswer::getAnswer).count();

            return DevelopmentVerdict.builder()
                    .verdict(node.path("verdict").asText("НОРМА"))
                    .analysis(node.path("analysis").asText())
                    .recommendation(node.path("recommendation").asText())
                    .conclusion(node.path("conclusion").asText())
                    .positiveCount((int) positive)
                    .totalCount(answers.size())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", jsonText, e);
            return buildFallbackVerdict(answers);
        }
    }

    private DevelopmentVerdict buildFallbackVerdict(List<RegistrationAnswer> answers) {
        long positive = answers.stream().filter(RegistrationAnswer::getAnswer).count();
        double ratio = (double) positive / answers.size();

        String verdict = ratio >= 0.8 ? "НОРМА" :
                ratio >= 0.5 ? "ТРЕБУЕТ ВНИМАНИЯ" : "ЗОНА РИСКА";

        return DevelopmentVerdict.builder()
                .verdict(verdict)
                .analysis("Анализ на основе ваших ответов.")
                .recommendation("Рекомендуем проконсультироваться со специалистом.")
                .conclusion("Результаты будут уточнены после консультации.")
                .positiveCount((int) positive)
                .totalCount(answers.size())
                .build();
    }
}