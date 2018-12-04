package ektara.com.BinObjectStorage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by mohoque on 11/01/2017.
 */

public class AppendResourceObjects extends ObjectOutputStream {

    public AppendResourceObjects(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // do not write a header, but reset:
        // this line added after another question
        // showed a problem with the original
        reset();
    }

}