package cmdstash.exception;

/**
 * キーボード入力がもう読めなくなったことを表す独自例外。
 * 「入力の終わり」終了の合図なので、専用の例外にして静かに終われるようにする。
 */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException(String message) {
        super(message);
    }
}
