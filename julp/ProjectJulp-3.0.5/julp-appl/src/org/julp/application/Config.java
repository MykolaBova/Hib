package org.julp.application;

import java.io.Reader;
import java.net.URL;
import java.util.List;
import org.w3c.dom.Document;

public interface Config {

    void load(String configFilePath);

    void load(URL configURI);

    void load(Reader reader);

    void write();

    void setConfigValue(String key, String value);

    String getConfigValue(String key);

    List getConfigValues(String key);

    String getConfigFilePath();

    void setConfigFilePath(String path);

    URL getConfigURL();

    void setConfigURL(URL url);

    Document getDocument();

    void setJDOMDocument(Document Document);

    Reader getReader();

    void setReader(Reader reader);

    void setXML(String xml);

    String getXML();
}
