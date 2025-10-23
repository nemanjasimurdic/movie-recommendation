package org.example.connector;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.example.model.MovieDescription;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MoviesConnector {

    private static final String MOVIES_NS = "http://www.example.org/movies#";

    public List<MovieDescription> loadMovies() {
        List<MovieDescription> movies = new ArrayList<>();

        Model model = ModelFactory.createDefaultModel();
        InputStream ontologyStream = getClass().getClassLoader().getResourceAsStream("movies.owl");
        InputStream instancesStream = getClass().getClassLoader().getResourceAsStream("movies-instances.owl");

        if (ontologyStream == null || instancesStream == null) {
            System.err.println("❌ Could not load ontology or instances files.");
            return movies;
        }

        RDFDataMgr.read(model, ontologyStream, Lang.RDFXML);
        RDFDataMgr.read(model, instancesStream, Lang.RDFXML);

        Property titleProp = model.getProperty(MOVIES_NS + "hasTitle");
        Property genreProp = model.getProperty(MOVIES_NS + "hasGenre");
        Property directorProp = model.getProperty(MOVIES_NS + "hasDirector");
        Property dirRatingProp = model.getProperty(MOVIES_NS + "hasDirectorRating");
        Property actRatingProp = model.getProperty(MOVIES_NS + "hasActorsRating");
        Property storyRatingProp = model.getProperty(MOVIES_NS + "hasStoryRating");
        Property visualRatingProp = model.getProperty(MOVIES_NS + "hasVisualRating");
        Property cultureRatingProp = model.getProperty(MOVIES_NS + "hasCultureRating");

        ResIterator it = model.listResourcesWithProperty(RDF.type, model.getResource(MOVIES_NS + "Film"));
        while (it.hasNext()) {
            Resource filmRes = it.next();
            MovieDescription movie = new MovieDescription();

            movie.setTitle(getLiteral(filmRes, titleProp));
            movie.setGenre(getLiteral(filmRes, genreProp));
            movie.setDirector(getLiteral(filmRes, directorProp));
            movie.setDirectorRating(getDouble(filmRes, dirRatingProp));
            movie.setActorsRating(getDouble(filmRes, actRatingProp));
            movie.setStoryRating(getDouble(filmRes, storyRatingProp));
            movie.setVisualRating(getDouble(filmRes, visualRatingProp));
            movie.setCultureRating(getDouble(filmRes, cultureRatingProp));

            movies.add(movie);
        }

        return movies;
    }

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
}
