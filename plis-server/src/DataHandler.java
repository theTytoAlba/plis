import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DataHandler {
    static JSONObject kibaLigands, kibaProteins;
    static String kibaAffinities[][];

    /**
     * Import data from kiba dataset.
     */
    public static void prepareCoreDataset() {
        System.out.println("Importing core dataset.");
        importLigands();
        importProteins();
        importAffinities();
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
        // TODO: Implement
    }
    private static void fetchJsonDataForLigands() {
        // TODO: Implement
    }
    private static void saveLigandJsons() {
        // TODO: Implement
    }
    private static void importProteinXmlDetails() {
        // TODO: Implement
    }
    private static void fetchXmlDataForProteins() {
        // TODO: Implement
    }
    private static void saveProteinXmls() {
        // TODO: Implement
    }
    private static void importProteinDetails() {
        // TODO: Implement
    }
    private static void convertProteinXmlsToJson() {
        // TODO: Implement
    }
    private static void saveProteinJsons() {
        // TODO: Implement
    }
}
