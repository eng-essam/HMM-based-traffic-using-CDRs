/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xml;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

/**
 *
 * @author essam
 */
public class main_osm_to_xsd {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String path = args[0];
        try {

            XmlObject[] xmlInstances = new XmlObject[1];
            xmlInstances[0] = XmlObject.Factory.parse(path);

            Inst2XsdOptions inst2XsdOptions = new Inst2XsdOptions();

//            if (design == null || design == XMLSchemaDesign.VENETIAN_BLIND) {
            inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_VENETIAN_BLIND);
//        } else if (design == XMLSchemaDesign.RUSSIAN_DOLL) {
//            inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_RUSSIAN_DOLL);
//        } else if (design == XMLSchemaDesign.SALAMI_SLICE) {
//            inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_SALAMI_SLICE);
//        }

            SchemaDocument[] schemas = Inst2Xsd.inst2xsd(xmlInstances, inst2XsdOptions);
            SchemaDocument schemaDocument = schemas[0];

            StringWriter writer = new StringWriter();
            schemaDocument.save(writer, new XmlOptions().setSavePrettyPrint());
            writer.close();

            String xmlText = writer.toString();
            System.out.println(xmlText);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
