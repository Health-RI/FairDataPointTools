package nl.healthri.fairdatapoint.search;

import nl.healthri.fairdatapoint.search.database.FdpRecord;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.LDP;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class FdpUtils {

    private FdpUtils() {
        //intentionally empty.
    }

    public static void checkStatusCode(HttpResponse<?> response) throws IOException {
        int c = response.statusCode() / 100;
        if (c == 2) {
            return;
        }
        throw new IOException("Http response invalid: " + response.statusCode());
    }

    public static List<String> extractDescriptions(FdpRecord rec) {
        List<String> text = new ArrayList<>();
        BreakIterator sentenceIterator = BreakIterator.getSentenceInstance();
        rec.model.forEach(st -> {
            IRI predicate = st.getPredicate();
            if (DCTERMS.DESCRIPTION.equals(predicate) || DCTERMS.TITLE.equals(predicate)) {
                //ignore accessRight description..
                boolean skip = (st.getSubject() instanceof IRI siri && "accessRights".equals(siri.getLocalName()));
                if (!skip) {
                    String t = st.getObject().stringValue();
                    sentenceIterator.setText(t);
                    int first = sentenceIterator.first();
                    int last;
                    while ((last = sentenceIterator.next()) != BreakIterator.DONE) {
                        text.add(t.substring(first, last));
                        first = last;
                    }
                }
            }
        });
        return text;
    }

    public final static class Mapper implements Function<Model, FdpRecord> {
        final private String url;

        public Mapper(String url) {
            this.url = url;
        }

        @Override
        public FdpRecord apply(Model model) {
            FdpRecord record = new FdpRecord(url, model);

            model.forEach(statement -> {
                final var predicate = statement.getPredicate();
                if (LDP.CONTAINS.equals(predicate)) {
                    record.addChildren(statement.getObject().stringValue());
                }
            });
            return record;
        }
    }
}
