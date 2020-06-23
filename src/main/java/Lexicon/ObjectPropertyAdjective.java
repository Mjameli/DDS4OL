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
public class ObjectPropertyAdjective {
    
    private String adjective;
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
        return onproperty.equals(((ObjectPropertyAdjective)obj).onproperty)
                &&hasvalue.equals(((ObjectPropertyAdjective)obj).hasvalue);  //but not adjective
    }
    
    public ObjectPropertyAdjective(String adj, String onproperty, String hasvalue, double score) {
        this.adjective = adj;
        this.onproperty = onproperty;
        this.hasvalue = hasvalue;
        this.score = score;
    }

    @Override
    public String toString() {
        return "ObjectPropertyAdjective{" + "adjective=" + adjective + ", onproperty=" + onproperty + ", hasvalue=" + hasvalue + ", score=" + score + '}';
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


    public String getAdjective() {
        return adjective;
    }

    public void setAdjective(String adjective) {
        this.adjective = adjective;
    }

}
