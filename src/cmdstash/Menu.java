package cmdstash;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

// メニューの表示と各機能
public class Menu {

	private SnippetService service = new SnippetService();
	private InputUtil input = new InputUtil();

	public void run() {
		System.out.println("=== cmdstash ===");
		System.out.println("よく使うコマンドを登録しておくアプリです。");
		System.out.println("登録件数: " + service.count() + "件");

		boolean running = true;

		while (running) {
			showMenu();
			int number = input.readNumber("番号を選んでください > ");

			switch (number) {
			case 1:
				add();
				// 一度入ったら抜けられないと困るので、空欄で中止に
				break;
			case 2:
				showAll();
				break;
			case 3:
				search();
				break;
			case 4:
				use();
				break;
			case 5:
				edit();
				break;
			case 6:
				delete();
				break;
			case 0:
				running = false;
				break;
			default:
				System.out.println("その番号はありません。");
			}
		}

		System.out.println("終了します。");
	}

	private void showMenu() {
		System.out.println();
		System.out.println("--------------------");
		System.out.println("1. 登録する");
		System.out.println("2. 一覧を見る");
		System.out.println("3. 検索する");
		System.out.println("4. 使う（コピーする）");
		System.out.println("5. 編集する");
		System.out.println("6. 削除する");
		System.out.println("0. 終了する");
		System.out.println("--------------------");
	}

	private void add() {
		System.out.println("■ 登録");
		System.out.println("空欄でEnterを押すと登録をやめます。");
		System.out.println("コマンドの中に {{ホスト名}} と書くと、使うときに入力できます。");

		String title = input.readTextOrEmpty("タイトル > ");
		if (title.isEmpty()) {
			System.out.println("登録をやめました。");
			return;
		}

		String command = input.readTextOrEmpty("コマンド > ");
		if (command.isEmpty()) {
			System.out.println("登録をやめました。");
			return;
		}

		String tag = input.readTextOrEmpty("タグ（任意） > ");
		String memo = input.readTextOrEmpty("メモ（任意） > ");

		service.add(title, command, tag, memo);

		System.out.println("登録しました。");
	}

	private void showAll() {
		System.out.println("■ 一覧");
		printList(service.getAll());
	}

	private void search() {
		System.out.println("■ 検索");

		String keyword = input.readTextOrEmpty("キーワード（空欄でやめる） > ");
		if (keyword.isEmpty()) {
			return;
		}

		ArrayList<Snippet> result = service.search(keyword);

		System.out.println(result.size() + "件見つかりました。");
		printList(result);
	}

	private void use() {
		System.out.println("■ 使う");
		printList(service.getAll());

		Snippet target = selectSnippet("使うID");
		if (target == null) {
			return;
		}

		String command = fillPlaceholder(target.getCommand());

		System.out.println("$ " + command);
		copyToClipboard(command);
	}

	// {{名前}} の部分を入力してもらって置き換える
	private String fillPlaceholder(String command) {
		String result = command;

		while (result.contains("{{")) {
			int start = result.indexOf("{{");
			int end = result.indexOf("}}", start);
			// start から後ろを探す。

			if (end < 0) {
				break;
			}

			String name = result.substring(start + 2, end);
			String value = input.readText(name + " > ");

			result = result.replace("{{" + name + "}}", value);
		}

		return result;
	}

	private void copyToClipboard(String text) {
		try {
			StringSelection selection = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
			System.out.println("クリップボードにコピーしました。");
			// 中身が他アプリに置き換わったとき知らせてもらう相手。今回は要らない
		} catch (Exception e) {
			System.out.println("クリップボードにコピーできませんでした。上の行をコピーしてください。");
		}
	}

	private void edit() {
		System.out.println("■ 編集");
		printList(service.getAll());

		Snippet target = selectSnippet("編集するID");
		// （編集・削除・使う）同じ処理だからまとめる
		if (target == null) {
			return;
		}

		System.out.println("そのままでよければ空欄でEnterを押してください。");
		target.setTitle(readOrKeep("タイトル", target.getTitle()));
		target.setCommand(readOrKeep("コマンド", target.getCommand()));
		target.setTag(readOrKeep("タグ", target.getTag()));
		target.setMemo(readOrKeep("メモ", target.getMemo()));

		service.save();

		System.out.println("更新しました。");
	}

	private String readOrKeep(String name, String now) {
		String value = input.readTextOrEmpty(name + "（今: " + now + "） > ");

		if (value.isEmpty()) {
			return now;
		}

		return value;
	}

	private void delete() {
		System.out.println("■ 削除");
		printList(service.getAll());

		Snippet target = selectSnippet("削除するID");
		if (target == null) {
			return;
		}

		System.out.println("[" + target.getId() + "] " + target.getTitle() + " を削除します。");
		String answer = input.readTextOrEmpty("よろしいですか？(y/n) > ");

		if (!answer.equals("y")) {
			System.out.println("削除をやめました。");
			return;
		}

		service.delete(target);

		System.out.println("削除しました。");
	}

	private Snippet selectSnippet(String message) {
		if (service.count() == 0) {
			return null;
		}

		int id = input.readNumber(message + "（0で戻る） > ");

		if (id == 0) {
			return null;
		}

		Snippet target = service.findById(id);

		if (target == null) {
			System.out.println("そのIDはありません。");
			return null;
		}

		return target;
	}

	private void printList(ArrayList<Snippet> list) {
		if (list.isEmpty()) {
			System.out.println("まだ登録がありません。");
			return;
		}

		for (Snippet snippet : list) {
			String tagText = "";

			if (!snippet.getTag().isEmpty()) {
				tagText = "  #" + snippet.getTag();
			}

			System.out.println("[" + snippet.getId() + "] " + snippet.getTitle() + tagText);
			System.out.println("    " + snippet.getCommand());

			if (!snippet.getMemo().isEmpty()) {
				System.out.println("    メモ: " + snippet.getMemo());
			}
		}
	}
}
