package cnlp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;


public class Main {
	
	public static void annotate(StanfordCoreNLP pipeline, BufferedReader fin, BufferedWriter fout) throws IOException{
		String text = "";
		String line = "";
		while((line = fin.readLine()) != null){
			text += line;
		}
		
		System.out.println("Loaded Document");
		
		Annotation document = new Annotation(text);
		List<Annotation> annotations = new ArrayList<Annotation>();
		annotations.add(document);
		pipeline.annotate(annotations, 1);
						
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    int sentid = 1;
	    for(CoreMap sentence: sentences) {
	    	
	    	List<Word> words = new ArrayList<Word>();
	
	    	int ct = 0;
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
			    String word = token.get(TextAnnotation.class);
			    String pos = token.get(PartOfSpeechAnnotation.class);
			    String ne = token.get(NamedEntityTagAnnotation.class);   
			    String lemma = token.get(LemmaAnnotation.class);   
			    int begin = token.get(CharacterOffsetBeginAnnotation.class);
			    int end = token.get(CharacterOffsetEndAnnotation.class);
			    ct ++;
			    Word w = new Word();
			    w.idx = ct;
			    w.word = word;
			    w.pos = pos;
			    w.ne = ne;
			    w.lemma = lemma;
			    w.offset1 = begin;
			    w.offset2 = end;
			    words.add(w);
			}
			Tree tree = sentence.get(TreeAnnotation.class);
			SemanticGraph dependencies = sentence.get(CollapsedDependenciesAnnotation.class);
  
			for(SemanticGraphEdge edge : dependencies.edgeIterable()){
				int src = edge.getSource().index();
				int target = edge.getTarget().index();
				words.get(target-1).par_id = src;
				words.get(target-1).par_label = edge.getRelation().toString();
			}
						
			for(Word w : words){
				fout.write(w.idx + "\t" + w.word + "\t" + w.pos + "\t"
						+ w.ne + "\t" + w.lemma + "\t" + w.par_label + "\t" + w.par_id + "\t"
						+ "SENT_" + sentid + "\t" + w.offset1 + ":" + w.offset2 + "\n");
			}
			fout.write("\n");
			sentid = sentid + 1;
	    }
	}
	
	public static void main(String[] args) throws IOException {

	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse");
	    props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.beam.ser.gz");
	    props.setProperty("clean.sentenceendingtags", "p");
	    props.setProperty("pos.maxlen", "80");
	    props.setProperty("parse.maxlen", "80");
	    props.setProperty("threads", "1");
	    
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props, true);
	    
	    for(int i=0;i<args.length;i++){
	    	System.out.println("Processing " + args[i] + "...");
	    	BufferedReader fin = new BufferedReader(new FileReader(args[i]));
	    	BufferedWriter fout= new BufferedWriter(new FileWriter(args[i] + ".nlp"));
	    	annotate(pipeline, fin, fout);
	    	fin.close();
	    	fout.close();
	    }
	    
	}

}
