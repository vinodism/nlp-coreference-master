

import java.util.*;

public class FeaturesObj {


    private List<String>        ruleOne_allWords;
    private String              ruleTwo_PronounType;
    private String              ruleThree_article;
    private String              ruleFour_appositive;
    private ENUM_NUMBER_TYPE    ruleFive_number;
    private boolean             ruleSix_properName;
    private String              ruleSeven_headNoun;
    private String              ruleEight_gender;
    private ENUM_ANIM_TYPE      ruleNine_animacy;
    private String              ruleTen_semanticClass;
    private int                 ruleEleven_position;


    //constructor all the feature and set
    //the feature to the
    public FeaturesObj(NPObj npObj){

        //use the rules to find all the features
        Rules rules = Rules.getInstance();

        //rule 1:
        //ALL WORDS
        this.ruleOne_allWords = rules.ruleOneGetALlWords(npObj);

        //rule 2:
        //PRONOUN TYPE
        this.ruleTwo_PronounType = rules.ruleTwoGetPronounType(this.ruleOne_allWords);

        //rule 3:
        //ARTICLE
        //send all the words to match the article
        this.ruleThree_article = rules.ruleThreeGetArticle(this.ruleOne_allWords);

        //rule 4:
        //APPOSITIVE
        //send all the words to match the article
        this.ruleFour_appositive = rules.ruleFourGetAppositive(npObj);

        //rule 5:
        //SINGULAR or PLURAL
        this.ruleFive_number = rules.ruleFiveGetNumber(this.ruleOne_allWords);

        //rule 6:
        //PROPER NAME
        this.ruleSix_properName = rules.ruleSixGetProperNames(npObj);

        //rule 7:
        //SEMANTIC CLASS
        this.ruleSeven_headNoun =  rules.ruleSevenHeadNoun(npObj.getStrNP());

        //rule 8:
        //GENDER
        this.ruleEight_gender = rules.ruleEightGender(this.ruleOne_allWords);

        //rule 9:
        //ANIMACY
        this.ruleNine_animacy = rules.ruleNineAnimacy(this.ruleSeven_headNoun);

        //rule 10:
        //SEMANTIC CLASS
        this.ruleTen_semanticClass =  rules.ruleTenSemanticClass(this.ruleSeven_headNoun,
                                        this.ruleSix_properName, this.ruleEight_gender, this.ruleNine_animacy);

        //rule 11:
        //POSITION
        this.ruleEleven_position = npObj.pos;

    }

    //getters


    public List<String> getRuleOne_allWords() {
        return ruleOne_allWords;
    }

    public String getRuleTwo_PronounType() {
        return ruleTwo_PronounType;
    }

    public String getRuleThree_article() {
        return ruleThree_article.trim().toLowerCase();
    }

    public String getRuleFour_appositive() {
        return ruleFour_appositive.trim().toLowerCase();
    }

    public ENUM_NUMBER_TYPE getRuleFive_number() {
        return ruleFive_number;
    }

    public boolean isRuleSix_properName() {
        return ruleSix_properName;
    }

    public String getRuleSeven_headNoun() {
        return ruleSeven_headNoun.trim().toLowerCase();
    }

    public String getRuleEight_gender() {
        return ruleEight_gender;
    }

    public ENUM_ANIM_TYPE getRuleNine_animacy() {
        return ruleNine_animacy;
    }

    public String getRuleTen_semanticClass() {
        return ruleTen_semanticClass;
    }

    public int getRuleEleven_position() {
        return ruleEleven_position;
    }
}
