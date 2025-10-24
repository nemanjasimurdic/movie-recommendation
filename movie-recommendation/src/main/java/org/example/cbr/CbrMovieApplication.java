package org.example.cbr;

import org.example.model.MovieDescription;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import ucm.gaia.jcolibri.cbraplications.StandardCBRApplication;
import ucm.gaia.jcolibri.cbrcore.*;
import ucm.gaia.jcolibri.method.retrieve.RetrievalResult;
import ucm.gaia.jcolibri.method.retrieve.NNretrieval.NNConfig;
import ucm.gaia.jcolibri.method.retrieve.NNretrieval.similarity.global.Average;
import ucm.gaia.jcolibri.method.retrieve.NNretrieval.similarity.local.Interval;

import java.util.ArrayList;
import java.util.List;

public class CbrMovieApplication implements StandardCBRApplication {

    private final List<CBRCase> caseBase;

    public CbrMovieApplication() {
        caseBase = new ArrayList<>();
        configureSimilarity();
    }

    /** Similarity configuration */
    private void configureSimilarity() {
        NNConfig simConfig = new NNConfig();
        simConfig.setDescriptionSimFunction(new Average());

        simConfig.addMapping(new Attribute("directorRating", MovieDescription.class), new Interval(1));
        simConfig.addMapping(new Attribute("actorsRating", MovieDescription.class), new Interval(1));
        simConfig.addMapping(new Attribute("storyRating", MovieDescription.class), new Interval(1));
        simConfig.addMapping(new Attribute("visualRating", MovieDescription.class), new Interval(1));
        simConfig.addMapping(new Attribute("cultureRating", MovieDescription.class), new Interval(1));
    }

    /** Loading cases from OWL */
    public void loadCasesFromOntology(Model model) {
        Property titleProp = model.getProperty("http://www.example.org/movies#hasTitle");
        Property genreProp = model.getProperty("http://www.example.org/movies#hasGenre");
        Property directorProp = model.getProperty("http://www.example.org/movies#hasDirector");
        Property languageProp = model.getProperty("http://www.example.org/movies#hasLanguage");
        Property audienceTypeProp = model.getProperty("http://www.example.org/movies#hasAudienceType");
        Property dirRatingProp = model.getProperty("http://www.example.org/movies#hasDirectorRating");
        Property actRatingProp = model.getProperty("http://www.example.org/movies#hasActorsRating");
        Property storyRatingProp = model.getProperty("http://www.example.org/movies#hasStoryRating");
        Property visualRatingProp = model.getProperty("http://www.example.org/movies#hasVisualRating");
        Property cultureRatingProp = model.getProperty("http://www.example.org/movies#hasCultureRating");

        ResIterator it = model.listResourcesWithProperty(RDF.type, model.getResource("http://www.example.org/movies#Film"));
        while (it.hasNext()) {
            Resource filmRes = it.next();

            MovieDescription movie = new MovieDescription();
            movie.setTitle(getLiteral(filmRes, titleProp));
            movie.setGenre(getLiteral(filmRes, genreProp));
            movie.setDirector(getLiteral(filmRes, directorProp));
            movie.setLanguage(getLiteral(filmRes, languageProp));
            movie.setAudienceType(getLiteral(filmRes, audienceTypeProp));
            movie.setDirectorRating(getDouble(filmRes, dirRatingProp));
            movie.setActorsRating(getDouble(filmRes, actRatingProp));
            movie.setStoryRating(getDouble(filmRes, storyRatingProp));
            movie.setVisualRating(getDouble(filmRes, visualRatingProp));
            movie.setCultureRating(getDouble(filmRes, cultureRatingProp));

            CBRCase c = new CBRCase();
            c.setDescription(movie);
            caseBase.add(c);
        }
    }

    /** Finding similar movies */
    public void findSimilarMoviesByTitle(String title) {
        MovieDescription queryMovie = null;

        // Find the query movie in the caseBase
        for (CBRCase c : caseBase) {
            MovieDescription m = (MovieDescription) c.getDescription();
            if (m.getTitle().equalsIgnoreCase(title)) {
                queryMovie = m;
                break;
            }
        }

        if (queryMovie == null) {
            System.out.println("Movie not found in case base.");
            return;
        }

        List<RetrievalResult> results = new ArrayList<>();

        for (CBRCase c : caseBase) {
            MovieDescription m = (MovieDescription) c.getDescription();
            if (m.getTitle().equalsIgnoreCase(queryMovie.getTitle())) {
                continue; // Skip the movie itself
            }

            RetrievalResult r = getRetrievalResult(c, m, queryMovie);
            results.add(r);
        }

        // Sort by similarity in descending order
        results.sort((r1, r2) -> Double.compare(r2.getEval(), r1.getEval()));

        // Top 5
        int k = Math.min(5, results.size());
        System.out.println("\n--- Most Similar Movies ---");
        for (int i = 0; i < k; i++) {
            RetrievalResult r = results.get(i);
            MovieDescription m = (MovieDescription) r.get_case().getDescription();
            System.out.printf("%d) Title: %s\n   Genre: %s\n   Director: %s\n   Language: %s\n   Audience: %s\n   Similarity: %.3f\n----------------------------\n",
                    i+1, m.getTitle(), m.getGenre(), m.getDirector(), m.getLanguage(), m.getAudienceType(), r.getEval());
        }
    }

    private static RetrievalResult getRetrievalResult(CBRCase c, MovieDescription m, MovieDescription queryMovie) {
        double genreSim = m.getGenre().equalsIgnoreCase(queryMovie.getGenre()) ? 1.0 : 0.0;
        double directorSim = m.getDirector().equalsIgnoreCase(queryMovie.getDirector()) ? 1.0 : 0.0;
        double languageSim = m.getLanguage().equalsIgnoreCase(queryMovie.getLanguage()) ? 1.0 : 0.0;
        double audienceSim = m.getAudienceType().equalsIgnoreCase(queryMovie.getAudienceType()) ? 1.0 : 0.0;

        // Attribute weights
        double combinedSim = (0.4 * genreSim) + (0.3 * directorSim) + (0.2 * languageSim) + (0.1 * audienceSim);

        return new RetrievalResult(c, combinedSim);
    }


    /** Helper methods */
    private String getLiteral(Resource res, Property prop) {
        Statement stmt = res.getProperty(prop);
        return stmt != null ? stmt.getString() : "";
    }

    private double getDouble(Resource res, Property prop) {
        Statement stmt = res.getProperty(prop);
        if (stmt != null && stmt.getObject().isLiteral()) {
            try {
                return Double.parseDouble(stmt.getString());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    @Override
    public void configure() {}
    @Override
    public void cycle(CBRQuery arg0) {}
    @Override
    public void postCycle() {}
    @Override
    public CBRCaseBase preCycle() { return null; }
}
