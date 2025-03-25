package nl.healthri.fairdatapoint.search.database;

import nl.healthri.fairdatapoint.search.FairDataPointHarvester;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FdpRecord {
    public final Set<FairDataPointHarvester.UrlAndLevel> children;
    public final FairDataPointHarvester.UrlAndLevel url;
    public final Model model;

    public FdpRecord(FairDataPointHarvester.UrlAndLevel url, Model model) {
        this.url = url;
        this.children = new HashSet<>();
        this.model = model;
    }

    private static String iriToString(Value o) {
        if (o instanceof SimpleIRI si) {
            return si.getLocalName();
        }
        return o.toString();
    }

    public void addChildren(String url) {
        this.children.add(new FairDataPointHarvester.UrlAndLevel(url, this.url.level() + 1));
    }

    public Set<FairDataPointHarvester.UrlAndLevel> children() {
        return Collections.unmodifiableSet(children);
    }

    public Set<String> types() {
        return model.filter(SimpleValueFactory.getInstance().createIRI(url.url()), RDF.TYPE, null)
                .objects().stream().map(FdpRecord::iriToString).collect(Collectors.toSet());
    }

    public String title() {
        return model.filter(SimpleValueFactory.getInstance().createIRI(url.url()), DCTERMS.TITLE, null)
                .objects().stream().map(Value::toString).collect(Collectors.joining(", "));
    }

}