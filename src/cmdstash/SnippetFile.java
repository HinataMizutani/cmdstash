package cmdstash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

// ファイルの読み書き
public class SnippetFile {

	private static final String FILE_NAME = "snippets.txt";
	private static final String SEPARATOR = "\t";
	// FILE_NAME/SEPARATOR は何度も出てくる文字列だから、
	// そこだけ直せばいいように定数にした。

	public ArrayList<Snippet> load() {
		ArrayList<Snippet> list = new ArrayList<Snippet>();

		File file = new File(FILE_NAME);
		//初めて起動したときはファイルが無いのが確定してるから、エラーにせず空で返す
		if (!file.exists()) {
			return list;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
			String line = reader.readLine();

			while (line != null) {
				// -1がないとタグとメモが空欄のとき項目数が足りなくなる
				String[] items = line.split(SEPARATOR, -1);

				if (items.length == 5) {
					int id = Integer.parseInt(items[0]);
					list.add(new Snippet(id, items[1], items[2], items[3], items[4]));
				}

				line = reader.readLine();
			}
		} catch (IOException e) {
			System.out.println("読み込みに失敗しました。");
		}

		return list;
	}

	public void save(ArrayList<Snippet> list) {
		// 例外が出てもファイルが閉じられる。16章でやった書き方
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
			for (Snippet snippet : list) {
				writer.write(snippet.getId() + SEPARATOR + snippet.getTitle() + SEPARATOR
						+ snippet.getCommand() + SEPARATOR + snippet.getTag() + SEPARATOR + snippet.getMemo());
				writer.newLine();
			}
		} catch (IOException e) {
			System.out.println("保存に失敗しました。");
		}
	}
}
