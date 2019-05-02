package nlpclass;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class JStanfordAnnotator {

	StanfordCoreNLP pipeline;

	public JStanfordAnnotator() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("pos.model", "stanford-corenlp-models/pos-tagger/english-left3words-distsim.tagger");
		this.pipeline = new StanfordCoreNLP(props);
	}

	public static class JStanfordAnnotatedToken {
		String word;
		String lemma;

		public JStanfordAnnotatedToken(String word, String lemma) {
			this.word = word;
			this.lemma = lemma;
		}
	}

	public List<List<JStanfordAnnotatedToken>> annotate(String text) {

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		List<List<JStanfordAnnotatedToken>> sentences = new ArrayList<List<JStanfordAnnotatedToken>>();
		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			List<JStanfordAnnotatedToken> tokens = new ArrayList<JStanfordAnnotatedToken>();
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String lemma = token.get(LemmaAnnotation.class);
				tokens.add(new JStanfordAnnotatedToken(word, lemma));
			}
			sentences.add(tokens);
		}

		return sentences;
	}
}
