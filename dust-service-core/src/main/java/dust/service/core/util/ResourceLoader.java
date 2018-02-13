package dust.service.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by huangshengtao on 2016-6-1.
 */
public enum ResourceLoader {
    INSTANCE;

    private Properties loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        return properties;
    }

    private Properties loadPropertiesFromXml(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.loadFromXML(is);
        return properties;
    }

    public String getProperty(String pathToConfig, String key) {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(pathToConfig);
            if (is != null) {
                Properties properties = loadProperties(is);
                return properties.getProperty(key);
            }

        } catch (Exception ex) {
        }
        return "";
    }

    public String getProperty(Class c, String pathToConfig, String key) {
        try {
            InputStream is = c.getResourceAsStream(pathToConfig);
            if (is != null) {
                Properties properties = loadProperties(is);
                return properties.getProperty(key);
            }

        } catch (Exception ex) {
        }
        return "";
    }

    public String getPropertyFromXml(Class c, String pathToConfig, String key) {
        try {
            InputStream is = c.getResourceAsStream(pathToConfig);
            if (is != null) {
                Properties properties = loadPropertiesFromXml(is);
                return properties.getProperty(key);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
    }
