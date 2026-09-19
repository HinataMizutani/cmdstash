package cmdstash.constant;

/**
 * アプリ全体で使う固定値をまとめたクラス。
 * マジックナンバー・マジック文字列を散らかさないため、定数はここに集約する。
 */
public class AppConst {

    /** 保存先ディレクトリ名（ユーザーのホーム直下に作る） */
    public static final String DATA_DIR_NAME = ".cmdstash";

    /** 保存先ファイル名 */
    public static final String DATA_FILE_NAME = "snippets.tsv";

    /** TSVの区切り文字。タブは入力時に禁止するので、値と衝突しない */
    public static final String FIELD_SEPARATOR = "\t";

    /** 1行あたりの項目数。読み込み時の壊れた行チェックに使う */
    public static final int FIELD_COUNT = 8;

    /** 未使用（一度も使っていない）を表す保存用の文字列 */
    public static final String NEVER_USED_MARK = "-";

    /** プレースホルダの開始記号（例：ssh {{host}}） */
    public static final String PLACEHOLDER_START = "{{";

    /** プレースホルダの終了記号 */
    public static final String PLACEHOLDER_END = "}}";

    /** 「よく使う」ランキングに出す件数 */
    public static final int TOP_RANKING_SIZE = 5;

    /** この日数以上使っていないスニペットを棚卸し対象とみなす */
    public static final int STALE_DAYS_THRESHOLD = 30;

    /** 保存の途中で落ちてもデータを失わないための、書きかけファイルの目印 */
    public static final String TEMP_FILE_SUFFIX = ".tmp";

    /** タイトルの最大文字数。長すぎる値でファイルが荒れるのを防ぐ */
    public static final int MAX_TITLE_LENGTH = 40;

    /** コマンド本体の最大文字数 */
    public static final int MAX_COMMAND_LENGTH = 300;

    /** タグの最大文字数 */
    public static final int MAX_TAG_LENGTH = 20;

    /** メモの最大文字数 */
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    /** 検索キーワードの最大文字数 */
    public static final int MAX_KEYWORD_LENGTH = 40;

    /** プレースホルダに入れる値の最大文字数 */
    public static final int MAX_PLACEHOLDER_VALUE_LENGTH = 200;
    // 上限はどれも「たまたま同じ数字」ではなく項目ごとに意味が違うので、使い回さず個別に定義する

    // インスタンス化しても意味がないクラスなので、コンストラクタを隠しておく
    private AppConst() {
    }
}
