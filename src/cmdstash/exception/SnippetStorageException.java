package cmdstash.exception;

/**
 * スニペットの保存・読み込みに失敗したことを表す独自例外。
 * IOException のまま上に投げると「ファイルの話」が呼び出し側まで漏れてしまうため、
 * アプリの言葉（スニペットの保存が失敗した）に翻訳して伝える。
 */
public class SnippetStorageException extends RuntimeException {

    /** 例外クラスに付ける決まりのバージョン番号。付けないとコンパイル時に警告が出る */
    private static final long serialVersionUID = 1L;

    public SnippetStorageException(String message, Throwable cause) {
        // 元の例外（cause）も一緒に持たせて、原因を追えるようにする
        super(message, cause);
    }

    public SnippetStorageException(String message) {
        super(message);
    }
}
