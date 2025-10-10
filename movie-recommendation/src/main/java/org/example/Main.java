package org.example;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;
import org.apache.jena.query.*;

public class Main {
    public static void main(String[] args) {
        // 1. Ucitavanje ontologije (klase + instance)
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, "data/movies.owl", Lang.RDFXML);
        RDFDataMgr.read(model, "data/movies-instances.owl", Lang.RDFXML);

        // 2. SPARQL query za predlog filma po karakteristikama (primer: zanr)
        String queryString = ""
                + "PREFIX mov: <http://www.example.org/movies#> "
                + "SELECT ?film ?title ?genre ?director "
                + "WHERE { "
                + "  ?film a mov:Film ; "
                + "        mov:hasTitle ?title ; "
                + "        mov:hasGenre ?genre ; "
                + "        mov:hasDirector ?director . "
                + "  FILTER(?genre = \"Science Fiction\") "
                + "} LIMIT 5";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                System.out.println("Film: " + sol.getLiteral("title").getString());
                System.out.println("Zanr: " + sol.getLiteral("genre").getString());
                System.out.println("Reziser: " + sol.getLiteral("director").getString());
                System.out.println("----------------------------");
            }
        }
    }
}