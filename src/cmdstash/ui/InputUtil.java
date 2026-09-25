package cmdstash.ui;

import cmdstash.exception.InputClosedException;

import java.util.Scanner;

/**
 * キーボード入力を受け取るクラス。
 * 「入力されるまで聞き直す」という同じ処理を各機能で書きたくないので、ここにまとめる。
 */
public class InputUtil {

    /** タブは保存フォーマットの区切り文字で、値としては受け付けない */
    private static final String FORBIDDEN_CHARACTER = "\t";

    private final Scanner scanner;

    public InputUtil(Scanner scanner) {
        this.scanner = scanner;
        // Scannerを外から受け取る形にして、Main側で一元管理する
    }

    /**
     * 指定した範囲の整数が入力されるまで聞き直す。
     */
    public int readNumberInRange(String prompt, int minValue, int maxValue) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();

            try {
                int value = Integer.parseInt(input);

                if (value >= minValue && value <= maxValue) {
                    return value;
                }
                System.out.println("→ " + minValue + "〜" + maxValue + " の数字を入力してください。");
            } catch (NumberFormatException e) {
                System.out.println("→ 数字を入力してください。");
                // 文字を打たれてもアプリを落とさず、聞き直しに戻すためにここで受け止める
            }
        }
    }

    /**
     * 上限を決めずに整数を1つ受け取る。
     */
    public int readNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("→ 数字を入力してください。");
            }
        }
        //「上限が決まっていない／存在チェックが別に必要」な場面で使う
    }

    /**
     * 必ず何か入力してもらう。空文字や長すぎる入力は聞き直す。
     */
    public String readRequiredText(String prompt, int maxLength) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();

            if (input.isEmpty()) {
                System.out.println("→ 空欄にはできません。");
                continue;
            }
            // 問題があるケースを先に弾くことで、ネストを深くしない

            if (input.length() > maxLength) {
                System.out.println("→ " + maxLength + "文字以内で入力してください。");
                continue;
            }

            if (input.contains(FORBIDDEN_CHARACTER)) {
                System.out.println("→ タブ文字は使えません。半角スペースに置き換えてください。");
                continue;
            }

            return input;
        }
    }

    /**
     * 空欄を許す入力。空欄なら空文字を返す。
     */
    public String readOptionalText(String prompt, int maxLength) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();

            if (input.length() > maxLength) {
                System.out.println("→ " + maxLength + "文字以内で入力してください。");
                continue;
            }

            if (input.contains(FORBIDDEN_CHARACTER)) {
                System.out.println("→ タブ文字は使えません。半角スペースに置き換えてください。");
                continue;
            }

            return input;
        }
    }

    /**
     * 編集用の入力。空欄のままEnterを押されたら、今の値をそのまま返す。
     */
    public String readTextOrKeepCurrent(String label, String currentValue, int maxLength) {
        System.out.println(label + "（現在: " + currentValue + "）");
        String input = readOptionalText("  変更後（空欄ならそのまま）> ", maxLength);

        if (input.isEmpty()) {
            return currentValue;
        }
        // 「変更しない」を毎回打ち直させないため。

        return input;
    }

    /**
     * はい／いいえを聞く。y または yes なら true。
     */
    public boolean readYesNo(String prompt) {
        System.out.print(prompt);
        String input = readLine().toLowerCase();

        return input.equals("y") || input.equals("yes");
        // 削除など取り消せない操作の前に確認を挟むために使う
    }

    /**
     * Enterが押されるまで待つ。一覧を読む時間を作るために使う。
     */
    public void waitForEnter() {
        System.out.print("（Enterでメニューに戻ります）");
        readLine();
    }

    /**
     * 1行読み取って前後の空白を取り除く。入力がもう無ければ例外で終了を知らせる。
     */
    private String readLine() {
        if (!scanner.hasNextLine()) {
            throw new InputClosedException("入力が終了しました。");
        }
        // 先に残りがあるか確かめる。確かめないと、聞き直しループが永久に回る

        return scanner.nextLine().trim();
    }
}
