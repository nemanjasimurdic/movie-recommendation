package org.example.similarity;

import org.example.model.MovieDescription;

public class SimilarityCalculator {

    public double calculateSimilarity(MovieDescription m1, MovieDescription m2) {
        double genreSim = calculateGenreSimilarity(m1.getGenre(), m2.getGenre());
        double directorSim = calculateDirectorSimilarity(m1.getDirector(), m2.getDirector());

        double ratingsSim = (
                numericSimilarity(m1.getDirectorRating(), m2.getDirectorRating()) +
                        numericSimilarity(m1.getActorsRating(), m2.getActorsRating()) +
                        numericSimilarity(m1.getStoryRating(), m2.getStoryRating()) +
                        numericSimilarity(m1.getVisualRating(), m2.getVisualRating()) +
                        numericSimilarity(m1.getCultureRating(), m2.getCultureRating())
        ) / 5.0;

        // Kombinuj sve komponente u ukupnu sličnost
        return (0.3 * genreSim) + (0.2 * directorSim) + (0.5 * ratingsSim);
    }

    private double calculateGenreSimilarity(String g1, String g2) {
        return g1.equalsIgnoreCase(g2) ? 1.0 : 0.0;
    }

    private double calculateDirectorSimilarity(String d1, String d2) {
        return d1.equalsIgnoreCase(d2) ? 1.0 : 0.0;
    }

    private double numericSimilarity(double v1, double v2) {
        double diff = Math.abs(v1 - v2);
        return 1 - (diff / 10.0); // pretpostavka: skala 0–10
    }
}
