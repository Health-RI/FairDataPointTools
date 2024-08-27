package nl.healthri.fairdatapoint.search.database;

import nl.healthri.fairdatapoint.search.EmbeddingEngine;
import nl.healthri.fairdatapoint.search.FdpUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class FdpRecordGraphEmbeddingSearch extends FdpRecordStoreBatches implements FdpRecordSearchableStore {
    private final EmbeddingEngine ee;
    private float[][] embeddings = null;
    private String[] payload = null;

    public FdpRecordGraphEmbeddingSearch(Path model) {
        super(Integer.MAX_VALUE);
        ee = new EmbeddingEngine(model);
    }

    private static double cosineDistance(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Array should have same size");
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += Math.pow(a[i], 2);
            normB += Math.pow(b[i], 2);
        }
        double cosineSimilarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return 1.0 - cosineSimilarity;
    }

    public void store(List<FdpRecord> recs) {
        List<RecordData> texts = recs.stream().map(RecordData::new).toList();
        String[] data = texts.stream().flatMap(RecordData::textStream).toArray(String[]::new);
        payload = texts.stream().flatMap(RecordData::urlStream).toArray(String[]::new);
        embeddings = ee.embed(data);
    }

    public void search(String text) {
        float[] searchVector = ee.embed(text);

        SortedMap<Double, String> result = new TreeMap<>();
        for (int i = 0; i < embeddings.length; i++) {
            double cosineSimilarity = cosineDistance(searchVector, embeddings[i]);
            result.put(cosineSimilarity, payload[i]);
        }

        result.entrySet().stream().limit(10).forEach(
                e -> System.out.printf("%.3f : %s\n", e.getKey(), e.getValue())
        );
    }

    @Override
    public void close() throws Exception {
        ee.close();
    }

    private static class RecordData {
        List<String> texts;
        String url;

        public RecordData(FdpRecord r) {
            texts = FdpUtils.extractDescriptions(r);
            url = r.url;
        }

        public Stream<String> textStream() {
            return texts.stream();
        }

        public Stream<String> urlStream() {
            return Stream.generate(() -> url).limit(texts.size());
        }

    }
}
