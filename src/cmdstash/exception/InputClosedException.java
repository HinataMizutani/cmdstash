package cmdstash.exception;

/**
 * キーボード入力がもう読めなくなったことを表す独自例外。
 *
 * Ctrl+D を押されたときや、ファイルから入力を流し込んで途中で尽きたときに起きる。
 * このとき Scanner をそのまま使うと例外で落ち、聞き直しループに入れると永久に回り続けてしまう。
 * 「入力の終わり」は異常ではなく終了の合図なので、専用の例外にして静かに終われるようにする。
 */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException(String message) {
        super(message);
    }
}
