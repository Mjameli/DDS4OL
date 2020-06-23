/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexicon;

import Lexicon.GrammaticalType;

/**
 *
 * @author Admin
 */
public class RelationalNoun {

    public String noun;
    public String ref;
    public GrammaticalType propSubj;
    public GrammaticalType propObj;
    public double score = 1.0;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {  //it needed by the "contain" method.
        return ref.equals(((RelationalNoun) obj).ref) && propObj.equals(((RelationalNoun) obj).propObj)
                && propSubj.equals(((RelationalNoun) obj).propSubj);  //but not noun
    }

    @Override
    public String toString() {
        return "RelationalNoun{" + "noun=" + noun + ", ref=" + ref + ", propSubj=" + propSubj + ", propObj=" + propObj + ", score=" + score + '}';
    }

    public RelationalNoun(String noun, String ref, GrammaticalType propSubj, GrammaticalType propObj, double score) {
        this.noun = noun;
        this.ref = ref;
        this.propSubj = propSubj;
        this.propObj = propObj;
        this.score = score;
    }

    public void setNoun(String noun) {
        this.noun = noun;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setPropSubj(GrammaticalType propSubj) {
        this.propSubj = propSubj;
    }

    public void setPropObj(GrammaticalType propObj) {
        this.propObj = propObj;
    }

    public String getNoun() {
        return noun;
    }

    public String getRef() {
        return ref;
    }

    public GrammaticalType getPropSubj() {
        return propSubj;
    }

    public GrammaticalType getPropObj() {
        return propObj;
    }
}
