package cmdstash.model;

import java.time.LocalDateTime;

public class Snippet {

	private final int id;
	private String title;
	private String command;
	private String tag;
	private String description;
	private int usageCount;
	private final LocalDateTime createdAt;
	private LocalDateTime lastUsedAt;

	/**
	 * 新規登録用のコンストラクタ。
	 * 使用回数0・最終使用日時なし、という「新品の状態」をここで決め打ちする。
	 */
	public Snippet(int id, String title, String command, String tag, String description) {
		// 登録直後の値が毎回同じなので、フル引数版で初期化を1か所にまとめる（コンストラクタチェーン）
		this(id, title, command, tag, description, 0, LocalDateTime.now(), null);
	}

	/**
	 * 復元用のコンストラクタ。
	 * ファイルから読み込むときは使用回数や日時も保存されているので、全項目を受け取る。
	 */
	public Snippet(int id, String title, String command, String tag, String description,
			int usageCount, LocalDateTime createdAt, LocalDateTime lastUsedAt) {
		this.id = id;
		this.title = title;
		this.command = command;
		this.tag = tag;
		this.description = description;
		this.usageCount = usageCount;
		this.createdAt = createdAt;
		this.lastUsedAt = lastUsedAt;
	}

	/**
	* このスニペットを1回使ったことを記録する。
	*/
	public void recordUsage() {
		this.usageCount++;
		this.lastUsedAt = LocalDateTime.now();
		// 「回数を増やす」と「日時を更新する」は必ずセット、外から別々に触らせずメソッドにまとめる
	}

	/**
	 * まだ一度も使われていないかを返す。
	 */
	public boolean isNeverUsed() {
		return lastUsedAt == null;
		// // lastUsedAt が null かどうかを、外から気にしなくて済むようにする
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getUsageCount() {
		return usageCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getLastUsedAt() {
		return lastUsedAt;
	}
}
