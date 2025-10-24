package org.example.model;

import ucm.gaia.jcolibri.cbrcore.Attribute;
import ucm.gaia.jcolibri.cbrcore.CaseComponent;

public class MovieDescription implements CaseComponent {
    private String title;
    private String genre;
    private String director;
    private String language;
    private String audienceType;
    private double directorRating;
    private double actorsRating;
    private double storyRating;
    private double visualRating;
    private double cultureRating;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getAudienceType() { return audienceType; }
    public void setAudienceType(String audienceType) { this.audienceType = audienceType; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public double getDirectorRating() { return directorRating; }
    public void setDirectorRating(double directorRating) { this.directorRating = directorRating; }

    public double getActorsRating() { return actorsRating; }
    public void setActorsRating(double actorsRating) { this.actorsRating = actorsRating; }

    public double getStoryRating() { return storyRating; }
    public void setStoryRating(double storyRating) { this.storyRating = storyRating; }

    public double getVisualRating() { return visualRating; }
    public void setVisualRating(double visualRating) { this.visualRating = visualRating; }

    public double getCultureRating() { return cultureRating; }
    public void setCultureRating(double cultureRating) { this.cultureRating = cultureRating; }

    @Override
    public Attribute getIdAttribute() { return null; } // ne koristim ID, pa moze ovako

    @Override
    public String toString() {
        return "Title: " + title +
                "\nGenre: " + genre +
                "\nDirector: " + director +
                "\nLanguage: " + language +
                "\nAudienceType: " + audienceType +
                "\nDirectorRating: " + directorRating +
                "\nActorsRating: " + actorsRating +
                "\nStoryRating: " + storyRating +
                "\nVisualRating: " + visualRating +
                "\nCultureRating: " + cultureRating;
    }
}
