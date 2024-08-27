package nl.healthri.fairdatapoint.search.database;

public interface FdpRecordStore extends AutoCloseable {
    void store(FdpRecord rec);

    void done();

}
