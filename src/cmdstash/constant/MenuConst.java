package cmdstash.constant;

/**
 * メインメニューの番号をまとめたクラス。
 * switch文の case に生の数字を書くと意味が読めなくなるので、名前を付けて定数化する。
 */
public class MenuConst {

    public static final int EXIT = 0;
    public static final int REGISTER = 1;
    public static final int LIST = 2;
    public static final int SEARCH = 3;
    public static final int USE = 4;
    public static final int EDIT = 5;
    public static final int DELETE = 6;
    public static final int REPORT = 7;
    public static final int TEMPLATE = 8;

    /** 選べる番号の下限・上限。メニューを増やしたらここだけ直せば入力チェックも追従する */
    public static final int MIN_NUMBER = EXIT;
    public static final int MAX_NUMBER = TEMPLATE;

    /** 画面に出すメニュー本文。表示とロジックを1か所で対応付けたいのでここに置く */
    public static final String MENU_TEXT =
            "1. スニペットを登録する\n"
            + "2. 一覧を見る（よく使う順）\n"
            + "3. キーワードで探す\n"
            + "4. スニペットを使う（クリップボードへコピー）\n"
            + "5. スニペットを編集する\n"
            + "6. スニペットを削除する\n"
            + "7. 棚卸しレポートを見る\n"
            + "8. テンプレートから追加する\n"
            + "0. 終了する";

    private MenuConst() {
    }
}
