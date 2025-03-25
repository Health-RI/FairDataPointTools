package nl.healthri.fairdatapoint.search.database;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FdpRecordStoreOnDisk implements FdpRecordStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(FdpRecordStoreOnDisk.class);
    private final Path parentFolder;
    private long totalTriples = 0;
    private long totalRecord = 0;

    public FdpRecordStoreOnDisk(Path parent) {
        this.parentFolder = parent;
    }

    private Path getFolderFromUrl(String urlString) throws URISyntaxException {
        URI uri = new URI(urlString);
        String path = uri.getPath().replaceAll("[^a-zA-Z0-9./_-]", "_");
        if (path.endsWith("/")) {
            path += uri.getHost() + ".ttl";
        } else {
            path += ".ttl";
        }

        String[] filePath = path.split("/");

        LOGGER.debug("url: {} will become dir: {}", urlString, path);

        return Path.of(parentFolder.toString(), filePath);
    }

    @Override
    public void store(FdpRecord rec) {
        try {
            Path p = getFolderFromUrl(rec.url.url());
            if (!Files.exists(p)) {
                Files.createDirectories(p.getParent());
            }
            FileWriter writer = new FileWriter(p.toFile());
            RDFWriter w = Rio.createWriter(RDFFormat.TURTLE, writer);
            Rio.write(rec.model, w);

            totalTriples += rec.model.size();
            totalRecord += 1;
            System.out.println("totalRecords: " + totalRecord + " totalTriples: " + totalTriples);

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void done() {

    }

    @Override
    public void close() {
    }
}
