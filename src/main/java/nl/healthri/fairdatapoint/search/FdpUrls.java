package nl.healthri.fairdatapoint.search;

import java.net.URI;
import java.net.URISyntaxException;

public enum FdpUrls {

    LOCAL_HOST_FDP("http://localhost:80"), //
    HEALTH_RI_TEST_FDP("https://fdp-test.healthdata.nl/"),
    HEALTH_RI_FDP("https://fdp.healthdata.nl/"),
    UMCG_FDP("https://tccweb.umcg.nl/Umcg.Euphoria.FairDataPointService-1"),
    LOVD_FDP("https://fdp.lovd.nl"),
    ORPHANET_FDP("http://fairdatapointorphanet.info/"),
    EU_FDP("https://fair.healthinformationportal.eu/");

    public final URI uri;

    FdpUrls(String url) {
        try {
            uri = new URI(url);
        } catch (URISyntaxException ue) {
            throw new RuntimeException(ue);
        }
    }
}
