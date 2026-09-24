package cmdstash.ui;

import cmdstash.constant.AppConst;
import cmdstash.model.Snippet;
import cmdstash.model.SnippetTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * スニペットを画面に表示するクラス。
 * 表示の書式をここに集めておくと、一覧・検索・レポートで見た目がバラバラになるのを防ぐ。
 */
public class SnippetView {

    /** 画面に出す日時の書式。保存用の形式とは別物なので、表示側で持つ */
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private static final String LINE = "--------------------------------------------------";

    public void printLine() {
        System.out.println(LINE);
    }

    public void printTitle(String title) {
        System.out.println();
        printLine();
        System.out.println("■ " + title);
        printLine();
        // 各機能の入口で必ず同じ形の見出しを出し、今どの画面にいるかを分かるようにする
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printBlankLine() {
        System.out.println();
        // Menu側に System.out を書かせないため、空行を出すのも表示クラスの仕事にする
    }

    /**
     * メインメニューを表示する。
     */
    public void printMenu(String menuText) {
        printBlankLine();
        printLine();
        System.out.println(menuText);
        printLine();
    }

    /**
     * お手本スニペットの一覧を、選択用の番号付きで表示する。
     */
    public void printTemplateList(List<SnippetTemplate> templates) {
        for (int index = 0; index < templates.size(); index++) {
            SnippetTemplate template = templates.get(index);
            int displayNumber = index + 1;
            // 画面では1から数えたいので、添字に1を足した番号を出す

            System.out.println(displayNumber + ") " + template.getTitle() + "  #" + template.getTag());
            System.out.println("      $ " + template.getCommand());
        }
    }

    /**
     * スニペットの一覧を表示する。空なら空である旨を伝える。
     */
    public void printList(List<Snippet> snippets) {
        if (snippets.isEmpty()) {
            System.out.println("該当するスニペットはありません。");
            return;
        }
        // 空リストのときに何も出ないと「固まった？」と不安になるので、必ず一言返す

        for (Snippet snippet : snippets) {
            printSummary(snippet);
        }
    }

    /**
     * 1件を2行（見出し行＋コマンド行）で表示する。
     */
    public void printSummary(Snippet snippet) {
        String tagText = "";

        if (!snippet.getTag().isEmpty()) {
            tagText = "  #" + snippet.getTag();
        }
        // タグ未設定のときに "#" だけが浮くのを避ける

        System.out.println("[" + snippet.getId() + "] " + snippet.getTitle()
                + "  (" + snippet.getUsageCount() + "回)" + tagText);
        System.out.println("      $ " + snippet.getCommand());
        // 実際のシェルの見た目に寄せて "$" を付け、コマンド行だと一目で分かるようにする
    }

    /**
     * 1件の詳細をすべて表示する。
     */
    public void printDetail(Snippet snippet) {
        printLine();
        System.out.println("ID       : " + snippet.getId());
        System.out.println("タイトル : " + snippet.getTitle());
        System.out.println("コマンド : " + snippet.getCommand());
        System.out.println("タグ     : " + toDisplayText(snippet.getTag()));
        System.out.println("メモ     : " + toDisplayText(snippet.getDescription()));
        System.out.println("使用回数 : " + snippet.getUsageCount() + "回");
        System.out.println("登録日時 : " + snippet.getCreatedAt().format(DISPLAY_FORMAT));
        System.out.println("最終使用 : " + toLastUsedText(snippet));
        printLine();
    }

    /**
     * 棚卸しレポート用に「最後に使ってから何日か」を添えて表示する。
     */
    public void printStaleSummary(Snippet snippet) {
        String statusText;

        if (snippet.isNeverUsed()) {
            statusText = "一度も未使用";
        } else {
            statusText = snippet.getDaysSinceLastUsed() + "日前が最後";
        }
        // 未使用と「久しく使っていない」は、文言を分けて出す

        System.out.println("[" + snippet.getId() + "] " + snippet.getTitle() + "  (" + statusText + ")");
    }

    /**
     * 空文字を「（なし）」に置き換えて、表示が寂しくならないようにする。
     */
    private String toDisplayText(String value) {
        if (value.isEmpty()) {
            return "（なし）";
        }

        return value;
    }

    private String toLastUsedText(Snippet snippet) {
        if (snippet.isNeverUsed()) {
            return "まだ使っていません";
        }

        return snippet.getLastUsedAt().format(DISPLAY_FORMAT)
                + "（" + snippet.getDaysSinceLastUsed() + "日前）";
    }

    /**
     * 起動時の案内。プレースホルダ記法は知らないと使えない機能なので、ここで一度説明する。
     */
    public void printWelcome(String dataFilePath, int snippetCount) {
        printLine();
        System.out.println("cmdstash — よく使うコマンドを貯めて、すぐ呼び出すツール");
        printLine();
        System.out.println("保存先   : " + dataFilePath);
        System.out.println("登録件数 : " + snippetCount + "件");
        System.out.println("ヒント   : コマンドに " + AppConst.PLACEHOLDER_START + "host"
                + AppConst.PLACEHOLDER_END + " と書いておくと、使うときに値を聞いてくれます。");
    }

    /**
     * 1件も登録されていないときに、テンプレート機能へ案内する。
     */
    public void printEmptyStateGuide(int snippetCount) {
        if (snippetCount > 0) {
            return;
        }
        // 登録済みの人に毎回この案内を出すと邪魔なので、空のときだけ出す

        System.out.println();
        System.out.println("まだ1件も登録されていません。");
        System.out.println("メニューの「8. テンプレートから追加する」で、よく使うコマンドをすぐ用意できます。");
    }

    /**
     * 読み込めなかった行があったことを警告する。
     */
    public void printBrokenLineWarning(int brokenLineCount, String dataFilePath) {
        if (brokenLineCount == 0) {
            return;
        }
        // 問題が無いときは何も出さない。正常時に警告欄があると、本当の警告を見逃すようになる

        System.out.println();
        System.out.println("【注意】保存ファイルの " + brokenLineCount + "行を読み込めませんでした。");
        System.out.println("        次に登録・編集・削除をすると、その行は失われます。");
        System.out.println("        残したい場合は先に " + dataFilePath + " をコピーしてください。");
    }
}
