package nl.healthri.fairdatapoint.search.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FdpRecordStoreBatches implements FdpRecordStore {

    private final List<FdpRecord> buffer = new ArrayList<>();
    private final int batchSize;

    public FdpRecordStoreBatches(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void store(FdpRecord rec) {
        buffer.add(rec);
        if (buffer.size() == batchSize) {
            store(Collections.unmodifiableList(buffer));
            buffer.clear();
        }

    }

    abstract void store(List<FdpRecord> batch);

    @Override
    public void done() {
        if (!buffer.isEmpty()) {
            store(Collections.unmodifiableList(buffer));
        }
    }
}
