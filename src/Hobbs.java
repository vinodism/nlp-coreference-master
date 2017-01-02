

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

class Hobbs {

    static final int MAXPREVSENTENCES = 4;
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TransformerException  {
        //      File dataFolder = new File("DataToPort");
        //      File[] documents;
        String grammar = "englishPCFG.ser.gz";
        String[] options = { "-maxLength", "100", "-retainTmpSubcategories" };
        //LexicalizedParser lp = new LexicalizedParser(grammar);
        //
        //      if (dataFolder.isDirectory()) {
        //          documents = dataFolder.listFiles();
        //      } else {
        //          documents = new File[] {dataFolder};
        //      }
        //      int currfile = 0;
        //      int totfiles = documents.length;
        //      for (File paper : documents) {
        //          currfile++;
        //          if (paper.getName().equals(".DS_Store")||paper.getName().equals(".xml")) {
        //              currfile--;
        //              totfiles--;
        //              continue;
        //          }
        //          System.out.println("Working on "+paper.getName()+" (file "+currfile+" out of "+totfiles+").");
        //
        //          DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); // This is for XML
        //          DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        //          Document doc = docBuilder.parse(paper.getAbsolutePath());
        //
        //          NodeList textlist = doc.getElementsByTagName("text");
        //          for(int i=0; i < textlist.getLength(); i++) {
        //              Node currentnode = textlist.item(i);
        //              String wholetext = textlist.item(i).getTextContent();
        String wholetext = "The house of King Arthur himself. You live in it all the day.";
        //System.out.println(wholetext);
        //Iterable<List<? extends HasWord>> sentences;
        System.out.println(wholetext);
        ArrayList<Tree> parseTrees = new ArrayList<Tree>();
        String asd = "";
        int j = 0;
        StringReader stringreader = new StringReader(wholetext);
        DocumentPreprocessor dp = new DocumentPreprocessor(stringreader);
        @SuppressWarnings("rawtypes")
        ArrayList<List> sentences = preprocess(dp);
        for (List sentence : sentences) {
            //parseTrees.add( lp.apply(sentence) ); // Parsing a new sentence and adding it to the parsed tree
            ArrayList<Tree> PronounsList = findPronouns(parseTrees.get(j)); // Locating all pronouns to resolve in the sentence
            Tree corefedTree;
            for (Tree pronounTree : PronounsList) {
                parseTrees.set(parseTrees.size()-1, HobbsResolve(pronounTree, parseTrees)); // Resolving the coref and modifying the tree for each pronoun
            }
            StringWriter strwr = new StringWriter();
            PrintWriter prwr = new PrintWriter(strwr);
            TreePrint tp = new TreePrint("penn");
            tp.printTree(parseTrees.get(j), prwr);
            prwr.flush();
            asd += strwr.toString();
            j++;
        }
        String armando = "";
        for (Tree sentence : parseTrees) {
            for (Tree leaf : Trees.leaves(sentence))
                armando += leaf + " ";
        }
        System.out.println(armando);
        System.out.println("All done.");
        //              currentnode.setTextContent(asd);
        //          }
        //          TransformerFactory transformerFactory = TransformerFactory.newInstance();
        //          Transformer transformer = transformerFactory.newTransformer();
        //          DOMSource source = new DOMSource(doc);
        //          StreamResult result = new StreamResult(paper);
        //          transformer.transform(source, result);
        //
        //          System.out.println("Done");
        //      }
    }

    public static Tree HobbsResolve(Tree pronoun, ArrayList<Tree> forest) {
        Tree wholetree = forest.get(forest.size()-1); // The last one is the one I am going to start from
        ArrayList<Tree> candidates = new ArrayList<Tree>();
        List<Tree> path = wholetree.pathNodeToNode(wholetree, pronoun);
        System.out.println(path);
        // Step 1
        Tree ancestor = pronoun.parent(wholetree); // This one locates the NP the pronoun is in, therefore we need one more "parenting" !
        // Step 2
        ancestor = ancestor.parent(wholetree);
        //System.out.println("LABEL: "+pronoun.label().value() + "\n\tVALUE: "+pronoun.firstChild());
        while ( !ancestor.label().value().equals("NP") && !ancestor.label().value().equals("S") )
            ancestor = ancestor.parent(wholetree);
        Tree X = ancestor;
        path = X.pathNodeToNode(wholetree, pronoun);
        System.out.println(path);
        // Step 3
        for (Tree relative : X.children()) {
            for (Tree candidate : relative) {
                if (candidate.contains(pronoun)) break; // I am looking to all the nodes to the LEFT (i.e. coming before) the path leading to X. contain <-> in the path
                //System.out.println("LABEL: "+relative.label().value() + "\n\tVALUE: "+relative.firstChild());
                if ( (candidate.parent(wholetree) != X) && (candidate.parent(wholetree).label().value().equals("NP") || candidate.parent(wholetree).label().value().equals("S")) )
                    if (candidate.label().value().equals("NP")) // "Propose as the antecedent any NP node that is encountered which has an NP or S node between it and X"
                        candidates.add(candidate);
            }
        }
        // Step 9 is a GOTO step 4, hence I will envelope steps 4 to 8 inside a while statement.
        while (true) { // It is NOT an infinite loop.
            // Step 4
            if (X.parent(wholetree) == wholetree) {
                for (int q=1 ; q < MAXPREVSENTENCES; ++q) {// I am looking for the prev sentence (hence we start with 1)
                    if (forest.size()-1 < q) break; // If I don't have it, break
                    Tree prevTree = forest.get(forest.size()-1-q); // go to previous tree
                    // Now we look for each S subtree, in order of recency (hence right-to-left, hence opposite order of that of .children() ).
                    ArrayList<Tree> backlist = new ArrayList<Tree>();
                    for (Tree child : prevTree.children()) {
                        for (Tree subtree : child) {
                            if (subtree.label().value().equals("S")) {
                                backlist.add(child);
                                break;
                            }
                        }
                    }
                    for (int i = backlist.size()-1 ; i >=0 ; --i) {
                        Tree Treetovisit = backlist.get(i);
                        for (Tree relative : Treetovisit.children()) {
                            for (Tree candidate : relative) {
                                if (candidate.contains(pronoun)) continue; // I am looking to all the nodes to the LEFT (i.e. coming before) the path leading to X. contain <-> in the path
                                //System.out.println("LABEL: "+relative.label().value() + "\n\tVALUE: "+relative.firstChild());
                                if (candidate.label().value().equals("NP")) { // "Propose as the antecedent any NP node that you find"
                                    if (!candidates.contains(candidate)) candidates.add(candidate);
                                }
                            }
                        }
                    }
                }
                break; // It will always come here eventually
            }
            // Step 5
            ancestor = X.parent(wholetree);
            //System.out.println("LABEL: "+pronoun.label().value() + "\n\tVALUE: "+pronoun.firstChild());
            while ( !ancestor.label().value().equals("NP") && !ancestor.label().value().equals("S") )
                ancestor = ancestor.parent(wholetree);
            X = ancestor;
            // Step 6
            if (X.label().value().equals("NP")) { // If X is an NP
                for (Tree child : X.children()) { // Find the nominal nodes that X directly dominates
                    if (child.label().value().equals("NN") || child.label().value().equals("NNS") || child.label().value().equals("NNP") || child.label().value().equals("NNPS") )
                        if (! child.contains(pronoun)) candidates.add(X); // If one of them is not in the path between X and the pronoun, add X to the antecedents
                }
            }
            // Step SETTE
            for (Tree relative : X.children()) {
                for (Tree candidate : relative) {
                    if (candidate.contains(pronoun)) continue; // I am looking to all the nodes to the LEFT (i.e. coming before) the path leading to X. contain <-> in the path
                    //System.out.println("LABEL: "+relative.label().value() + "\n\tVALUE: "+relative.firstChild());
                    if (candidate.label().value().equals("NP")) { // "Propose as the antecedent any NP node that you find"
                        boolean contains = false;
                        for (Tree oldercandidate : candidates) {
                            if (oldercandidate.contains(candidate)) {
                                contains=true;
                                break;
                            }
                        }
                        if (!contains) candidates.add(candidate);
                    }
                }
            }
            // Step 8
            if (X.label().value().equals("S")) {
                boolean right = false;
                // Now we want all branches to the RIGHT of the path pronoun -> X.
                for (Tree relative : X.children()) {
                    if (relative.contains(pronoun)) {
                        right = true;
                        continue;
                    }
                    if (!right) continue;
                    for (Tree child : relative) { // Go in but do not go below any NP or S node. Go below the rest
                        if (child.label().value().equals("NP")) {
                            candidates.add(child);
                            break; // not sure if this means avoid going below NP but continuing with the rest of non-NP children. Should be since its DFS.
                        }
                        if (child.label().value().equals("S")) break; // Same
                    }
                }
            }
        }

        // Step 9 is a GOTO, so we use a while.

        System.out.println(pronoun + ": CHAIN IS " + candidates.toString());
        ArrayList<Integer> scores = new ArrayList<Integer>();

        for (int j=0; j < candidates.size(); ++j) {
            Tree candidate = candidates.get(j);
            Tree parent = null;
            int parent_index = 0;
            for (Tree tree : forest) {
                if (tree.contains(candidate)) {
                    parent = tree;
                    break;
                }
                ++parent_index;
            }
            scores.add(0);
            if (parent_index == 0)
                scores.set(j, scores.get(j)+100); // If in the last sentence, +100 points
            scores.set(j, scores.get(j) + syntacticScore(candidate, parent));

            if (existentialEmphasis(candidate)) // Example: "There was a dog standing outside"
                scores.set(j, scores.get(j)+70);
            if (!adverbialEmphasis(candidate, parent))
                scores.set(j, scores.get(j)+50);
            if (headNounEmphasis(candidate, parent))
                scores.set(j, scores.get(j)+80);

            int sz = forest.size()-1;
//          System.out.println("pronoun in sentence " + sz + "(sz). Candidate in sentence "+parent_index+" (parent_index)");
            int dividend = 1;
            for (int u=0; u < sz - parent_index; ++u)
                dividend *= 2;
            //System.out.println("\t"+dividend);
            scores.set(j, scores.get(j)/dividend);
            System.out.println(candidate + " -> " + scores.get(j) );
        }
        int max = -1;
        int max_index = -1;
        for (int i=0; i < scores.size(); ++i) {
            if (scores.get(i) > max) {
                max_index = i;
                max = scores.get(i);
            }
        }
        Tree final_candidate = candidates.get(max_index);
        System.out.println("My decision for " + pronoun + " is: " + final_candidate);
        // Decide what candidate, with both gender resolution and Lappin and Leass ranking.

        Tree pronounparent = pronoun.parent(wholetree).parent(wholetree); // 1 parent gives me the NP of the pronoun
        int pos = 0;
        for (Tree sibling : pronounparent.children()) {
            System.out.println("Sibling "+pos+": " + sibling);
            if (sibling.contains(pronoun)) break;
            ++pos;
        }
        System.out.println("Before setchild: " + pronounparent);
        @SuppressWarnings("unused")
        Tree returnval = pronounparent.setChild(pos, final_candidate);
        System.out.println("After setchild: " + pronounparent);

        return wholetree; // wholetree is already modified, since it contains pronounparent
    }

    private static int syntacticScore(Tree candidate, Tree root) {
        // We will check whether the NP is inside an S (hence it would be a subject)
        // a VP (direct object)
        // a PP inside a VP (an indirect obj)
        Tree parent = candidate;
        while (! parent.label().value().equals("S")) {
            if (parent.label().value().equals("VP")) return 50; // direct obj
            if (parent.label().value().equals("PP")) {
                Tree grandparent = parent.parent(root);
                while (! grandparent.label().value().equals("S")) {
                    if (parent.label().value().equals("VP")) // indirect obj is a PP inside a VP
                        return 40;
                    parent = grandparent;
                    grandparent = grandparent.parent(root);
                }
            }
            parent = parent.parent(root);
        }
        return 80; // If nothing remains, it must be the subject
    }

    private static boolean existentialEmphasis(Tree candidate) {
        // We want to check whether our NP's Dets are "a" or "an".
        for (Tree child : candidate) {
            if (child.label().value().equals("DT")) {
                for (Tree leaf : child) {
                    if (leaf.value().equals("a")||leaf.value().equals("an")
                            ||leaf.value().equals("A")||leaf.value().equals("An") ) {
                        //System.out.println("Existential emphasis!");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean headNounEmphasis(Tree candidate, Tree root) {
        Tree parent = candidate.parent(root);
        while (! parent.label().value().equals("S")) { // If it is the head NP, it is not contained in another NP (that's exactly how the original algorithm does it)
            if (parent.label().value().equals("NP")) return false;
            parent = parent.parent(root);
        }
        return true;
    }

    private static boolean adverbialEmphasis(Tree candidate, Tree root) { // Like in "Inside the castle, King Arthur was invincible". "Castle" has the adv emph.
        Tree parent = candidate;
        while (! parent.label().value().equals("S")) {
            if (parent.label().value().equals("PP")) {
                for (Tree sibling : parent.siblings(root)) {
                    if ( (sibling.label().value().equals(","))) {
                        //System.out.println("adv Emph!");
                        return true;
                    }
                }
            }
            parent = parent.parent(root);
        }
        return false;
    }

    public static ArrayList<Tree> findPronouns(Tree t) {
        ArrayList<Tree> pronouns = new ArrayList<Tree>();
        if (t.label().value().equals("PRP") && !t.children()[0].label().value().equals("I") && !t.children()[0].label().value().equals("you") && !t.children()[0].label().value().equals("You")) {
            pronouns.add(t);
        }
        else
            for (Tree child : t.children())
                pronouns.addAll(findPronouns(child));
        return pronouns;
    }

    @SuppressWarnings("rawtypes")
    public static ArrayList<List> preprocess(DocumentPreprocessor strarray) {
        ArrayList<List> Result = new ArrayList<List>();
        for (List<HasWord> sentence : strarray) {
            if (!StringUtils.isAsciiPrintable(sentence.toString())) {
                continue; // Removing non ASCII printable sentences
            }
            //string = StringEscapeUtils.escapeJava(string);
            //string = string.replaceAll("([^A-Za-z0-9])", "\\s$1");
            int nonwords_chars = 0;
            int words_chars = 0;
            for (HasWord hasword : sentence ) {
                String next = hasword.toString();
                if ((next.length() > 30)||(next.matches("[^A-Za-z]"))) nonwords_chars += next.length(); // Words too long or non alphabetical will be junk
                else words_chars += next.length();
            }
            if ( (nonwords_chars / (nonwords_chars+words_chars)) > 0.5) // If more than 50% of the string is non-alphabetical, it is going to be junk
                continue;   // Working on a character-basis because some sentences may contain a single, very long word
            if (sentence.size() > 100) {
                System.out.println("\tString longer than 100 words!\t" + sentence.toString());
                continue;
            }
            Result.add(sentence);
        }
        return Result;
    }
}