package cmdstash.service;

import cmdstash.model.SnippetTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 最初から用意しておくお手本スニペットの一覧を持つクラス。
 * ここに並べているのは、とりあえずAIと考えた実務で忘れやすく・打ち間違えると面倒なコマンドを選んだもの。
 */
public class TemplateCatalog {

    private final List<SnippetTemplate> templates;

    public TemplateCatalog() {
        this.templates = createTemplates();
        // 一覧はアプリの起動中ずっと変わらないので、コンストラクタで1回だけ組み立てる
    }

    public List<SnippetTemplate> getAll() {
        return templates;
    }

    /**
     * お手本スニペットを組み立てる。
     * 可変部分は {{名前}} にしてあり、使うときに値を聞かれる。
     */
    private List<SnippetTemplate> createTemplates() {
        List<SnippetTemplate> list = new ArrayList<>();

        list.add(new SnippetTemplate(
                "直前のコミットをやり直す",
                "git commit --amend --no-edit",
                "git",
                "コミットし忘れたファイルを直前のコミットに混ぜ込む"));

        list.add(new SnippetTemplate(
                "作業をいったん退避する",
                "git stash push -m \"{{メモ}}\"",
                "git",
                "急ぎの割り込みが入ったとき、今の変更を名前付きで棚に上げる"));

        list.add(new SnippetTemplate(
                "マージ済みブランチを掃除する",
                "git branch --merged | grep -v \"\\*\" | xargs -n 1 git branch -d",
                "git",
                "取り込み済みのローカルブランチをまとめて削除する"));

        list.add(new SnippetTemplate(
                "ブランチの差分ファイルだけ見る",
                "git diff --name-only {{比較先ブランチ}}",
                "git",
                "レビュー前にどのファイルを触ったか確認する"));

        list.add(new SnippetTemplate(
                "コンテナの中に入る",
                "docker exec -it {{コンテナ名}} bash",
                "docker",
                "動いているコンテナの中で直接コマンドを打ちたいとき"));

        list.add(new SnippetTemplate(
                "コンテナのログを追いかける",
                "docker logs -f --tail 100 {{コンテナ名}}",
                "docker",
                "直近100行から、流れてくるログをそのまま見続ける"));

        list.add(new SnippetTemplate(
                "使っていないDockerリソースを消す",
                "docker system prune -a",
                "docker",
                "ディスクを圧迫しているイメージやキャッシュをまとめて削除（消える範囲に注意）"));

        list.add(new SnippetTemplate(
                "ポートを使っているプロセスを探す",
                "lsof -i :{{ポート番号}}",
                "network",
                "「アドレスは既に使用中です」と言われたときの犯人探し"));

        list.add(new SnippetTemplate(
                "サーバーに接続する",
                "ssh {{ユーザー名}}@{{ホスト}}",
                "ssh",
                "接続先ごとにユーザー名が違って混乱しがちなので穴にしてある"));

        list.add(new SnippetTemplate(
                "ファイルをサーバーへ送る",
                "scp {{手元のパス}} {{ユーザー名}}@{{ホスト}}:{{送り先のパス}}",
                "ssh",
                "引数の順番を毎回思い出せないコマンドの代表格"));

        list.add(new SnippetTemplate(
                "文字列を含むファイルを探す",
                "grep -rn \"{{探したい文字列}}\" .",
                "shell",
                "今いるフォルダ以下を再帰的に検索し、行番号付きで表示する"));

        list.add(new SnippetTemplate(
                "容量を食っているものを探す",
                "du -sh * | sort -rh | head -10",
                "shell",
                "ディスクが足りないときに、大きい順に上位10件を出す"));

        list.add(new SnippetTemplate(
                "JSONを読みやすく整形する",
                "cat {{ファイル名}} | python3 -m json.tool",
                "shell",
                "1行にべったり出力されたAPIのレスポンスを見るとき"));

        return list;
    }
}
