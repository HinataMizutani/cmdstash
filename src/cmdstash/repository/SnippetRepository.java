package cmdstash.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cmdstash.exception.SnippetStorageException;
import cmdstash.model.Snippet;

public class SnippetRepository {

	private static final String STORAGE_FILE = System.getProperty("user.home") + "/.cmdstash/snippets.tsv";
	// 保存先を1か所にまとめておくことで、パスを変更したいときここだけ直せばよくする

	/**
	 * 保存されている全スニペットを読み込む。
	 * ファイルがまだ無い場合は、1件も保存されていないとみなして空リストを返す。
	 */
	public List<Snippet> loadAll() {
		Path path = Paths.get(STORAGE_FILE);

		if (!Files.exists(path)) {
			return new ArrayList<>();
			// 初回起動時はファイルが無いのが正常な状態なので、エラーにしない
		}

		List<Snippet> snippets = new ArrayList<>();

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String line;
			while ((line = reader.readLine()) != null) {
				snippets.add(parseLine(line));
				// 1行を1件のSnippetに変換してリストに追加する
			}
		} catch (IOException e) {
			throw new SnippetStorageException("スニペットの読み込みに失敗しました。", e);
		}

		return snippets;
	}

	/**
	 * 全スニペットをファイルへ書き込む（上書き保存）。
	 */
	public void saveAll(List<Snippet> snippets) {
		Path path = Paths.get(STORAGE_FILE);

		try {
			Files.createDirectories(path.getParent());
			// ~/.cmdstash フォルダがまだ無い場合に備えて、書き込み前に作成する
		} catch (IOException e) {
			throw new SnippetStorageException("保存フォルダの作成に失敗しました。", e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			for (Snippet snippet : snippets) {
				writer.write(toLine(snippet));
				writer.newLine();
				// 1件ごとに1行書き込み、改行して次の行へ進む
			}
		} catch (IOException e) {
			throw new SnippetStorageException("スニペットの保存に失敗しました。", e);
		}
	}

	private String toLine(Snippet snippet) {
		String lastUsedAtText = (snippet.getLastUsedAt() == null) ? "" : snippet.getLastUsedAt().toString();
		// lastUsedAtはnullの可能性があるので、nullなら空文字として保存する

		return snippet.getId() + "\t"
				+ snippet.getTitle() + "\t"
				+ snippet.getCommand() + "\t"
				+ snippet.getTag() + "\t"
				+ snippet.getDescription() + "\t"
				+ snippet.getUsageCount() + "\t"
				+ snippet.getCreatedAt() + "\t"
				+ lastUsedAtText;
	}

	private Snippet parseLine(String line) {
		String[] columns = line.split("\t", -1);
		// タブで区切って各項目を取り出す

		int id = Integer.parseInt(columns[0]);
		String title = columns[1];
		String command = columns[2];
		String tag = columns[3];
		String description = columns[4];
		int usageCount = Integer.parseInt(columns[5]);
		LocalDateTime createdAt = LocalDateTime.parse(columns[6]);
		LocalDateTime lastUsedAt = columns[7].isEmpty() ? null : LocalDateTime.parse(columns[7]);
		// 空文字なら「まだ使われていない」という意味なのでnullに戻す

		return new Snippet(id, title, command, tag, description, usageCount, createdAt, lastUsedAt);
	}
}