package cmdstash;

import cmdstash.model.Snippet;

public class Main {

	public static void main(String[] args) {
		//Snippet snippet = new Snippet(1, "Nginx再起動", "docker restart web-1", "docker", "よく忘れるやつ");
		// 新規作成なので5引数版。0 / now / null を毎回手で書くと書き間違えるため

		SnippetService service = new SnippetService();
		// Snippet の生成をservice任せにすることで、呼び出し側は「何を登録するか」だけ考えればよくするため

		service.register("挨拶を表示するコード", "System.out.println(\"Hello\");");
		// new Snippet(...) を直接書かず、service.register に文字列を渡すだけにするため

		service.register("合計を求めるコード", "int sum = a + b;");
		// 同じ書き方を繰り返すことで、登録方法が統一されていることを確認するため

		service.register("繰り返し処理のコード", "for (int i = 0; i < 10; i++) {}");
		// 3件登録しておき、findAll() で複数件まとめて取り出せることを見るため

		// ✅ 修正後（1件目のSnippetを別変数で取り出す）
		for (Snippet snippet : service.findAll()) {
			System.out.println(snippet.getTitle() + " : " + snippet.getCommand());
		}

		Snippet firstSnippet = service.findAll().get(0);
		// 動作確認用に、登録した1件目を取り出して使用回数の変化を見る

		System.out.println(firstSnippet.getTitle() + " / " + firstSnippet.getCommand());
		System.out.println("使用回数: " + firstSnippet.getUsageCount());

		firstSnippet.recordUsage();

		System.out.println("使用回数: " + firstSnippet.getUsageCount());

	}
}
