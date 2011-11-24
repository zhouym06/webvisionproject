import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;


public class WebVisionAnalysis {
	
	String inputPath = null;
	String outputPath = null; 
	HashMap<String, Integer> keywordInfo = null; 

	public WebVisionAnalysis(String inputPath, String outputPath)
	{
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.keywordInfo = new HashMap<String, Integer>();
	}
	
	
	public static void main(String[] args) {
		String inputPath = "";			//request for sample data
		String outputPath = "";			//request for sample data
		
		WebVisionAnalysis wva = new WebVisionAnalysis(inputPath, outputPath);
		wva.handle();
	}


	private void handle() {
		System.out.println(" handling " + inputPath);
		//Process 
		try {
			FileReader fr = new FileReader(inputPath);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				//read a line from data source
				String line = br.readLine();
				System.out.println("\t" + line);
				
				//analysis the line
				LinkedList<WordInfo> words  = new LinkedList<WordInfo>();
				if(isEnglish(line))
				{
					analysisEnglish(line, words);
				}
				else
				{
					analysisChinese(line, words);
				}
				
				//select keyword by some rules and update keywordInfo
				keywordInfoUpdate(words);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//	sort and output the Keywords group3 needed
		Keywords2File();
	}


	private void Keywords2File() {
		try{
			PrintWriter fos = new PrintWriter(new FileWriter(outputPath));
			//select top10 or toArray and sort or whatever

			fos.println("");
			//data format requested
			fos.flush();
		}catch (IOException e) {
            e.printStackTrace();
        }
		
	}


	private void keywordInfoUpdate(LinkedList<WordInfo> words) {
		// select noun and adjective or any other rule you can imagine
		
	}


	private void analysisChinese(String line, LinkedList<WordInfo> result) {
		//ictclas suggestted


		
	}


	private void analysisEnglish(String line, LinkedList<WordInfo> result) {
		// wordnet suggestted
		
	}


	private boolean isEnglish(String line) {
		// check if this line is pure English
		
		return false;
	}

}
