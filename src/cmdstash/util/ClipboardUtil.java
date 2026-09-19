package cmdstash.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * 文字列をOSのクリップボードへコピーするクラス。
 *
 * ＜授業の範囲外の仕組みを使っている理由＞
 * このアプリの価値は「探したコマンドをすぐ貼り付けられる」ことなので、
 * 画面に出すだけでは手で打ち直す手間が残ってしまう。
 * そこでMacに最初から入っている pbcopy というコマンドを、Javaから呼び出して代わりに働いてもらう。
 * ProcessBuilder は「別のプログラムを起動して、その入力に文字を流し込む」ための標準クラス。
 * 失敗しても致命的ではないので、例外を投げずに成功／失敗を boolean で返す方針にしている。
 */
public class ClipboardUtil {

    /** Macのクリップボードにコピーするコマンド */
    private static final String MAC_COPY_COMMAND = "pbcopy";

    private ClipboardUtil() {
    }

    /**
     * クリップボードへコピーする。成功したら true、できなければ false を返す。
     */
    public static boolean copy(String text) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(MAC_COPY_COMMAND);
            Process process = processBuilder.start();
            // pbcopy を起動する。この時点ではまだ何も渡していない

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(text);
            }
            // pbcopy は「入力が閉じられた時点」でコピーを確定するので、try-with-resourcesで確実に閉じる

            int exitCode = process.waitFor();
            return exitCode == 0;
            // pbcopy の終了を待ち、終了コード0（正常終了）だけを成功とみなす
        } catch (IOException e) {
            return false;
            // pbcopy が無い環境（Windowsなど）では起動自体に失敗する。その場合は静かに失敗を返す
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
            // 待っている間に中断された場合は、中断されたという印を戻してから失敗を返すのが作法
        }
    }
}
