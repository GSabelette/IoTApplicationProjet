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
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

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
        System.out.println(filename);
        InputStream inputStream = new FileInputStream(filename);
        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
        String firstLine = scanner.nextLine();
        // N locations
        String[] locations = firstLine.split(",");
        int n = 1;

        while (scanner.hasNextLine()) {
            n++;
            String line = scanner.nextLine();
            String[] values = line.split(",");
            if (line.endsWith(",")) {
                int i = 0;
                while (line.substring(line.length() - 1 - i).matches(",+")) {
                    i++;
                }
                String[] tmp = values;
                values = new String[values.length + i];
                System.arraycopy(tmp, 0, values, 0, tmp.length);
                for (int j = tmp.length; j < values.length; j++) {
                    values[j] = "";
                }

            }
            // Date, Particle, Concentration1, Concentration2, ..., ConcentrationN
            if (values.length != (locations.length + 2)) {
                System.out.println(line);
                System.out.println(String.join(", ", values));
                System.out.println("line length " + values.length);
                System.out.println("n locations " + locations.length);
                throw new IllegalArgumentException("Bad data format");
            }

            for (int i = 0; i < locations.length; i++) {
                String instanceName = values[1] + "Measure_" + values[0] +"_" + values[2+i];
                Float f;
                if (values[2+i].equals("")) {
                    f = Float.NaN;
                } else {
                    f = Float.parseFloat(values[2 + i]);
                }
                JenaEngine.createInstanceOfClass(model, NS, "Measure", instanceName);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "Date", values[0]);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "Particle", values[1]);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "Concentration", f);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "Location", locations[i]);
                JenaEngine.addValueOfDataTypeProperty(model, NS, instanceName, "AirQuality", "Correcte");
            }
        }
    }

    /**
     * @param args rhe command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        String NS = "http://www.semanticweb.org/valentin/ontologies/2022/0/untitled-ontology-7#";
        // lire le model a partir d'une ontologie
        Model model = JenaEngine.readModel("data/measures.owl");
        String dataPath = "data/source/airparif/dataset/air-quality/version/2022-Jan-13/2022_%s.csv";
        String[] filenames = new String[]{
                String.format(dataPath, "CO"),
                String.format(dataPath, "NOX"),
                String.format(dataPath, "O3"),
                String.format(dataPath, "PM25"),
                String.format(dataPath, "SO2")
        };
        if (model != null) {
            // create all instances from csv files
            for (String filename: filenames) {
                fillModelWithData(model, filename, NS);
            }
            System.out.println("End of instances creations");
            //apply owl rules on the model
            Model owlInferencedModel =
                    JenaEngine.readInferencedModelFromRuleFile(model, "data/owlrules.txt");
            // apply our rules on the owlInferencedModel
            Model inferedModel =
                    JenaEngine.readInferencedModelFromRuleFile(owlInferencedModel, "data/rules.txt");

            String userInput;
            Scanner scanner = new Scanner(System.in);
            System.out.println("Choose a location to know air quality:");
            while (!Objects.equals(userInput = scanner.nextLine().trim(), "q")) {
                System.out.println(JenaEngine.executeQueryFileWithParameter(inferedModel, "data/query.txt", userInput));
                System.out.println("Choose a location to know air quality:");
            }
        } else {
            System.out.println("Error when reading model from ontology");
        }
    }
}

