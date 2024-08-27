package nl.healthri.fairdatapoint.search.apps;

import nl.healthri.fairdatapoint.search.FairDataPointHarvester;
import nl.healthri.fairdatapoint.search.database.FdpRecordGraphEmbeddingSearch;
import nl.healthri.fairdatapoint.search.database.FdpRecordSearchableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Scanner;


public class FdpSearchApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(FdpSearchApp.class);

    public static void run(URI uri, Path model) {

        try (FdpRecordSearchableStore database = new FdpRecordGraphEmbeddingSearch(model);
             FairDataPointHarvester harvester = new FairDataPointHarvester()) {

            LOGGER.info("harvesting: {}", uri);
            harvester.harvest(uri, database); //will store all description in memory.

            Scanner scanner = new Scanner(System.in);
            String line;
            do {
                System.out.println("Search the catalog:");
                line = scanner.nextLine();
                System.out.println("Searching for: " + line);
                database.search(line);
                System.out.println();
            } while (!line.isBlank());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
