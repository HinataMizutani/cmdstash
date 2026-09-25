package cmdstash.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 登録した1件のコマンドスニペットを表すクラス。
 */
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
     * 使用回数0・最終使用日時なし、をここで決め打ち。
     */
    public Snippet(int id, String title, String command, String tag, String description) {
        // 登録直後の値が毎回同じだから、初期化を1か所にまとめる（コンストラクタチェーン）
        this(id, title, command, tag, description, 0, LocalDateTime.now(), null);
    }

    /**
     * 復元用のコンストラクタ。
     * ファイルから読み込むときは使用回数や日時、全項目を受け取る。
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
        // 「回数を増やす」と「日時を更新する」は必ずセット、メソッドにまとめる
    }

    /**
     * まだ一度も使われていないかを返す。
     */
    public boolean isNeverUsed() {
        return lastUsedAt == null;
        // nullかどうかを、意味のある名前で聞けるように
    }

    /**
     * 最後に使ってから何日経ったか。未使用の場合は -1。
     */
    public long getDaysSinceLastUsed() {
        if (isNeverUsed()) {
            return -1;
        }
        // 「未使用」を先に返して抜けて、この下をネストさせない（早期return）

        LocalDate lastUsedDate = lastUsedAt.toLocalDate();
        LocalDate today = LocalDate.now();

        return ChronoUnit.DAYS.between(lastUsedDate, today);
        // 時刻まで含めて引くと面倒なので、日付だけで比べる
    }

    /**
     * 指定したキーワードがこのスニペットのどこかに含まれるかを返す。
     */
    public boolean matchesKeyword(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        // 大文字小文字を無視して探したいので、比較前に両側とも小文字へそろえる

        return title.toLowerCase().contains(lowerKeyword)
                || command.toLowerCase().contains(lowerKeyword)
                || tag.toLowerCase().contains(lowerKeyword)
                || description.toLowerCase().contains(lowerKeyword);
        // 検索対象の判定もここに
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
