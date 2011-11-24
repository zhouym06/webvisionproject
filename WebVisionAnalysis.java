import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

import ICTCLAS.I3S.AC.ICTCLAS50;

public class WebVisionAnalysis {

	String inputPath = null;

	String outputPath = null;

	HashMap<String, Integer> keywordInfo = null;

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
		String inputPath = "D:/testdata"; // request for sample data
		String outputPath = "D:/testoutput"; // request for sample data

		WebVisionAnalysis wva = new WebVisionAnalysis(inputPath, outputPath);
		wva.handle();
		
	}

	private void handle() {
		System.out.println(" handling " + inputPath);
		// Process
		try {
			FileReader fr = new FileReader(inputPath);
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				// read a line from data source
				String line = br.readLine();
				
				System.out.println("\t" + line);

				// analysis the line
				if (isEnglish(line)) {
					analysisEnglish(line);
				} else {
					analysisChinese(line);
				}

				// select keyword by some rules and update keywordInfo
				// keywordInfoUpdate(words);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// sort and output the Keywords group3 needed
		Keywords2File();
	}

	private void Keywords2File() {
		System.out.println(" Keywords2File ");
		try {
			PrintWriter fos = new PrintWriter(new FileWriter(outputPath));
			// select top10 or toArray and sort or whatever
			Set<Entry<String, Integer>> entryset = keywordInfo.entrySet();
			
			for (Entry e : entryset) {
				System.out.println(e.getKey() + " " + e.getValue());
				fos.println(e.getKey() + " " + e.getValue());
			}
			

			
			// data format requested
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void analysisChinese(String line) {
		System.out.println("analysisChinese");
		try {
			ICTCLAS50 testICTCLAS50 = new ICTCLAS50();
			// �ִ�������·��
			String argu = ".";
			// ��ʼ��
			if (testICTCLAS50.ICTCLAS_Init(argu.getBytes("GB2312")) == false) {
				System.out.println("Init Fail!");
				return;
			} else {
				System.out.println("Init Succeed!");
			}
			//String sInput = "������س�Ů���������ܹ���ϲ����̫cool�ˡ������б����ڷǵ�֮��";
			byte nativeBytes[] = testICTCLAS50.ICTCLAS_ParagraphProcess(line
					.getBytes("GB2312"), 0, 1);
			System.out.println(nativeBytes.length);
			String nativeStr = new String(nativeBytes, 0, nativeBytes.length,
					"GB2312");
			String[] words = nativeStr.split(" ");
			for (String s : words) {
				 System.out.println("\t" + s);
				String[] parts = s.split("/");
				// System.out.println("word:" + parts[0]);
				// System.out.println("pos:" + parts[1]);
				if (!ChineseStopWords.contains(parts[0])) {
					if (parts[1].contains("n") || parts[1].contains("a") || parts[1].contains("x"))
						addWord(parts[0]);
				}
			}
			System.out.println("The result is ��" + nativeStr);
			testICTCLAS50.ICTCLAS_Exit();
		} catch (Exception ex) {
		}

		// ictclas suggestted

	}

	private void addWord(String keyword) {
		System.out.println("addWord " + keyword);

		if (keywordInfo.containsKey(keyword)) {
			int i = keywordInfo.get(keywordInfo);
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
				if(w.endsWith("s"))
				{
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
