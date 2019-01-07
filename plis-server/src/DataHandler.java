import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Scanner;

public class DataHandler {
    private static JSONObject kibaLigands, kibaProteins, ligandJsons, proteinXmls, proteinJsons, kibaInteractions;
    private static String kibaAffinities[][];

    /**
     * Import data from kiba dataset.
     */
    public static void prepareCoreDataset() {
        System.out.println("Importing core dataset.");
        importLigands();
        importProteins();
        importAffinities();
        importOrCreateInteractions();
    }

    private static void importLigands() {
        System.out.println("Reading the ligand database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/kiba/kiba_ligands.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String ligandsDB = in.nextLine();
        kibaLigands = new JSONObject(ligandsDB);

        System.out.println("Imported " + kibaLigands.keySet().size() + " kibaLigands from core dataset.");
    }

    private static void importProteins() {
        System.out.println("Reading the protein database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/kiba/kiba_proteins.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String proteinsDB = in.nextLine();
        kibaProteins = new JSONObject(proteinsDB);

        System.out.println("Imported " + kibaProteins.keySet().size() + " kibaProteins from core dataset.");
    }

    private static void importAffinities() {
        kibaAffinities = new String[kibaLigands.keySet().size()][kibaProteins.keySet().size()];

        System.out.println("Reading the binding affinity database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/kiba/kiba_binding_affinity_v2.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < kibaAffinities.length; i++) {
            String line = in.nextLine();
            String[] cols = line.split("\t");
            for (int j = 0; j < cols.length; j++) {
                kibaAffinities[i][j] = cols[j];
            }
        }
        System.out.println("Imported " + kibaAffinities.length * kibaAffinities[0].length + " kibaAffinities from core dataset.");
    }

    private static void importOrCreateInteractions() {
        File interactionsFile = new File("files/kiba/kibaInteractions.txt");
        if (interactionsFile.exists()) {
            importInteractions();
        } else {
            createInteractions();
            saveInteractions();
        }
    }

    private static void importInteractions() {
        System.out.println("Reading the interaction database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/kiba/kibaInteractions.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String interactionsDB = in.nextLine();
        kibaInteractions = new JSONObject(interactionsDB);

        System.out.println("Imported " + kibaInteractions.keySet().size() + " interaction lists from core dataset.");
    }

    private static void createInteractions() {
        kibaInteractions = new JSONObject();
        for (int ligandIndex = 0; ligandIndex < kibaLigands.keySet().size(); ligandIndex++) {
            for (int proteinIndex = 0; proteinIndex < kibaProteins.keySet().size(); proteinIndex++) {
                String affinity = kibaAffinities[ligandIndex][proteinIndex];
                if (!affinity.equals("nan")) {
                    String ligand = kibaLigands.keySet().toArray()[ligandIndex].toString();
                    String protein = kibaProteins.keySet().toArray()[proteinIndex].toString();

                    if (!kibaInteractions.has(ligand)) {
                        kibaInteractions.put(ligand, new JSONArray());
                    }
                    kibaInteractions.put(ligand, kibaInteractions.getJSONArray(ligand).put(protein));


                    if (!kibaInteractions.has(protein)) {
                        kibaInteractions.put(protein, new JSONArray());
                    }
                    kibaInteractions.put(protein, kibaInteractions.getJSONArray(protein).put(ligand));
                }
            }
        }
    }

    private static void saveInteractions() {
        System.out.println("Saving interactions");
        try (FileWriter file = new FileWriter("files/kiba/kibaInteractions.txt")) {
            file.write(kibaInteractions.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void prepareCoreDatasetDetails() {
        // Check if detail files are already created. If so, import them.

        // Ligand details.
        File ligandFile = new File("files/kiba/ligandJsons.txt");
        if (ligandFile.exists()) {
            importLigandDetails();
        } else {
            fetchJsonDataForLigands();
            saveLigandJsons();
        }

        // Protein details in xml.
        File proteinXmlFile = new File("files/kiba/proteinXmls.txt");
        if (proteinXmlFile.exists()) {
            importProteinXmlDetails();
        } else {
            fetchXmlDataForProteins();
            saveProteinXmls();
        }

        // Protein details in json.
        File proteinFile = new File("files/kiba/proteinJsons.txt");
        if (proteinFile.exists()) {
            importProteinDetails();
        } else {
            convertProteinXmlsToJson();
            saveProteinJsons();
        }
    }

    private static void importLigandDetails() {
        System.out.println("Reading the ligand details database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/kiba/ligandJsons.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String ligandsDB = in.nextLine();
        ligandJsons = new JSONObject(ligandsDB);

        System.out.println("Imported " + ligandJsons.keySet().size() + " ligand details from core dataset.");
    }

    private static void fetchJsonDataForLigands() {
        int i = 0;
        ligandJsons = new JSONObject("{}");
        for (String ligand : kibaLigands.keySet()) {
            i++;
            System.out.println("Starting for " + ligand);
            JSONObject json = requestLigandJsonByChemblId(ligand);
            ligandJsons.put(ligand, json);
            System.out.println("Finished " + i + "/" + kibaLigands.keySet().size());
        }
    }

    private static void saveLigandJsons() {
        System.out.println("Saving fetched data");
        try (FileWriter file = new FileWriter("files/kiba/ligandJsons.txt")) {
            file.write(ligandJsons.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void importProteinXmlDetails() {
        System.out.println("Reading the protein xml details database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/kiba/proteinXmls.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String proteinXmlDB = in.nextLine();
        proteinXmls = new JSONObject(proteinXmlDB);

        System.out.println("Imported " + proteinXmls.keySet().size() + " protein xml details from core dataset.");
    }

    private static void fetchXmlDataForProteins() {
        int i = 0;
        proteinXmls = new JSONObject("{}");
        for (String protein : kibaProteins.keySet()) {
            i++;
            System.out.println("Starting for " + protein);
            String xml = requestProteinXmlByUniprotId(protein);
            proteinXmls.put(protein, xml);
            System.out.println("Finished " + i + "/" + kibaProteins.keySet().size());
        }
    }

    private static String requestProteinXmlByUniprotId(String uniprotId) {
        try {
            URL uniprotUrl = new URL("https://www.uniprot.org/uniprot/" + uniprotId + ".xml");
            URLConnection connection = uniprotUrl.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;
            String xml = "";

            while ((inputLine = in.readLine()) != null) {
                xml += inputLine;
            }
            in.close();

            return xml;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private static void saveProteinXmls() {
        System.out.println("Saving fetched data");
        try (FileWriter file = new FileWriter("/files/kiba/proteinXmls.txt")) {
            file.write(proteinXmls.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static JSONObject requestLigandJsonByChemblId(String chemblId) {
        try {
            URL pubchemUrl = new URL("https://pubchem.ncbi.nlm.nih.gov/compound/" + chemblId);
            URLConnection connection = pubchemUrl.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;
            String pubchemIdLink = "";

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("og:url")) {
                    pubchemIdLink = inputLine.split("\"")[3];
                }
            }
            in.close();

            String pubchemId = pubchemIdLink.split("/")[4];

            URL jsonUrl = new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/" + pubchemId + "/JSON/");
            URLConnection connection2 = jsonUrl.openConnection();
            BufferedReader in2 = new BufferedReader(
                    new InputStreamReader(
                            connection2.getInputStream()));
            String inputLine2;

            String jsonText = "";
            while ((inputLine2 = in2.readLine()) != null) {
                jsonText += inputLine2;
            }

            JSONObject json = new JSONObject(jsonText);

            //System.out.println(json);

            return json;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private static void importProteinDetails() {
        System.out.println("Reading the protein details database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/kiba/proteinJsons.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String proteinDB = in.nextLine();
        proteinJsons = new JSONObject(proteinDB);

        System.out.println("Imported " + proteinJsons.keySet().size() + " protein details from core dataset.");
    }

    private static void convertProteinXmlsToJson() {
        System.out.println("Converting fetched data");
        proteinJsons = new JSONObject("{}");
        int i = 0;
        for (String protein : proteinXmls.keySet()) {
            i++;
            proteinJsons.put(protein, XML.toJSONObject(proteinXmls.getString(protein)));
            System.out.println("Finished " + i + "/" + proteinXmls.keySet().size());
        }
    }

    private static void saveProteinJsons() {
        System.out.println("Saving converted data");
        try (FileWriter file = new FileWriter("files/kiba/proteinJsons.txt")) {
            file.write(proteinJsons.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Get affinity of a protein and a ligand by their ids.
    public static String getAffinity(String ligandId, String proteinId) {
        return kibaAffinities[Arrays.asList(kibaLigands.keySet().toArray()).indexOf(ligandId)]
                [Arrays.asList(kibaProteins.keySet().toArray()).indexOf(proteinId)];
    }

    // Get a protein's details.
    public static JSONObject getProtein(String proteinId) {
        return simplifyProtein(proteinJsons.getJSONObject(proteinId));
    }

    // Get a ligand's details.
    public static JSONObject getLigand(String ligandId) {
        JSONObject ligandDetail = ligandJsons.getJSONObject(ligandId);
        ligandDetail.put("id", ligandId);
        return simplifyLigand(ligandDetail);
    }

    public static JSONArray getInteractions(String id) {
        return kibaInteractions.getJSONArray(id);
    }

    private static JSONObject simplifyProtein(JSONObject proteinDetail) {
        try {
            JSONObject simpleProtein = new JSONObject();
            // Name of protein.
            simpleProtein.put("name", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getString("name"));
            // Id of protein.
            simpleProtein.put("id", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONArray("accession").get(0));
            // Gene name
            JSONObject geneObject = proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("gene");
            try {
                simpleProtein.put("gene", geneObject
                        .getJSONArray("name")
                        .getJSONObject(0)
                        .getString("content"));
            } catch (Exception e) {
                simpleProtein.put("gene", geneObject
                        .getJSONObject("name")
                        .getString("content"));
            }
            // Protein
            simpleProtein.put("protein", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("protein")
                    .getJSONObject("recommendedName")
                    .getString("fullName"));
            // Sequence
            simpleProtein.put("sequence", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("sequence")
                    .getString("content"));
            // Length
            simpleProtein.put("length", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("sequence")
                    .getInt("length"));
            // Organism
            simpleProtein.put("organism", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("organism")
                    .getJSONArray("name")
                    .getJSONObject(0)
                    .getString("content"));
            return simpleProtein;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private static JSONObject simplifyLigand(JSONObject ligandDetail) {
        try {
            JSONObject simpleLigand = new JSONObject();
            simpleLigand.put("id", ligandDetail.getString("id"));
            JSONArray sections = ligandDetail
                    .getJSONObject("Record")
                    .getJSONArray("Section");


            // Traverse sections.
            for (int i = 0; i < sections.length(); i++) {
                if (sections.getJSONObject(i).getString("TOCHeading").contains("Names and Identifiers")) {
                    JSONArray identifierSection = sections.getJSONObject(i).getJSONArray("Section");
                    for (int j = 0; j < identifierSection.length(); j++) {
                        // Name of ligand.
                        if (identifierSection.getJSONObject(j).getString("TOCHeading").contains("Record Title")) {
                            String name = identifierSection.getJSONObject(j).getJSONArray("Information").getJSONObject(0).getString("StringValue");
                            simpleLigand.put("name", name);
                        }

                        // IUPAC, InChI and Smiles
                        if (identifierSection.getJSONObject(j).getString("TOCHeading").contains("Computed Descriptors")) {
                            JSONArray chemicalNames = identifierSection.getJSONObject(j).getJSONArray("Section");
                            JSONObject chemicalNamesObject = new JSONObject();

                            for (int k = 0; k < chemicalNames.length(); k++) {
                                chemicalNamesObject.put(chemicalNames.getJSONObject(k).getString("TOCHeading"),
                                        chemicalNames.getJSONObject(k).getJSONArray("Information").getJSONObject(0).getString("StringValue"));
                            }
                            simpleLigand.put("chemicalNames", chemicalNamesObject);
                        }
                    }
                }
            }

            return simpleLigand;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}
