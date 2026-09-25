package cmdstash.model;

/**
 * 最初から用意してある「お手本スニペット」を表すクラス。
 * 同じクラスで兼ねると「まだ登録されていないのにIDがある」という中途半端な状態が生まれるので、
 * 型を分けて区別。
 */
public class SnippetTemplate {

    private final String title;
    private final String command;
    private final String tag;
    private final String description;

    public SnippetTemplate(String title, String command, String tag, String description) {
        this.title = title;
        this.command = command;
        this.tag = tag;
        this.description = description;
        // 中身を後から書き換える必要がないので、全フィールドをfinal
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return command;
    }

    public String getTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }
}
