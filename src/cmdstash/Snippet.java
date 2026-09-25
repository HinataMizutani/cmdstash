package cmdstash;

// コマンド1件分のデータ
public class Snippet {

	private int id;
	private String title;
	private String command;
	private String tag;
	private String memo;

	public Snippet(int id, String title, String command, String tag, String memo) {
		this.id = id;
		this.title = title;
		this.command = command;
		this.tag = tag;
		this.memo = memo;
	}

	public boolean contains(String keyword) {
		return title.contains(keyword) || command.contains(keyword)
				|| tag.contains(keyword) || memo.contains(keyword);
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
