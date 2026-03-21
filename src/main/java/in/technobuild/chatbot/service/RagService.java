package in.technobuild.chatbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.technobuild.chatbot.client.PythonAiClient;
import in.technobuild.chatbot.entity.VectorChunk;
import in.technobuild.chatbot.repository.VectorChunkRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final PythonAiClient pythonAiClient;
    private final VectorChunkRepository vectorChunkRepository;
    private final ObjectMapper objectMapper;

    public List<String> retrieveRelevantChunks(String query, String category) {
        List<Float> queryVector = pythonAiClient.embed(query);

        List<VectorChunk> candidates = (category == null || category.isBlank())
                ? vectorChunkRepository.findAll()
                : vectorChunkRepository.findByCategory(category);

        List<ScoredChunk> vectorTop = queryVector.isEmpty()
                ? List.of()
                : candidates.stream()
                .map(chunk -> new ScoredChunk(chunk.getContent(),
                        calculateCosineSimilarity(queryVector, parseEmbedding(chunk.getEmbedding()))))
                .sorted(Comparator.comparing(ScoredChunk::score).reversed())
                .limit(20)
                .toList();

        List<VectorChunk> bm25 = vectorChunkRepository.findByFullText(query, category, 20);
        List<String> bm25Texts = bm25.stream().map(VectorChunk::getContent).toList();

        Map<String, Double> rrfScores = new HashMap<>();

        for (int i = 0; i < vectorTop.size(); i++) {
            String text = vectorTop.get(i).content();
            rrfScores.put(text, rrfScores.getOrDefault(text, 0.0) + (1.0 / (60 + (i + 1))));
        }

        for (int i = 0; i < bm25Texts.size(); i++) {
            String text = bm25Texts.get(i);
            rrfScores.put(text, rrfScores.getOrDefault(text, 0.0) + (1.0 / (60 + (i + 1))));
        }

        List<String> top20Texts = rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .toList();

        List<PythonAiClient.RerankResult> reranked = pythonAiClient.rerank(query, top20Texts, 3);

        return reranked.stream()
                .map(PythonAiClient.RerankResult::text)
                .filter(text -> text != null && !text.isBlank())
                .distinct()
                .limit(3)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private double calculateCosineSimilarity(List<Float> vec1, List<Float> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty() || vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            double a = vec1.get(i);
            double b = vec2.get(i);
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private List<Float> parseEmbedding(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<List<Float>>() {
            });
        } catch (Exception ex) {
            log.error("Failed to parse embedding JSON", ex);
            return List.of();
        }
    }

    private record ScoredChunk(String content, double score) {
    }
}
