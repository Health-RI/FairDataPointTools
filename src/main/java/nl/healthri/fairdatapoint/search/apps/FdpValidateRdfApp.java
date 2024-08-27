package nl.healthri.fairdatapoint.search.apps;

import nl.healthri.fairdatapoint.search.FairDataPointHarvester;
import nl.healthri.fairdatapoint.search.database.FdpRecordValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class FdpValidateRdfApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(FdpValidateRdfApp.class);

    public static void run(URI url, String shaclUri, String profile) {
        LOGGER.info("harvesting: {} using profile: {} ({})", url, shaclUri, profile);

        try (
                FairDataPointHarvester harvester = new FairDataPointHarvester();
                FdpRecordValidate database = new FdpRecordValidate(new URI(shaclUri), profile)) {

            harvester.harvest(url, database);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
