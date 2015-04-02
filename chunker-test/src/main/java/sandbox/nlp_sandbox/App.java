package sandbox.nlp_sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

public class App {
	public static void main(String[] args) throws Exception {
		String sentence = "Okawa was born on March 5, 1898, and died of heart failure just a few weeks after celebrating her birthday.";
		Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

		System.out.println("Running POS on sentence");
		POSTaggerME posTagger = new POSTaggerME(getPOSModel());
		String sent[] = tokenizer.tokenize(sentence);
		String pos[] = posTagger.tag(sent);

		System.out.println("Running Chunker Test");
		ChunkerME chunker = new ChunkerME(getChunkerModel());
		Span[] spans = chunker.chunkAsSpans(sent, pos);
		StringBuilder sb = new StringBuilder();
		List<String> sentList = Arrays.asList(sent);
		for (Span span : spans) {
			String substr = sentList.subList(span.getStart(), span.getEnd())
					.stream().collect(Collectors.joining(" "));
			if (span.getType().equals("VP") || span.getType().equals("ADVP")) {
				sb.append('[').append(span.getType()).append(' ').append(substr)
				.append("] ");
			}
		}
		System.out.println(sb.toString());
	}

	private static ChunkerModel getChunkerModel() {
		InputStream modelIn = null;
		ChunkerModel model = null;
		try {
			modelIn = App.class.getClassLoader().getResourceAsStream(
					"en-chunker.bin");
			model = new ChunkerModel(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return model;
	}

	private static POSModel getPOSModel() {
		InputStream modelIn = null;
		POSModel model = null;
		try {
			modelIn = App.class.getClassLoader().getResourceAsStream(
					"en-pos-maxent.bin");
			model = new POSModel(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return model;
	}
}
