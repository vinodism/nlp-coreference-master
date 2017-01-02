# nlp-coreference
This system resolves noun phrases and find their co-references , the input provided is XMS tagged noun phrases.
Algorithm:
1) parsing of the text corpus to find the already tagged Noun Phrases and finding new Noun phrases
2) Deterministic approach: Finding the co-referent noun phrases that are having high accuracy , Proper Nouns , abbreviation , pronoun resolution , head noun, string matching 
3) Clustering approach : the rest of untagged noun phrases are tagged using the clustering algorithm based on the distance metric which is calculated using experiments


