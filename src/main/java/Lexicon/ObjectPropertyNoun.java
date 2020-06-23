/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexicon;

/**
 *
 * @author Admin
 */
public class ObjectPropertyNoun {
    
    private String noun;
    private String onproperty;
    private String hasvalue;
    public double score = 1.0;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    @Override
    public boolean equals(Object obj) {  //it needed by the "contain" method of Lists.
        return onproperty.equals(((ObjectPropertyNoun)obj).onproperty)
                &&hasvalue.equals(((ObjectPropertyNoun)obj).hasvalue); //but not noun
    }
    
    
    public ObjectPropertyNoun(String noun, String onproperty, String hasvalue , double score) {
        this.noun = noun;
        this.onproperty = onproperty;
        this.hasvalue = hasvalue;
        this.score = score;
    }

    @Override
    public String toString() {
        return "ObjectPropertyNoun{" + "noun=" + noun + ", onproperty=" + onproperty + ", hasvalue=" + hasvalue + ", score=" + score + '}';
    }

    

    public String getHasvalue() {
        return hasvalue;
    }

    public void setHasvalue(String hasvalue) {
        this.hasvalue = hasvalue;
    }

    public String getOnproperty() {
        return onproperty;
    }

    public void setOnproperty(String onproperty) {
        this.onproperty = onproperty;
    }


    public String getNoun() {
        return noun;
    }

    public void setNoun(String noun) {
        this.noun = noun;
    }

}
