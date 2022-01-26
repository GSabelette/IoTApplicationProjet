/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package itsudparis.application;


import com.hp.hpl.jena.rdf.model.Model;
import itsudparis.tools.JenaEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author DO.ITSUDPARIS
 */
public class Main {


    /**
     * Create all instances and its properties for our Measures ontologie. The data are from a csv file.
     * @param model the model to fill.
     * @param filename the file where data are stored.
     * @param NS namespace of the ontology.
     * @throws FileNotFoundException in case file does not exist
     */
    private static void fillModelWithData(Model model, String filename, String NS) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(filename);
        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
        String firstLine = scanner.nextLine();
        // N locations
        String[] locations = firstLine.split(",");


        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");
            // Date, Particle, Concentration1, Concentration2, ..., ConcentrationN
            assert values.length == locations.length + 2;
            for (int i = 0; i < locations.length; i++) {
                String instanceName = values[1] + "Measure_" + values[0] +"_" + values[2+i];
                JenaEngine.createInstanceOfClass(model, NS, "Measure", instanceName);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "Date", values[0]);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "Particle", values[1]);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "Concentration", values[2 + i]);
            }
        }
    }

    /**
     * @param args rhe command line arguments
     */
    public static void main(String[] args) {
        String NS = "";
        // lire le model a partir d'une ontologie
        Model model = JenaEngine.readModel("data/measures.owl");
        if (model != null) {
            //lire le Namespace de l’ontologie
            NS = model.getNsPrefixURI("");
            //apply owl rules on the model
            Model owlInferencedModel =
                    JenaEngine.readInferencedModelFromRuleFile(model, "data/owlrules.txt");
            // apply our rules on the owlInferencedModel
            Model inferedModel =
                    JenaEngine.readInferencedModelFromRuleFile(owlInferencedModel, "data/rules.txt");

            System.out.println(JenaEngine.executeQueryFileWithParameter(inferedModel, "data/query.txt", "Thomas"));

        } else {
            System.out.println("Error when reading model from ontology");
        }

    }
}

