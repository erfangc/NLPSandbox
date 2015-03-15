package com.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class Driver {

	private static Logger logger = LoggerFactory.getLogger(Driver.class);

	/**
	 * Usage: java Driver "SENTENCE"
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String input = args[0];
		logger.info("Initializing Application");
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("spring/root-context.xml");

		@SuppressWarnings("unchecked")
		Map<String, NameFinderME> models = (Map<String, NameFinderME>) ctx.getBean("nerModels");
		Tokenizer tokenizer = ctx.getBean(Tokenizer.class);

		String[] sentence = tokenizer.tokenize(input);
		for (Entry<String, NameFinderME> model : models.entrySet()) {
			runModel(sentence, model);
		}
		ctx.close();
	}

	private static void runModel(String[] sentence, Entry<String, NameFinderME> model) {
		logger.info("Loaded model {} of type {}", model.getKey(), model.getValue().getClass());
		NameFinderME nerModel = model.getValue();
		for (Span span : nerModel.find(sentence)) {
			String entity = extractEntityFromSpan(sentence, span);
			logger.info("Identified {}: {}", span.getType(), entity);
		}
		nerModel.clearAdaptiveData();
	}

	private static String extractEntityFromSpan(String[] sentence, Span span) {
		int start = span.getStart(), end = span.getEnd();
		StringBuilder bdr = new StringBuilder(sentence[start]);
		for (int i = start + 1; i < end; i++) {
			bdr.append(' ').append(sentence[i]);
		}
		String entity = bdr.toString();
		return entity;
	}

	@Bean
	public Tokenizer nlpTokenizer() throws InvalidFormatException, IOException {
		TokenizerModel model = new TokenizerModel(new ClassPathResource("models/en-token.bin").getFile());
		return new TokenizerME(model);
	}

	@Bean
	public NameFinderME dateNERModel() {
		return buildNameFinderME("models/en-ner-date.bin");
	}

	@Bean
	public NameFinderME locationNERModel() {
		return buildNameFinderME("models/en-ner-location.bin");
	}

	@Bean
	@Autowired
	public Map<String, NameFinderME> nerModels(NameFinderME personNERModel, NameFinderME organizationNERModel,
			NameFinderME locationNERModel, NameFinderME dateNERModel) {
		Map<String, NameFinderME> result = new HashMap<String, NameFinderME>();
		result.put("date", dateNERModel);
		result.put("location", locationNERModel);
		result.put("organization", organizationNERModel);
		result.put("person", personNERModel);
		return result;
	}

	@Bean
	public NameFinderME organizationNERModel() {
		return buildNameFinderME("models/en-ner-organization.bin");
	}

	@Bean
	public NameFinderME personNERModel() {
		return buildNameFinderME("models/en-ner-person.bin");
	}

	private NameFinderME buildNameFinderME(String modelFile) {
		try {
			InputStream modelIn = new FileInputStream(new ClassPathResource(modelFile).getFile());
			TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
			return new NameFinderME(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
