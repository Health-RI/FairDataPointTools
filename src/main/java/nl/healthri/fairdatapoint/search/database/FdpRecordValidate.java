package nl.healthri.fairdatapoint.search.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nl.healthri.fairdatapoint.search.FdpUtils;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class FdpRecordValidate extends FdpRecordStoreBatches {

    private static final Logger LOGGER = LoggerFactory.getLogger(FdpRecordValidate.class);

    private final static int DEFAULT_BATCH_SIZE = 10;

    private final URI shaclValidatorServer;
    private final ObjectWriter ow = new ObjectMapper().writer();
    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.of(500, ChronoUnit.MILLIS))
            .build();

    private final String profile;

    public FdpRecordValidate(URI shaclValidatorServer, String profile) {
        super(DEFAULT_BATCH_SIZE);
        this.shaclValidatorServer = shaclValidatorServer;
        this.profile = profile;
    }


    @Override
    void store(List<FdpRecord> batch) {
        try {
            RequestBodyJson[] data = batch.stream().map(fdpRec -> new RequestBodyJson(fdpRec.model, profile)).toArray(RequestBodyJson[]::new);
            String body = ow.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(shaclValidatorServer)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            FdpUtils.checkStatusCode(response);

            ResultBodyJson[] results = om.readValue(response.body(), ResultBodyJson[].class);

            for (int i = 0; i < batch.size(); i++) {
                new ReportSummary(batch.get(i), results[i].convertToReportModel()).printSummary();
            }
        } catch (RuntimeException | IOException | InterruptedException e) {
            LOGGER.error("Validation failed: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    //This is the JSON for the request
    @SuppressWarnings("unused")
    public static class RequestBodyJson {
        public final String contentToValidate;
        public final String contentSyntax = "text/turtle";
        public final String validationType;
        public String embeddingMethod = "BASE64";
        public String reportSyntax = "application/rdf+xml";

        public RequestBodyJson(Model model, String profile) {
            StringWriter sw = new StringWriter();
            Rio.write(model, sw, RDFFormat.TURTLE);
            contentToValidate = new String(Base64.getEncoder().encode(sw.toString().getBytes(StandardCharsets.UTF_8)));
            validationType = profile;
        }
    }

    //this is the JSON for http response.
    @SuppressWarnings("unused")
    public static class ResultBodyJson {
        public String report;
        public String reportSyntax;

        //NOTE the response is base64 encode RDF-XML file.
        public Model convertToReportModel() throws IOException {
            String reportRdfXml = new String(Base64.getDecoder().decode(report), StandardCharsets.UTF_8);
            return Rio.parse(new StringReader(reportRdfXml), "", RDFFormat.RDFXML);
        }
    }

    public static class ReportSummary {
        public final String url;
        public final boolean valid;
        public Set<String> errors = new HashSet<>();

        public ReportSummary(FdpRecord rec, Model model) {
            url = rec.url;
            ValueFactory valueFactory = SimpleValueFactory.getInstance();
            Literal trueLiteral = valueFactory.createLiteral("true", XSD.BOOLEAN);
            valid = model.contains(null, SHACL.CONFORMS, trueLiteral);

            model.filter(null, SHACL.RESULT, null).objects().forEach(subj -> {
                if (subj instanceof BNode bnode) {
                    List<String> desc = new ArrayList<>();

                    Models.objectIRI(model.filter(bnode, SHACL.RESULT_SEVERITY, null))
                            .ifPresent(i -> desc.add("Severity: " + i.stringValue()));

                    Models.objectLiteral(model.filter(bnode, SHACL.RESULT_MESSAGE, null))
                            .ifPresent(l -> desc.add("Message: " + l.getLabel()));

                    Models.object(model.filter(bnode, SHACL.FOCUS_NODE, null))
                            .ifPresent(v -> desc.add("Focus node: " + v.stringValue()));

                    Models.object(model.filter(bnode, SHACL.RESULT_PATH, null))
                            .ifPresent(v -> desc.add("Result path: " + v.stringValue()));

                    errors.add(String.join(", ", desc));
                }
            });
        }


        public void printSummary() {
            System.out.println("url: " + url + " valid: " + valid);
            if (!errors.isEmpty()) {
                errors.stream().map(s -> "--> " + s).forEach(System.out::println);
            }
        }

    }
}