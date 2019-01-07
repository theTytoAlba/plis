import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class DataHandler {
    private static JSONObject kibaLigands, kibaProteins, ligandJsons, proteinXmls, proteinJsons, kibaInteractions,
            extractionProteins, extractionLigands, extractionAffinities;
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

            return requestLigandJsonByPubchemId(pubchemId);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private static JSONObject requestLigandJsonByPubchemId(String pubchemId) {
        try {
            URL jsonUrl = new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/" + pubchemId + "/JSON/");
            URLConnection connection = jsonUrl.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;

            String jsonText = "";
            while ((inputLine = in.readLine()) != null) {
                jsonText += inputLine;
            }

            JSONObject json = new JSONObject(jsonText);
            return json;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private static String extractChemblIdFromLigandJson(JSONObject ligandJson) {
        // The TOCHeading order: Names and Identifiers > Synonyms > Depositor-Supplied Synonyms.
        JSONArray sections = ligandJson.getJSONObject("Record").getJSONArray("Section");
        for (int i = 0; i < sections.length(); i++) {
            JSONObject sectionContent = sections.getJSONObject(i);
            if (sectionContent.getString("TOCHeading").equals("Names and Identifiers")) {
                JSONArray identifierSections = sectionContent.getJSONArray("Section");
                for (int j = 0; j < identifierSections.length(); j++) {
                    JSONObject identifierSectionContent = identifierSections.getJSONObject(j);
                    if (identifierSectionContent.getString("TOCHeading").equals("Synonyms")) {
                        JSONArray synonymSections = identifierSectionContent.getJSONArray("Section");
                        for (int k = 0; k < synonymSections.length(); k++) {
                            JSONObject synonymSectionContent = synonymSections.getJSONObject(k);
                            if (synonymSectionContent.getString("TOCHeading").equals("Depositor-Supplied Synonyms")) {
                                JSONObject synonymInfo = synonymSectionContent.getJSONArray("Information").getJSONObject(0);
                                JSONArray synonyms = synonymInfo.getJSONArray("StringValueList");
                                for (int l = 0; l < synonyms.length(); l++) {
                                    if (synonyms.getString(l).contains("CHEMBL")) {
                                        return synonyms.getString(l);
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

    /** Process extraction data. */
    public static void prepareExtractionDataset() {
        extractionProteins = new JSONObject();
        extractionLigands = new JSONObject();
        extractionAffinities = new JSONObject();
        // Read the paper links from Atakan's file.
        System.out.println("Reading the extraction database.");
        Scanner in = null;
        try {
            in = new Scanner(new File("files/other/extractions.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Process every line.
        int i = 0;
        while (in.hasNextLine()) {
            // <protein id> <ligand id> <url>
            String[] tokens = in.nextLine().split("\t");

            // Get protein info if needed.
            if (!extractionProteins.has(tokens[0])) {
                JSONObject proteinJSON = XML.toJSONObject(requestProteinXmlByUniprotId(tokens[0]));
                extractionProteins.put(tokens[0], proteinJSON);
            }

            // Get ligand info.
            JSONObject ligandJSON = requestLigandJsonByPubchemId(tokens[1]);
            String ligandChemblId = extractChemblIdFromLigandJson(ligandJSON);
            if (ligandChemblId == null) {
                System.out.println("FAILED FOR " + tokens[1]);
            }
            extractionLigands.put(ligandChemblId, ligandJSON);

            // Create a random affinity
            double affinity = Math.random() * 10 + 10;
            // Save affinity for protein.
            if (!extractionAffinities.has(tokens[0])) {
                extractionAffinities.put(tokens[0], new JSONObject());
            }
            JSONObject proteinAffinities = extractionAffinities.getJSONObject(tokens[0]);
            proteinAffinities.put(ligandChemblId, new JSONObject("{value:" + affinity + ", source:" + tokens[3] + "}"));
            extractionAffinities.put(tokens[0], proteinAffinities);
            // Save affinity for ligand
            if (!extractionAffinities.has(ligandChemblId)) {
                extractionAffinities.put(ligandChemblId, new JSONObject());
            }
            JSONObject ligandAffinities = extractionAffinities.getJSONObject(ligandChemblId);
            ligandAffinities.put(tokens[0], "{value:" + affinity + ", source:" + tokens[3] + "}");
            extractionAffinities.put(ligandChemblId, ligandAffinities);

            System.out.println(i++);
        }
        saveExtractionJSONS();
    }

    private static void saveExtractionJSONS() {
        System.out.println("Saving extraction dataset info");
        try (FileWriter file = new FileWriter("files/kiba/extractionProteins.txt")) {
            file.write(extractionProteins.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println(e);
        }

        try (FileWriter file = new FileWriter("files/kiba/extractionLigands.txt")) {
            file.write(extractionLigands.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println(e);
        }

        try (FileWriter file = new FileWriter("files/kiba/extractionAffinities.txt")) {
            file.write(extractionAffinities.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Get affinity of a protein and a ligand by their ids.
    public static String getKibaAffinity(String ligandId, String proteinId) {
        return kibaAffinities[Arrays.asList(kibaLigands.keySet().toArray()).indexOf(ligandId)]
                [Arrays.asList(kibaProteins.keySet().toArray()).indexOf(proteinId)];
    }

    // Get a protein's details.
    public static JSONObject getProtein(String proteinId) {
        if (proteinJsons.has(proteinId)) {
            return simplifyProtein(proteinJsons.getJSONObject(proteinId));
        } else {
            return null;
        }
    }

    // Get a ligand's details.
    public static JSONObject getLigand(String ligandId) {
        if (ligandJsons.has(ligandId)) {
            JSONObject ligandDetail = ligandJsons.getJSONObject(ligandId);
            ligandDetail.put("id", ligandId);
            return simplifyLigand(ligandDetail);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of ids.
     */
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
            JSONObject entryObject = proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry");
            try {
                simpleProtein.put("id", entryObject.getJSONArray("accession").get(0));
            } catch (Exception e) {
                simpleProtein.put("id", entryObject.getString("accession"));
            }

            JSONObject simpleProteinDetails = new JSONObject();
            // Gene name
            JSONObject geneObject = proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("gene");
            try {
                simpleProteinDetails.put("Gene", geneObject
                        .getJSONArray("name")
                        .getJSONObject(0)
                        .getString("content"));
            } catch (Exception e) {
                simpleProteinDetails.put("Gene", geneObject
                        .getJSONObject("name")
                        .getString("content"));
            }
            // Protein
            simpleProteinDetails.put("Protein", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("protein")
                    .getJSONObject("recommendedName")
                    .getString("fullName"));
            // Sequence
            simpleProteinDetails.put("Sequence", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("sequence")
                    .getString("content"));
            // Length
            simpleProteinDetails.put("Length", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("sequence")
                    .getInt("length"));
            // Organism
            simpleProteinDetails.put("Organism", proteinDetail
                    .getJSONObject("uniprot")
                    .getJSONObject("entry")
                    .getJSONObject("organism")
                    .getJSONArray("name")
                    .getJSONObject(0)
                    .getString("content"));
            simpleProtein.put("details", simpleProteinDetails);
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
