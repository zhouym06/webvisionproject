import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import ICTCLAS.I3S.AC.ICTCLAS50;

public class WebVisionAnalysis {

	String inputPath = null;

	String outputPath = null;

	HashMap<String, Integer> keywordInfo = null;

	ArrayList<String> sortedKeywords = null;

	HashSet<String> ChineseStopWords = null;

	HashSet<String> EnglishStopWords = null;

	public WebVisionAnalysis(String inputPath, String outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;

		this.keywordInfo = new HashMap<String, Integer>();

		this.EnglishStopWords = new HashSet<String>();
		this.ChineseStopWords = new HashSet<String>();
		initStopWords();
	}

	private void initStopWords() {
		String EnglishStopWordsPath = "D:/workspace2/WebVisionAnalysis/blacklist_en.txt";
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
		String ChineseStopWordsPath = "D:/workspace2/WebVisionAnalysis/blacklist_cn.txt";
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

	public static void main(String[] args) {
		String inputPath = "D:/data"; 			// request for sample data
		String outputPath = "D:/testoutput"; 		// request for sample data

		WebVisionAnalysis wva = new WebVisionAnalysis(inputPath, outputPath);
		wva.handle();

	}

	private void handle() {
		System.out.println(" handling " + inputPath);
		// Process
		File f = new File(inputPath);
		readAllFiles(f);
		// sort and output the Keywords group3 needed
		Keywords2File();
	}
	private void readAllFiles(File f)
	{	
		if(f.isDirectory())
		{
			File[] fs = f.listFiles();
			for(int i=0; i < fs.length; i++){
				System.out.println(fs[i].getAbsolutePath());
				readAllFiles(fs[i]);
			}
		}
		else
		{
			readData(f);
		}
	}
	private void readData(File file)
	{
		System.out.println("\t" + keywordInfo.size());
		 
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void Keywords2File() {
		System.out.println(" Keywords2File ");
		try {
			PrintWriter fos = new PrintWriter(new FileWriter(outputPath));
			// select top10 or toArray and sort or whatever
			Set<Entry<String, Integer>> entryset = keywordInfo.entrySet();
			sortedKeywords = new ArrayList<String>(entryset.size());
			for (Entry e : entryset) {
				sortedKeywords.add((String) e.getKey());			
			}
			
			Collections.sort(sortedKeywords, new KeywordComparator());
			for(String s:sortedKeywords)
			{
				//System.out.println(s + " " + keywordInfo.get(s));
				fos.println(s + " " + keywordInfo.get(s));				// data format requested
			}

			
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

	private void analysisChinese(String line) {
		//System.out.println("analysisChinese");
		//		 select keyword by some rules and update keywordInfo
		
		try {
			ICTCLAS50 testICTCLAS50 = new ICTCLAS50();
			// 分词所需库的路径
			String argu = ".";
			// 初始化
			String code = "GB2312";
			// String code = "UTF8";
			if (testICTCLAS50.ICTCLAS_Init(argu.getBytes(code)) == false) {
				System.out.println("ICTCLAS Init Fail!");
				return;
			} else {
				//System.out.println("ICTCLAS Init Succeed!");
			}
			byte nativeBytes[] = testICTCLAS50.ICTCLAS_ParagraphProcess(line
					.getBytes(code), 0, 1);
			//System.out.println(nativeBytes.length);
			String nativeStr = new String(nativeBytes, 0, nativeBytes.length,
					code);
			String[] words = nativeStr.split(" ");
			for (String s : words) {
				//System.out.println("\t" + s);
				String[] parts = s.split("/");
				// System.out.println("word:" + parts[0]);
				// System.out.println("pos:" + parts[1]);
				if (!ChineseStopWords.contains(parts[0])) {
					if (parts[1].contains("n") || parts[1].contains("a"))
						addWord(parts[0]);
				}
			}
			// System.out.println("The result is ：" + nativeStr);
			testICTCLAS50.ICTCLAS_Exit();
		} catch (Exception ex) {
		}

		// ictclas suggestted

	}

	private void addWord(String keyword) {
		//System.out.println("addWord " + keyword);

		if (keywordInfo.containsKey(keyword)) {
			int i = keywordInfo.get(keyword);
			keywordInfo.put(keyword, i + 1);
		} else {
			keywordInfo.put(keyword, 1);
		}

	}

	private void analysisEnglish(String line) {
		// wordnet suggestted
		String[] words = line.split(" ,.?!");
		for (String w : words) {
			if (!EnglishStopWords.contains(w)) {
				if (w.endsWith("s")) {
					w = w.substring(0, w.length() - 2);
				}
				addWord(w);
			}
		}

	}

	public static boolean isEnglish(String line) {
		// check if this line is pure English
		int n;
		n = line.length();
		// System.out.print(n);
		for (int i = 0; i < n; i++) {
			// System.out.print(line.charAt(i));
			// int a=line.charAt(i);
			// System.out.print(a);
			if (line.charAt(i) >= 128 || line.charAt(i) <= 0)
				return false;
			else if (i == n) {
				return true;
			}
		}
		return true;
	}

}
