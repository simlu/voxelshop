package com.vitco.util.xml;

import com.vitco.util.error.ErrorHandlerInterface;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Some basic xml functionality
 */
public class XmlTools {
    public static boolean validateAgainstXSD(String xml, String xsd, ErrorHandlerInterface errorHandler)
    {
        return validateAgainstXSD(xml, new StreamSource(xsd), errorHandler);
    }

    public static boolean validateAgainstXSD(String xml, StreamSource xsd, ErrorHandlerInterface errorHandler)
    {
        try
        {
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsd);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            return true;
        }
        catch(Exception ex)
        {
            errorHandler.handle(ex);
            return false;
        }
    }
}
