package nl.healthri.fairdatapoint.search.database;

import org.eclipse.rdf4j.model.Model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FdpRecord {
    public final Set<String> children;
    public final String url;
    public final Model model;

    public FdpRecord(String url, Model model) {
        this.url = url;
        this.children = new HashSet<>();
        this.model = model;
    }

    public void addChildren(String url) {
        this.children.add(url);
    }

    public Set<String> children() {
        return Collections.unmodifiableSet(children);
    }

}