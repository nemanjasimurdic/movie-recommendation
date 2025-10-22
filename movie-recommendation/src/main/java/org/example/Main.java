package org.example;

import java.io.InputStream;
import java.util.Scanner;

import net.sourceforge.jFuzzyLogic.FIS;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

public class Main {

    public static void main(String[] args) {
        // 1. Load ontology (classes + instances)
        Model model = ModelFactory.createDefaultModel();
        InputStream moviesStream = Main.class.getClassLoader().getResourceAsStream("movies.owl");
        InputStream instancesStream = Main.class.getClassLoader().getResourceAsStream("movies-instances.owl");
        RDFDataMgr.read(model, moviesStream, Lang.RDFXML);
        RDFDataMgr.read(model, instancesStream, Lang.RDFXML);

        // 2. Load Fuzzy system
        InputStream fisStream = Main.class.getClassLoader().getResourceAsStream("film.fcl");
        FIS fis = FIS.load(fisStream, true);
        if (fis == null) {
            System.err.println("Error loading fuzzy file.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Movie Application Menu ===");
            System.out.println("1. Movie suggestions");
            System.out.println("2. Evaluate movie quality");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    movieSuggestions(model, sc);
                    break;
                case "2":
                    evaluateMovieQuality(model, fis, sc);
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting program...");
                    break;
                default:
                    System.out.println("Invalid choice! Please enter 0, 1, or 2.");
            }
        }

        sc.close();
    }

    // === Movie Suggestions Function ===
    private static void movieSuggestions(Model model, Scanner sc) {
        System.out.print("Enter movie title (or press Enter for all): ");
        String titleInput = sc.nextLine().trim();
        System.out.print("Enter genre (or press Enter for all): ");
        String genreInput = sc.nextLine().trim();
        System.out.print("Enter director (or press Enter for all): ");
        String directorInput = sc.nextLine().trim();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX mov: <http://www.example.org/movies#> ");
        queryBuilder.append("SELECT ?film ?title ?genre ?director WHERE { ");
        queryBuilder.append("?film a mov:Film ; mov:hasTitle ?title ; mov:hasGenre ?genre ; mov:hasDirector ?director . ");

        if (!titleInput.isEmpty()) {
            queryBuilder.append("FILTER(CONTAINS(LCASE(str(?title)), \"" + titleInput.toLowerCase() + "\")) ");
        }
        if (!genreInput.isEmpty()) {
            queryBuilder.append("FILTER(CONTAINS(LCASE(str(?genre)), \"" + genreInput.toLowerCase() + "\")) ");
        }
        if (!directorInput.isEmpty()) {
            queryBuilder.append("FILTER(CONTAINS(LCASE(str(?director)), \"" + directorInput.toLowerCase() + "\")) ");
        }

        queryBuilder.append("} LIMIT 10");

        try {
            org.apache.jena.query.Query query = org.apache.jena.query.QueryFactory.create(queryBuilder.toString());
            try (org.apache.jena.query.QueryExecution qexec = org.apache.jena.query.QueryExecutionFactory.create(query, model)) {
                org.apache.jena.query.ResultSet results = qexec.execSelect();
                if (!results.hasNext()) {
                    System.out.println("No movies found with given filters.");
                    return;
                }
                while (results.hasNext()) {
                    org.apache.jena.query.QuerySolution sol = results.nextSolution();
                    System.out.println("\nFilm: " + sol.getLiteral("title").getString());
                    System.out.println("Genre: " + sol.getLiteral("genre").getString());
                    System.out.println("Director: " + sol.getLiteral("director").getString());
                    System.out.println("----------------------------");
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing SPARQL query: " + e.getMessage());
        }
    }

    // === Evaluate Movie Quality Function ===
    private static void evaluateMovieQuality(Model model, FIS fis, Scanner sc) {
        System.out.print("Enter movie title to evaluate: ");
        String titleInput = sc.nextLine().trim();
        Resource selectedMovie = null;

        for (Resource movie : model.listResourcesWithProperty(RDF.type, model.getResource("http://www.example.org/movies#Film")).toList()) {
            Statement titleStmt = movie.getProperty(model.getProperty("http://www.example.org/movies#hasTitle"));
            if (titleStmt != null && titleInput.equalsIgnoreCase(titleStmt.getString())) {
                selectedMovie = movie;
                break;
            }
        }

        if (selectedMovie == null) {
            System.out.println("Movie not found.");
            return;
        }

        double directorRating = getDoubleProperty(selectedMovie, model.getProperty("http://www.example.org/movies#hasDirectorRating"));
        double actorsRating = getDoubleProperty(selectedMovie, model.getProperty("http://www.example.org/movies#hasActorsRating"));
        double storyRating = getDoubleProperty(selectedMovie, model.getProperty("http://www.example.org/movies#hasStoryRating"));
        double visualRating = getDoubleProperty(selectedMovie, model.getProperty("http://www.example.org/movies#hasVisualRating"));
        double cultureRating = getDoubleProperty(selectedMovie, model.getProperty("http://www.example.org/movies#hasCultureRating"));

        fis.setVariable("DirectorRating", directorRating);
        fis.setVariable("ActorsRating", actorsRating);
        fis.setVariable("StoryRating", storyRating);
        fis.setVariable("VisualRating", visualRating);
        fis.setVariable("CultureRating", cultureRating);

        fis.evaluate();

        double quality = fis.getVariable("FilmQuality").getValue();
        String qualityLabel;

        if (quality < 50) {
            qualityLabel = "poor";
        } else if (quality < 80) {
            qualityLabel = "good";
        } else {
            qualityLabel = "excellent";
        }

        System.out.printf("Film quality score: %.2f (%s)%n", quality, qualityLabel);
    }

    // Helper method
    private static double getDoubleProperty(Resource res, Property prop) {
        Statement stmt = res.getProperty(prop);
        if (stmt != null && stmt.getObject().isLiteral()) {
            try {
                return Double.parseDouble(stmt.getString());
            } catch (NumberFormatException e) {
                System.err.println("Cannot parse value for property " + prop.getLocalName());
            }
        }
        return 0.0;
    }
}
