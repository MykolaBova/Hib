package org.julp.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public abstract class AbstractConfig implements Config {

    protected String configFilePath;
    protected URL configURL;
    protected Reader reader;
    protected Document doc;
    protected boolean reloadDocument;

    public AbstractConfig() {
    }

    public void load() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // Create the builder and parse the file
            if (doc == null || reloadDocument) {
                if (configFilePath != null) {
                    doc = factory.newDocumentBuilder().parse(new File(configFilePath));
                } else if (configURL != null) {
                    doc = factory.newDocumentBuilder().parse(configURL.toString());
                } else if (reader != null) {
                    doc = factory.newDocumentBuilder().parse(new InputSource(reader));
                } else {
                    throw new IOException("Config source is missing");
                }
            }      
        } catch (Exception ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public void load(String configFilePath) {
        setConfigFilePath(configFilePath);
        load();
    }

    @Override
    public void load(URL configURL) {
        setConfigURL(configURL);
        load();
    }

    @Override
    public void load(Reader reader) {
        setReader(reader);
        load();
    }

    @Override
    public void setXML(String xml) {
        StringReader stringReader = new StringReader(xml);
        setReader(stringReader);
    }

    @Override
    public String getXML() {
        String xml = null;
        DOMImplementationLS impl = null;
        try {
            //testing the support for DOM Load and Save
            if ((doc.getFeature("Core", "3.0") != null) && (doc.getFeature("LS", "3.0") != null)) {
                impl = (DOMImplementationLS) (doc.getImplementation()).getFeature("LS", "3.0");
            } else {
                throw new RuntimeException("DOM Load and Save is unsupported");
            }

            LSSerializer serializer = impl.createLSSerializer();
            xml = serializer.writeToString(doc);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure to output XML: " + e.toString(), e);
        }
        return xml;
    }

    @Override
    public void write() {
        OutputStream out = null;
        try {
            if (configFilePath != null) {
                out = new FileOutputStream(configFilePath);
            } else if (configURL != null) {
                out = configURL.openConnection().getOutputStream();
            } else {
                throw new IOException("Output location is missing");
            }

            DOMImplementationLS impl = null;
            try {
                //testing the support for DOM Load and Save
                if ((doc.getFeature("Core", "3.0") != null) && (doc.getFeature("LS", "3.0") != null)) {
                    impl = (DOMImplementationLS) (doc.getImplementation()).getFeature("LS", "3.0");
                } else {
                    throw new RuntimeException("DOM Load and Save is unsupported");
                }

                LSSerializer serializer = impl.createLSSerializer();                
                LSOutput lso = impl.createLSOutput();                
                lso.setByteStream((OutputStream) out);                                
                boolean success = serializer.write(doc, lso);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failure to output XML: " + e.toString(), e);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failure to save configuration: " + e.toString(), e);
        } finally {
            if (out != null) {
                try {                
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failure to close configuration file: " + e.toString(), e);
                }
            }
        }
    }

    @Override
    public void setConfigValue(String key, String value) {
        if (doc == null) {
            throw new NullPointerException("Document does not exist");
        }
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            Node node = (Node) xpath.evaluate(key, doc, XPathConstants.NODE);
            if (node == null) {
                throw new IllegalArgumentException("Content for key \"" + key + "\" does not exist");
            }            
            node.setTextContent(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getConfigValue(String key) {
        if (doc == null) {
            throw new NullPointerException("Document does not exist");
        }
        String value = null;
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            Node node = (Node) xpath.evaluate(key, doc, XPathConstants.NODE);
            if (node == null) {
                throw new IllegalArgumentException("Content for key \"" + key + "\" does not exist");
            }
            value = node.getTextContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    @Override
    public List<String> getConfigValues(String key) {
        if (doc == null) {
            throw new NullPointerException("Document does not exist");
        }
        List values = null;
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(key, doc, XPathConstants.NODESET);
            values = new ArrayList<>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                String value = nodes.item(i).getTextContent();
                if (value != null) {
                    value = value.trim();
                }
                values.add(value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return values;
    }

    @Override
    public String getConfigFilePath() {
        return configFilePath;
    }

    @Override
    public void setConfigFilePath(String path) {
        this.reader = null;
        this.configURL = null;
        this.configFilePath = path;
    }

    @Override
    public URL getConfigURL() {
        return configURL;
    }

    @Override
    public void setConfigURL(URL url) {
        this.reader = null;
        this.configFilePath = null;
        this.configURL = url;
    }

    @Override
    public Document getDocument() {
        return doc;
    }

    public void setDocument(Document doc) {
        this.doc = doc;
    }

    @Override
    public Reader getReader() {
        return reader;
    }

    @Override
    public void setReader(Reader reader) {
        this.configFilePath = null;
        this.configURL = null;
        this.reader = reader;
    }
}
