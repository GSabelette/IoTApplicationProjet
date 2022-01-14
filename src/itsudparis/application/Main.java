/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package itsudparis.application;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.rdf.model.Model;
import itsudparis.tools.JenaEngine;

import java.util.Scanner;

/**
 *
 * @author DO.ITSUDPARIS
 */
public class Main {

    /**
     * @param args rhe command line arguments
     */


    public static void main(String[] args) {
        String NS = "";
        // lire le model a partir d'une ontologie
        Model model = JenaEngine.readModel("data/famille.owl");
        if (model != null) {
            //lire le Namespace de l’ontologie
            NS = model.getNsPrefixURI("");
            //apply owl rules on the model
            Model owlInferencedModel =
                    JenaEngine.readInferencedModelFromRuleFile(model, "data/owlrules.txt");
            // apply our rules on the owlInferencedModel
            Model inferedModel =
                    JenaEngine.readInferencedModelFromRuleFile(owlInferencedModel,"data/rules.txt");

            System.out.println(JenaEngine.executeQueryFileWithParameter(inferedModel, "data/query.txt", "Thomas"));

        } else {
            System.out.println("Error when reading model from ontology");
        }

    }
}

