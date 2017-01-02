# nlp-coreference
<br>This system resolves noun phrases and find their co-references , the input provided is XMS tagged noun phrases.
<br>Algorithm:
<br>1) parsing of the text corpus to find the already tagged Noun Phrases and finding new Noun phrases
<br>2) Deterministic approach: Finding the co-referent noun phrases that are having high accuracy , Proper Nouns , abbreviation , pronoun resolution , head noun, string matching 
<br>3) Clustering approach : the rest of untagged noun phrases are tagged using the clustering algorithm based on the distance metric which is calculated using experiments


