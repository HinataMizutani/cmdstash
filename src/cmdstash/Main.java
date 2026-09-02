package cmdstash;

import cmdstash.model.Snippet;

public class Main {

	public static void main(String[] args) {
		Snippet snippet = new Snippet(1, "Nginx再起動", "docker restart web-1", "docker", "よく忘れるやつ");
		// 新規作成なので5引数版。0 / now / null を毎回手で書くと書き間違えるため

		System.out.println(snippet.getTitle() + " / " + snippet.getCommand());
		System.out.println("使用回数: " + snippet.getUsageCount());

		snippet.recordUsage();
		// 使用回数を記録して値が変わること確認

		System.out.println("使用回数: " + snippet.getUsageCount());

	}
}
