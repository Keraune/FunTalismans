package ft.keraune.funtalismans.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class HoconLoader {

    public static Config load(File file) {

        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "HOCON file not found: " + file.getAbsolutePath()
            );
        }

        return ConfigFactory.parseFile(file).resolve();
    }
}
