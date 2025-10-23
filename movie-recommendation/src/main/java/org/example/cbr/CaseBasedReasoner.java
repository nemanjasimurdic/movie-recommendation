package org.example.cbr;

import org.example.connector.MoviesConnector;
import org.example.model.MovieDescription;
import org.example.similarity.SimilarityCalculator;

import java.util.List;

public class CaseBasedReasoner {

    private final MoviesConnector connector;
    private final SimilarityCalculator calculator;

    public CaseBasedReasoner() {
        this.connector = new MoviesConnector();
        this.calculator = new SimilarityCalculator();
    }

    public List<MovieDescription> loadMovies() {
        return connector.loadMovies();
    }

    public void findSimilarMovies(org.apache.jena.rdf.model.Model model, String titleInput) {
        List<MovieDescription> movies = connector.loadMovies();

        MovieDescription queryMovie = movies.stream()
                .filter(m -> m.getTitle().equalsIgnoreCase(titleInput))
                .findFirst()
                .orElse(null);

        if (queryMovie == null) {
            System.out.println("❌ Movie not found in ontology instances.");
            return;
        }

        System.out.println("\n🎬 Query Movie:");
        System.out.println(queryMovie);

        System.out.println("\n🔍 Most Similar Movies:");

        movies.stream()
                .filter(m -> !m.getTitle().equalsIgnoreCase(titleInput))
                .map(m -> new Object[]{m, calculator.calculateSimilarity(queryMovie, m)})
                .sorted((a, b) -> Double.compare((double) b[1], (double) a[1]))
                .limit(5)
                .forEach(entry -> {
                    MovieDescription m = (MovieDescription) entry[0];
                    double sim = (double) entry[1];
                    System.out.printf("\nSimilarity: %.3f\n%s\n", sim, m);
                });
    }
}
