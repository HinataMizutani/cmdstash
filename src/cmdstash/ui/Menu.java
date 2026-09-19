package cmdstash.ui;

import cmdstash.constant.AppConst;
import cmdstash.constant.MenuConst;
import cmdstash.exception.SnippetStorageException;
import cmdstash.model.Snippet;
import cmdstash.model.SnippetTemplate;
import cmdstash.service.SnippetService;
import cmdstash.service.TemplateCatalog;
import cmdstash.util.ClipboardUtil;
import cmdstash.util.PlaceholderUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * メニューを表示し、選ばれた機能を呼び出すクラス。
 * 「入力を受け取る → サービスに頼む → 表示クラスに出してもらう」の橋渡しだけを担当し、
 * 計算や保存のロジックは持たず、画面への出力も自分では行わない。
 */
public class Menu {

    /** 一覧などで「やめる」を選ぶための番号。IDは1から振るので0とぶつからない */
    private static final int CANCEL_NUMBER = 0;

    private final SnippetService service;
    private final TemplateCatalog templateCatalog;
    private final SnippetView view;
    private final InputUtil input;

    public Menu(SnippetService service, TemplateCatalog templateCatalog, SnippetView view, InputUtil input) {
        this.service = service;
        this.templateCatalog = templateCatalog;
        this.view = view;
        this.input = input;
        // 必要な部品を外から受け取る形にして、このクラス自身がnewしないようにする（差し替えやすくなる）
    }

    /**
     * 終了が選ばれるまでメニューを出し続ける。
     */
    public void run() {
        boolean isRunning = true;

        while (isRunning) {
            view.printMenu(MenuConst.MENU_TEXT);

            int selected = input.readNumberInRange(
                    "番号を選んでください > ", MenuConst.MIN_NUMBER, MenuConst.MAX_NUMBER);

            isRunning = executeSelected(selected);
            // ループ継続の判断を戻り値1つに集約し、whileの条件をシンプルに保つ
        }

        view.printMessage("cmdstash を終了しました。また使ってください。");
    }

    /**
     * 選ばれた番号に対応する機能を実行する。続行するなら true を返す。
     */
    private boolean executeSelected(int selected) {
        try {
            return dispatch(selected);
        } catch (SnippetStorageException e) {
            view.printMessage("");
            view.printMessage("【保存エラー】" + e.getMessage());
            view.printMessage("この操作は保存されませんでした。空き容量と書き込み権限を確認してください。");
            return true;
        }
        // 保存に1回失敗しただけでアプリごと終わると、他の登録内容も見られなくなる。
        // 起動時の読み込み失敗（Mainで処理）と違い、実行中の保存失敗は伝えて続行するのが親切。
    }

    private boolean dispatch(int selected) {
        switch (selected) {
            case MenuConst.REGISTER:
                registerSnippet();
                break;
            case MenuConst.LIST:
                showList();
                break;
            case MenuConst.SEARCH:
                searchSnippet();
                break;
            case MenuConst.USE:
                useSnippet();
                break;
            case MenuConst.EDIT:
                editSnippet();
                break;
            case MenuConst.DELETE:
                deleteSnippet();
                break;
            case MenuConst.REPORT:
                showReport();
                break;
            case MenuConst.TEMPLATE:
                addFromTemplate();
                break;
            case MenuConst.EXIT:
                return false;
            default:
                view.printMessage("その番号の機能はありません。");
                break;
        }

        return true;
        // 入力範囲は readNumberInRange で保証済みだが、将来メニューを増やしたときの保険として default を残す
    }

    /**
     * 1. 登録
     */
    private void registerSnippet() {
        view.printTitle("スニペットを登録する");

        String title = readUniqueTitle();
        String command = input.readRequiredText("コマンド > ", AppConst.MAX_COMMAND_LENGTH);
        String tag = input.readOptionalText("タグ（任意・例 docker） > ", AppConst.MAX_TAG_LENGTH);
        String description = input.readOptionalText("メモ（任意） > ", AppConst.MAX_DESCRIPTION_LENGTH);

        Snippet registered = service.register(title, command, tag, description);

        view.printMessage("登録しました。");
        view.printDetail(registered);
        printPlaceholderHint(command);

        input.waitForEnter();
    }

    /**
     * コマンドに穴があることを知らせる。書き間違いにその場で気づけるようにするため。
     */
    private void printPlaceholderHint(String command) {
        List<String> placeholderNames = PlaceholderUtil.extractNames(command);

        if (placeholderNames.isEmpty()) {
            return;
        }

        view.printMessage("このコマンドには " + placeholderNames.size() + " 個の穴があります: " + placeholderNames);
    }

    /**
     * 新規登録用。重複しないタイトルが入力されるまで聞き直す。
     */
    private String readUniqueTitle() {
        while (true) {
            String title = input.readRequiredText("タイトル > ", AppConst.MAX_TITLE_LENGTH);

            if (!service.existsSameTitle(title)) {
                return title;
            }

            view.printMessage("→ 同じタイトルが既にあります。別の名前にしてください。");
            // 同名が並ぶと後から探すときに区別できなくなるので、登録・編集どちらでも防ぐ
        }
    }

    /**
     * 2. 一覧
     */
    private void showList() {
        view.printTitle("一覧（よく使う順）");
        view.printList(service.findAllSortedByUsage());
        input.waitForEnter();
    }

    /**
     * 3. 検索
     */
    private void searchSnippet() {
        view.printTitle("キーワードで探す");

        String keyword = input.readRequiredText("キーワード > ", AppConst.MAX_KEYWORD_LENGTH);
        List<Snippet> results = service.searchByKeyword(keyword);

        view.printMessage(results.size() + "件見つかりました。");
        view.printList(results);
        input.waitForEnter();
    }

    /**
     * 4. 使う（プレースホルダを埋めてクリップボードへ）
     */
    private void useSnippet() {
        view.printTitle("スニペットを使う");

        Snippet target = selectSnippet();
        if (target == null) {
            return;
        }
        // 選択がキャンセルされたら何もせず戻る（早期return）

        String finalCommand = buildCommandWithValues(target);

        view.printBlankLine();
        view.printMessage("実行するコマンド:");
        view.printMessage("  $ " + finalCommand);

        boolean isCopied = ClipboardUtil.copy(finalCommand);

        if (isCopied) {
            view.printMessage("→ クリップボードにコピーしました。ターミナルに貼り付けてください。");
        } else {
            view.printMessage("→ クリップボードにコピーできませんでした。上の行をコピーして使ってください。");
        }
        // コピー失敗でも作業が止まらないよう、必ず画面にもコマンドを出しておく

        service.recordUsage(target);
        view.printMessage("（使用回数を " + target.getUsageCount() + "回 に更新しました）");

        input.waitForEnter();
    }

    /**
     * プレースホルダがあれば値を聞いて、実行用のコマンドを組み立てる。
     */
    private String buildCommandWithValues(Snippet snippet) {
        List<String> placeholderNames = PlaceholderUtil.extractNames(snippet.getCommand());

        if (placeholderNames.isEmpty()) {
            return snippet.getCommand();
        }
        // 穴が無いときは何も聞かずにそのまま返す

        Map<String, String> values = new LinkedHashMap<>();
        view.printMessage("値を入力してください（" + placeholderNames.size() + "項目）");

        for (String name : placeholderNames) {
            String value = input.readRequiredText("  " + name + " > ", AppConst.MAX_PLACEHOLDER_VALUE_LENGTH);
            values.put(name, value);
        }
        // 聞いた順番を保ちたいのでLinkedHashMapを使う（HashMapだと順序が保証されない）

        return PlaceholderUtil.fill(snippet.getCommand(), values);
    }

    /**
     * 5. 編集
     */
    private void editSnippet() {
        view.printTitle("スニペットを編集する");

        Snippet target = selectSnippet();
        if (target == null) {
            return;
        }

        view.printDetail(target);

        String newTitle = readEditedTitle(target.getTitle());
        String newCommand = input.readTextOrKeepCurrent("コマンド", target.getCommand(), AppConst.MAX_COMMAND_LENGTH);
        String newTag = input.readTextOrKeepCurrent("タグ", target.getTag(), AppConst.MAX_TAG_LENGTH);
        String newDescription =
                input.readTextOrKeepCurrent("メモ", target.getDescription(), AppConst.MAX_DESCRIPTION_LENGTH);
        // 入力を先に全部そろえてから、書き換えはサービスに1回で任せる。
        // 画面側がSnippetを直接書き換えると、保存に失敗したときに元へ戻す手立てが無くなる

        service.update(target.getId(), newTitle, newCommand, newTag, newDescription);

        view.printMessage("更新しました。");
        view.printDetail(target);

        input.waitForEnter();
    }

    /**
     * 編集時のタイトル入力。空Enterなら今のまま、変更するなら重複チェックを通す。
     */
    private String readEditedTitle(String currentTitle) {
        while (true) {
            view.printMessage("タイトル（現在: " + currentTitle + "）");
            String newTitle = input.readOptionalText("  変更後（空欄ならそのまま）> ", AppConst.MAX_TITLE_LENGTH);

            if (newTitle.isEmpty() || newTitle.equalsIgnoreCase(currentTitle)) {
                return currentTitle;
            }
            // 空Enterはもちろん、今と同じ名前を打たれた場合も「変更なし」として通す

            if (!service.existsSameTitle(newTitle)) {
                return newTitle;
            }

            view.printMessage("→ 同じタイトルが既にあります。別の名前にしてください。");
            // 登録時だけ重複を防いでも、編集で同じ名前にできてしまっては意味がない。
            // 聞き直しは空Enterで抜けられるようにして、行き止まりを作らない
        }
    }

    /**
     * 6. 削除
     */
    private void deleteSnippet() {
        view.printTitle("スニペットを削除する");

        Snippet target = selectSnippet();
        if (target == null) {
            return;
        }

        view.printDetail(target);

        boolean isConfirmed = input.readYesNo("本当に削除しますか？ (y/n) > ");

        if (!isConfirmed) {
            view.printMessage("削除をやめました。");
            return;
        }
        // 取り消せない操作なので、実行前に必ず確認をはさむ

        service.deleteById(target.getId());
        view.printMessage("削除しました。");

        input.waitForEnter();
        // 他の機能と同じく、結果を読む時間を作ってからメニューに戻す
    }

    /**
     * 7. 棚卸しレポート
     */
    private void showReport() {
        view.printTitle("棚卸しレポート");

        printTopUsedSection();
        view.printBlankLine();
        printStaleSection();

        input.waitForEnter();
    }

    private void printTopUsedSection() {
        view.printMessage("● よく使うスニペット TOP" + AppConst.TOP_RANKING_SIZE);
        List<Snippet> topUsed = service.findTopUsed();

        if (topUsed.isEmpty()) {
            view.printMessage("  まだ使用実績がありません。");
            return;
        }

        for (Snippet snippet : topUsed) {
            view.printMessage("  " + snippet.getUsageCount() + "回  " + snippet.getTitle());
        }
    }

    private void printStaleSection() {
        view.printMessage("● " + AppConst.STALE_DAYS_THRESHOLD + "日以上使っていない／未使用のスニペット");
        List<Snippet> staleSnippets = service.findStaleSnippets();

        if (staleSnippets.isEmpty()) {
            view.printMessage("  ありません。よく整理されています。");
            return;
        }
        // 「該当なし」で早く返して、この下を「必ず1件以上ある」前提で書けるようにする

        for (Snippet snippet : staleSnippets) {
            view.printStaleSummary(snippet);
        }

        view.printMessage("  → 使わないものは削除、使えるものは思い出して使うと一覧が軽くなります。");
        // 「増える一方で探しにくくなる」というこの手のツールの弱点に、正面から対処するための機能
    }

    /**
     * 8. テンプレートから追加
     */
    private void addFromTemplate() {
        view.printTitle("テンプレートから追加する");

        List<SnippetTemplate> templates = templateCatalog.getAll();
        view.printTemplateList(templates);
        view.printBlankLine();

        boolean addsAll = input.readYesNo("すべて追加しますか？ (y/n、nなら1件ずつ選べます) > ");

        if (addsAll) {
            addAllTemplates(templates);
            input.waitForEnter();
            return;
        }

        selectTemplatesOneByOne(templates);
        input.waitForEnter();
    }

    private void addAllTemplates(List<SnippetTemplate> templates) {
        int addedCount = 0;
        int skippedCount = 0;

        for (SnippetTemplate template : templates) {
            if (service.existsSameTitle(template.getTitle())) {
                skippedCount++;
                continue;
            }
            // 2回目に実行しても二重登録にならないよう、同名は静かに飛ばす

            service.register(template.getTitle(), template.getCommand(),
                    template.getTag(), template.getDescription());
            addedCount++;
        }

        view.printMessage(addedCount + "件を追加しました。（登録済みのため " + skippedCount + "件はスキップ）");
    }

    private void selectTemplatesOneByOne(List<SnippetTemplate> templates) {
        while (true) {
            int number = input.readNumber("番号を入力（" + CANCEL_NUMBER + "で終了） > ");

            if (number == CANCEL_NUMBER) {
                return;
            }

            if (number < 1 || number > templates.size()) {
                view.printMessage("→ 1〜" + templates.size() + " の番号を入力してください。");
                continue;
            }

            SnippetTemplate selected = templates.get(number - 1);

            if (service.existsSameTitle(selected.getTitle())) {
                view.printMessage("→ 「" + selected.getTitle() + "」は既に登録されています。");
                continue;
            }

            service.register(selected.getTitle(), selected.getCommand(),
                    selected.getTag(), selected.getDescription());
            view.printMessage("→ 「" + selected.getTitle() + "」を追加しました。");
            // 1件ごとに結果を返すことで、続けて選ぶかどうかを判断できるようにする
        }
    }

    /**
     * 一覧を出して、対象のスニペットを1件選んでもらう。
     * キャンセルされた場合や1件も無い場合は null を返す。
     */
    private Snippet selectSnippet() {
        if (service.countAll() == 0) {
            view.printMessage("まだスニペットが1件も登録されていません。");
            view.printMessage("メニューの「8. テンプレートから追加する」から始めるのが早いです。");
            return null;
        }

        view.printList(service.findAllSortedByUsage());

        while (true) {
            int id = input.readNumber("IDを入力（" + CANCEL_NUMBER + "でキャンセル） > ");
            // IDは削除で歯抜けになるため「1〜N」の範囲チェックが成り立たない。範囲ではなく存在で確かめる

            if (id == CANCEL_NUMBER) {
                return null;
            }

            Snippet found = service.findById(id);

            if (found != null) {
                return found;
            }

            view.printMessage("→ そのIDのスニペットはありません。");
        }
    }
}
