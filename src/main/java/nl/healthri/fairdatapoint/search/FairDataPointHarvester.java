package nl.healthri.fairdatapoint.search;

import nl.healthri.fairdatapoint.search.database.FdpRecordStore;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class FairDataPointHarvester implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FairDataPointHarvester.class);

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.of(500, ChronoUnit.MILLIS))
            .build();

    public void harvest(URI fdp, FdpRecordStore db) {
        LOGGER.trace("harvesting fdp: {} ", fdp);
        Queue<String> urls = new LinkedList<>();
        urls.add(fdp.toString());

        do {
            final String url = urls.remove();
            final FdpUtils.Mapper convertModelToRecord = new FdpUtils.Mapper(url);

            fetchModel(url).map(convertModelToRecord).ifPresent(fdpRecord -> {
                urls.addAll(fdpRecord.children());
                db.store(fdpRecord);
            });

        } while (!urls.isEmpty());
        LOGGER.trace("done harvesting fdp");
        db.done();
    }

    private Optional<Model> fetchModel(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() / 100 != 2) {
                throw new IOException("Could not fetch data: response code: " + response.statusCode());
            }

            InputStream inputStream = response.body();
            var result = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            LOGGER.debug("fetching model: {} model size: {}", url, result.size());
            return Optional.of(result);

        } catch (IOException ioe) {
            LOGGER.error("Could not fetch {} ", url);
        } catch (RDFParseException rpe) {
            LOGGER.error("Model fetched from: {} is not a valid rdf document", url);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
