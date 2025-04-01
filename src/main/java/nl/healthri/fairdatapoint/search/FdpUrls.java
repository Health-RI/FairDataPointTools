package nl.healthri.fairdatapoint.search;

import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("unused")
public enum FdpUrls {

    /**
     * for the --fdp option a list of known fdp's
     */
    LOCAL_HOST("http://localhost:80"), //
    HEALTH_RI_TEST("https://fdp-test.healthdata.nl/"),
    HEALTH_RI("https://fdp.healthdata.nl/"),
    UMCG("https://tccweb.umcg.nl/Umcg.Euphoria.FairDataPointService-1"),
    LOVD("https://fdp.lovd.nl"),
    ORPHANET("http://fairdatapointorphanet.info/"),
    EU_FDP("https://fair.healthinformationportal.eu/"),
    MOLGENIS("https://demo-rd3.molgenis.net/catalogue/api/rdf"),
    LUMC("https://fdp.lumc.nl");
    public final URI uri;

    FdpUrls(String url) {
        try {
            uri = new URI(url);
        } catch (URISyntaxException ue) {
            throw new RuntimeException(ue);
        }
    }
}
