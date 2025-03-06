package nl.healthri.fairdatapoint.search.apps;

import nl.healthri.fairdatapoint.search.FairDataPointHarvester;
import nl.healthri.fairdatapoint.search.database.FdpRecord;
import nl.healthri.fairdatapoint.search.database.FdpRecordStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class FdpPrintApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(FdpPrintApp.class);

    public static void run(URI url) {
        LOGGER.info("Start printing FDP: {} ", url);

        try (FdpRecordStore database = new FdpPrintRecordStore();
             FairDataPointHarvester harvester = new FairDataPointHarvester()) {
            harvester.harvest(url, database);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FdpPrintRecordStore implements FdpRecordStore {

        @Override
        public void store(FdpRecord rec) {
            System.out.println("*** url: " + rec.url + " : " + rec.types());
        }

        @Override
        public void done() {

        }

        @Override
        public void close() throws Exception {
        }
    }
}
