package cmdstash;

import java.util.List;

import cmdstash.model.Snippet;
import cmdstash.repository.SnippetRepository;

public class SnippetService {

	private final SnippetRepository repository = new SnippetRepository();
	// ファイルの読み書きはrepositoryに任せ、serviceは「何をするか」だけを考える

	private final List<Snippet> snippets;
	private int nextId;

	public SnippetService() {
		this.snippets = repository.loadAll();
		// 起動時に保存済みのデータを読み込んでおく

		this.nextId = calculateNextId();
	}

	private int calculateNextId() {
		int maxId = 0;
		for (Snippet snippet : snippets) {
			if (snippet.getId() > maxId) {
				maxId = snippet.getId();
			}
		}
		return maxId + 1;
		// 保存データが無ければmaxIdは0のままなので、1から始まる
	}

	public void register(String title, String command) {
		Snippet snippet = new Snippet(nextId, title, command, "", "");
		snippets.add(snippet);
		nextId++;

		repository.saveAll(snippets);
		// 登録するたびに保存し直すことで、アプリを終了しても内容が残るようにする
	}

	public List<Snippet> findAll() {
		return snippets;
	}
}