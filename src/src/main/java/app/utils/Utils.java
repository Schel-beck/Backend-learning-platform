
package app.utils;

import app.exceptions.ApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    public static String getPropertyValue(
            String propName,
            String resourceName
    ) {
        try (InputStream is = Utils.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {

            if (is == null) {
                throw new ApiException(
                        500,
                        String.format(
                                "Properties file '%s' was not found on the classpath.",
                                resourceName
                        )
                );
            }

            Properties prop = new Properties();
            prop.load(is);

            String value = prop.getProperty(propName);

            if (value == null || value.isBlank()) {
                throw new ApiException(
                        500,
                        String.format(
                                "Property '%s' not found or empty in '%s'.",
                                propName,
                                resourceName
                        )
                );
            }

            return value.trim();

        } catch (IOException ex) {
            throw new ApiException(
                    500,
                    String.format(
                            "Could not read properties file '%s'.",
                            resourceName
                    )
            );
        }
    }
}
