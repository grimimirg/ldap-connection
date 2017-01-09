package it.grimi.ldapConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader
{
    public String file = "default.properties";

    public PropertiesReader()
    {
    }

    public PropertiesReader(String file)
    {
        this.file = file;
    }

    public String getPropValue(String propName) throws IOException
    {
        Properties customProp = new Properties();
        InputStream fileInputStream = new FileInputStream(this.file);
        customProp.load(fileInputStream);
        return customProp.getProperty(propName);
    }

}
