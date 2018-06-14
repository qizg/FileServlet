package com.sinwn.web.files.id;

import java.util.logging.Logger;

public final class Loggers {
    public static final String PREFIX = "org.bson";

    public static Logger getLogger(String suffix) {
        if (suffix == null) {
            throw new IllegalArgumentException("suffix can not be null");
        } else if (!suffix.startsWith(".") && !suffix.endsWith(".")) {
            return Logger.getLogger("org.bson." + suffix);
        } else {
            throw new IllegalArgumentException("The suffix can not start or end with a '.'");
        }
    }

    private Loggers() {
    }
}
