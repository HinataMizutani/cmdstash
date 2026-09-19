package cmdstash.exception;

public class SnippetStorageException extends RuntimeException {

	public SnippetStorageException(String message, Throwable cause) {
		super(message, cause);
		// ファイルの読み書き失敗はプログラムのバグではなく環境側の問題なので、
		// 呼び出し元に毎回throwsを書かせない非チェック例外(RuntimeException)にした
	}
}