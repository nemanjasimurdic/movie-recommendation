package org.example;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;
import org.apache.jena.query.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Load ontology (classes + instances)
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, "data/movies.owl", Lang.RDFXML);
        RDFDataMgr.read(model, "data/movies-instances.owl", Lang.RDFXML);

        Scanner sc = new Scanner(System.in);

        // 2. Ask user for filters
        System.out.print("Enter movie title(or press Enter for all): ");
        String titleInput = sc.nextLine().trim();
        System.out.print("Enter genre(or press Enter for all): ");
        String genreInput = sc.nextLine().trim();
        System.out.print("Enter director(or press Enter for all): ");
        String directorInput = sc.nextLine().trim();

        // 3. Build SPARQL query dynamically
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX mov: <http://www.example.org/movies#> ");
        queryBuilder.append("SELECT ?film ?title ?genre ?director WHERE { ");
        queryBuilder.append("?film a mov:Film ; mov:hasTitle ?title ; mov:hasGenre ?genre ; mov:hasDirector ?director . ");

        if (!titleInput.isEmpty()) {
            queryBuilder.append("FILTER(str(?title) = \"" + titleInput + "\") ");
        }
        if (!genreInput.isEmpty()) {
            queryBuilder.append("FILTER(str(?genre) = \"" + genreInput + "\") ");
        }
        if (!directorInput.isEmpty()) {
            queryBuilder.append("FILTER(str(?director) = \"" + directorInput + "\") ");
        }

        queryBuilder.append("} LIMIT 10");

        // 4. Execute query
        Query query = QueryFactory.create(queryBuilder.toString());
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                System.out.println("Film: " + sol.getLiteral("title").getString());
                System.out.println("Genre: " + sol.getLiteral("genre").getString());
                System.out.println("Director: " + sol.getLiteral("director").getString());
                System.out.println("----------------------------");
            }
        }

        sc.close();
    }
}