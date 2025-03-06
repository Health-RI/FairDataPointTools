package nl.healthri.fairdatapoint.search.database;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FdpRecord {
    public final Set<String> children;
    public final String url;
    public final Model model;

    public FdpRecord(String url, Model model) {
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
        this.children.add(url);
    }

    public Set<String> children() {
        return Collections.unmodifiableSet(children);
    }

    public Set<String> types() {
        return model.filter(SimpleValueFactory.getInstance().createIRI(url), RDF.TYPE, null)
                .objects().stream().map(FdpRecord::iriToString).collect(Collectors.toSet());
    }
}