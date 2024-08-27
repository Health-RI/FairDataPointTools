package nl.healthri.fairdatapoint.search.database;

public interface FdpRecordSearchableStore extends FdpRecordStore {
    void search(String text);
}
