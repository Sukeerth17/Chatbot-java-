package in.technobuild.chatbot.service;

import in.technobuild.chatbot.client.PythonAiClient;
import in.technobuild.chatbot.entity.VectorChunk;
import in.technobuild.chatbot.repository.VectorChunkRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RagService {

    private final PythonAiClient pythonAiClient;
    private final VectorChunkRepository vectorChunkRepository;

    public List<String> retrieveRelevantChunks(String query, String category) {
        List<Float> queryVector = pythonAiClient.embed(query);
        List<VectorChunk> vectorTop = queryVector.isEmpty()
                ? List.of()
                : vectorChunkRepository.findByVectorSimilarity(toPgVectorLiteral(queryVector), category, 20);

        List<VectorChunk> bm25 = vectorChunkRepository.findByFullText(query, category, 20);
        List<String> bm25Texts = bm25.stream().map(VectorChunk::getContent).toList();

        Map<String, Double> rrfScores = new HashMap<>();

        for (int i = 0; i < vectorTop.size(); i++) {
            String text = vectorTop.get(i).getContent();
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

    private String toPgVectorLiteral(List<Float> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(embedding.get(i));
        }
        sb.append(']');
        return sb.toString();
    }
}
