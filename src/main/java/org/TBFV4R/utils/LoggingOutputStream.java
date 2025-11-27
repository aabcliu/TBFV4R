package org.TBFV4R.utils;

import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.OutputStream;

public class LoggingOutputStream extends OutputStream {
    private final Logger logger;
    private final boolean isError;
    private StringBuilder buffer = new StringBuilder();

    public LoggingOutputStream(Logger logger, boolean isError) {
        this.logger = logger;
        this.isError = isError;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            flush();
        } else {
            buffer.append((char) b);
        }
    }

    @Override
    public void flush() {
        if (!buffer.isEmpty()) {
            if (isError) {
                logger.error(buffer.toString());
            } else {
                logger.info(buffer.toString());
            }
            buffer.setLength(0);
        }
    }
}
