import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import ICTCLAS.I3S.AC.ICTCLAS50;

public class WebVisionAnalysis {

	String inputPath = null;
	String outputPath = null;
	String posnegPath = null;
	

	ICTCLAS50 testICTCLAS50;
	String code = "GB2312";
	
	HashMap<String, Integer> keywordInfo = null;

	ArrayList<String> sortedKeywords = null;

	HashSet<String> ChineseStopWords = null;
	HashSet<String> EnglishStopWords = null;
	
	HashSet<String> ChinesePositiveWords = null;
	HashSet<String> ChineseNegativeWords = null;
	int posCount = -1;
	int negCount = -1;
	int totalPosPara = 0;
	int totalNegPara = 0;
	int totalNeutralPara = 0;
	
	int fileCount = -1;

	public WebVisionAnalysis(String inputPath, String outputPath, String posnegPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.posnegPath = posnegPath;

		this.keywordInfo = new HashMap<String, Integer>();

		this.EnglishStopWords = new HashSet<String>();
		this.ChineseStopWords = new HashSet<String>();
		
		this.ChinesePositiveWords = new HashSet<String>();
		this.ChineseNegativeWords = new HashSet<String>();
		
		initStopWords();
		initSentiDict();
		this.fileCount = 0;
		
		testICTCLAS50 = new ICTCLAS50();
		// 分词所需库的路径
		String argu = ".";
		// 初始化
		code = "GB2312";
		// String code = "UTF8";
		try {
			if (testICTCLAS50.ICTCLAS_Init(argu.getBytes(code)) == false) {
				System.out.println("ICTCLAS Init Fail!");
				return;
			} else {
				//System.out.println("ICTCLAS Init Succeed!");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		
		//System.out.println(nativeBytes.length);
		
	}

	private void initStopWords() {
		String EnglishStopWordsPath = Constant.EnglishStopWordsPath;
		try {
			FileReader fr = new FileReader(EnglishStopWordsPath);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				EnglishStopWords.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String ChineseStopWordsPath = Constant.ChineseStopWordsPath;
		try {
			FileReader fr = new FileReader(ChineseStopWordsPath);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				EnglishStopWords.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private void initSentiDict() {
		String ChinesePositiveWordsPath = Constant.ChinesePositiveWords;
		try {
			FileReader fr = new FileReader(ChinesePositiveWordsPath);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				ChinesePositiveWords.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String ChineseNegativeWordsPath = Constant.ChineseNegativeWords;
		try {
			FileReader fr = new FileReader(ChineseNegativeWordsPath);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				ChineseNegativeWords.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		//String inputPath = "D:/data2/";
		String inputPath = Constant.InputPath;
		//String outputPath = "D:/output/";
		String outputPath = Constant.OutputPath;
		String posnegPath = Constant.PosNegPath;
		//while(true)
		{
			WebVisionAnalysis wva = new WebVisionAnalysis(inputPath, outputPath, posnegPath);
			wva.handle();
		}

	}

	private void handle() {
		System.out.println(" handling " + inputPath);
		// Process
		File f = new File(inputPath);
		readAllFiles(f);
		testICTCLAS50.ICTCLAS_Exit();
		
	}
	private void readAllFiles(File f)
	{	
		if(f.isDirectory())
		{
			File[] fs = f.listFiles();
			
			for(int i=0; i < fs.length; i++){
				//System.out.println( i + " " + fs.length + fs[i].getAbsolutePath());
				readAllFiles(fs[i]);
			}
			String rela = f.getPath().substring(Constant.InputPath.length(), f.getPath().length());
			if(rela.length() > 1)
			{
				folderStatic(rela.substring(1).replace('\\', '/') + '/');
				Keywords2File(rela.substring(1).replace('\\', '/') + '/');
			}
		}
		else
		{
			String rel = f.getAbsolutePath().substring(inputPath.length(), f.getAbsolutePath().length());
			handleFile(f, rel);
			fileCount++;
		}
	}

	

	private void handleFile(File file, String relativePath)
	{
		//System.out.println("\nhandling File\t" + relativePath + " " + keywordInfo.size());
		posCount = 0;
		negCount = 0;
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				// read a line from data source
				String line = br.readLine();
				//System.out.println("\t" + line);

				// analysis the line
				if (isEnglish(line)) {
					analysisEnglish(line);
				} else {
					analysisChinese(line);
				}
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//get total sentimental
		if(negCount < 5 )
		{
			if(posCount < 5)
				totalNegPara ++;
			else 
				totalPosPara ++;
		} 
		else
		{
			if(posCount / negCount > 1.7)
				totalPosPara ++;
			else if(posCount / negCount < 0.75)
				totalNegPara ++;
			else 
				totalNeutralPara ++;
		}
//		 sort and output the Keywords group3 needed
		//Keywords2File(relativePath);
		//PosNeg2File(relativePath);
		
		//System.out.println("readData finished");
	}


	

	private void analysisChinese(String line) {
		//System.out.println("analysisChinese");
		//		 select keyword by some rules and update keywordInfo
		
		try {
			byte nativeBytes[] = testICTCLAS50.ICTCLAS_ParagraphProcess(line
					.getBytes(code), 0, 1);
			String nativeStr = new String(nativeBytes, 0, nativeBytes.length,
					code);
			String[] words = nativeStr.split(" ");
			for (String s : words) {
				//System.out.println("\tword: " + s);
				String[] parts = s.split("/");
				if (parts.length > 1)
					if (!ChineseStopWords.contains(parts[0])) {
						if(ChinesePositiveWords.contains(parts[0]))
						{
							//System.out.println("Positive:" + parts[0]);
							posCount ++;
						}
						if(ChineseNegativeWords.contains(parts[0]))
						{
							//System.out.println("Negative:" + parts[0]);
							negCount++;
						}
						
						if(parts[1].contains("n") || parts[1].contains("a"))
							//if(parts[1].contains("a"))
						{
							//System.out.println("word:" + parts[0]);
							//System.out.println("pos:" + parts[1]);
							addWord(parts[0]);
							
				
						}
				}
			}
			//System.out.println("The result is ：" + nativeStr);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//System.out.println("analysisChinese finished");

		// ictclas suggestted

	}

	private void addWord(String keyword) {
		//System.out.println("addWord " + keyword);

		if (keywordInfo.containsKey(keyword)) {
			int i = keywordInfo.get(keyword);
			keywordInfo.put(keyword, i + 1);
		} else {
			keywordInfo.put(keyword, 1);
			//System.out.println("newWord " + keyword);
		}

	}

	private void analysisEnglish(String line) {
		// wordnet suggestted
		//System.out.println("analysisEnglish");
		String[] words = line.split(" |,|\\.|\\?|!|-|\\=");
		for (String w : words) {
			if (!EnglishStopWords.contains(w.toLowerCase())) {
				/*
				if (w.endsWith("s")) {
					w = w.substring(0, w.length() - 2);
				}
				*/
				//System.out.println("\t" + w);
				addWord(w);
			}
		}

	}
	private void folderStatic(String relativePath) {
		System.out.println(" folderStatic " + outputPath + "/" + relativePath + "pos-neg-neu.txt");
		try {
			File f = new File(outputPath + "/" + relativePath  + "pos-neg-neu.txt");
			f.mkdirs();
			if(f.isDirectory())
			{
				f.delete();
			}
			PrintWriter fos = new PrintWriter(new FileWriter(outputPath + "/" + relativePath + "pos-neg-neu.txt"));
			//fos.println("totalPosPara of " + relativePath + " " + totalPosPara);
			//fos.println("totalNegPara of " + relativePath + " " + totalNegPara);
			//fos.println("totalNeutralPara of " + relativePath + " " + totalNeutralPara);
			fos.println(totalPosPara);
			fos.println(totalNegPara);
			fos.println(totalNeutralPara);
			totalPosPara = 0;
			totalNegPara = 0;
			totalNeutralPara = 0;
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private void Keywords2File(String relativePath) {
		System.out.println(" Keywords2File " + outputPath + "/" + relativePath + "topWords.txt");
		try {
			File f = new File(outputPath + "/" + relativePath + "topWords.txt");
			f.mkdirs();
			if(f.isDirectory())
			{
				f.delete();
			}
			PrintWriter fos = new PrintWriter(new FileWriter(outputPath + "/" + relativePath + "topWords.txt"));
			Set<Entry<String, Integer>> entryset = keywordInfo.entrySet();
			sortedKeywords = new ArrayList<String>(entryset.size());
			for (Entry e : entryset) {
				sortedKeywords.add((String) e.getKey());			
			}
			
			Collections.sort(sortedKeywords, new KeywordComparator());
			for(String s:sortedKeywords)
			{
				//System.out.println(s + " " + keywordInfo.get(s));
				if(s.length() > 2 && !isEnglish(s))
					fos.println(s + " " + keywordInfo.get(s));				// data format requested
			}

			keywordInfo.clear();
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void PosNeg2File(String relativePath) {
		System.out.println(" PosNeg2File " + posnegPath + relativePath);
		try {
			File f = new File(posnegPath + relativePath);
			f.mkdirs();
			if(f.isDirectory())
				f.delete();
			PrintWriter fos = new PrintWriter(new FileWriter(posnegPath + relativePath));
			fos.println("Positive\n" + posCount);
			fos.println("Negtive\n" + negCount);

			
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	class KeywordComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			return keywordInfo.get(s2) - keywordInfo.get(s1);
		}

	}
	public static boolean isEnglish(String line) {
		// check if this line is pure English
		int n;
		n = line.length();
		// System.out.print(n);
		for (int i = 0; i < n; i++) {
			// System.out.print(line.charAt(i));
			int a=line.charAt(i);
			//System.out.print(a);
			if (line.charAt(i) >= 128 || line.charAt(i) <= 0)
				return false;
			else if (i == n) {
				return true;
			}
		}
		return true;
	}

}
