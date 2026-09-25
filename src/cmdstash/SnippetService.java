package cmdstash;

import java.util.ArrayList;

// 登録・検索・削除などの処理
public class SnippetService {

	private SnippetFile file = new SnippetFile();
	private ArrayList<Snippet> list;
	private int nextId;

	public SnippetService() {
		list = file.load();
		nextId = makeNextId();
		// 件数+1だと途中を削除した既存IDとぶつかるので、最大値+1にした
	}

	private int makeNextId() {
		int max = 0;

		for (Snippet snippet : list) {
			if (snippet.getId() > max) {
				max = snippet.getId();
			}
		}

		return max + 1;
	}

	public ArrayList<Snippet> getAll() {
		return list;
	}

	public int count() {
		return list.size();
	}

	public void add(String title, String command, String tag, String memo) {
		list.add(new Snippet(nextId, title, command, tag, memo));
		nextId++;
		file.save(list);
		// 登録のたびに保存。終了時まとめてだと強制終了で消える
	}

	public Snippet findById(int id) {
		for (Snippet snippet : list) {
			if (snippet.getId() == id) {
				return snippet;
			}
		}

		return null;
	}

	public ArrayList<Snippet> search(String keyword) {
		ArrayList<Snippet> result = new ArrayList<Snippet>();

		for (Snippet snippet : list) {
			if (snippet.contains(keyword)) {
				result.add(snippet);
			}
		}

		return result;
	}

	public void save() {
		file.save(list);
	}

	public void delete(Snippet snippet) {
		list.remove(snippet);
		file.save(list);
	}
}
