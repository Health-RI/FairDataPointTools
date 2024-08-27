package nl.healthri.fairdatapoint.search;

import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TString;

import java.nio.file.Path;

public class EmbeddingEngine implements AutoCloseable {

    private final SavedModelBundle model;
    private final Session session;

    public EmbeddingEngine(Path useModel) {
        model = SavedModelBundle.load(useModel.toString(), "serve");
        session = model.session();
    }

    public float[] embed(String data) {
        return embed(new String[]{data})[0];
    }

    public float[][] embed(String[] data) {

        try (   //multiple resources
                // Convert the input text to a Tensor
                Tensor inputTensor = TString.tensorOf(NdArrays.vectorOfObjects(data));
                // run
                Result outputs = session.runner()
                        .feed("serving_default_inputs", inputTensor)
                        .fetch("StatefulPartitionedCall")
                        .run();
                //retrieve results
                TFloat32 embeddings = (TFloat32) outputs.get(0)) {

            return StdArrays.array2dCopyOf(embeddings);
        } //try will close resources
    }

    @Override
    public void close() {
        if (session != null) {
            session.close();
        }
        if (model != null) {
            model.close();
        }

    }
}
