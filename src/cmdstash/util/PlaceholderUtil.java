package cmdstash.util;

import cmdstash.constant.AppConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * コマンド中の {{名前}} という穴（プレースホルダ）を扱うクラス。
 * 例：ssh {{user}}@{{host}} を登録しておき、使うときだけ値を埋める。
 */
public class PlaceholderUtil {

    private PlaceholderUtil() {
    }

    /**
     * コマンド文字列に含まれるプレースホルダ名を、出てきた順に重複なしで返す。
     */
    public static List<String> extractNames(String command) {
        List<String> names = new ArrayList<>();
        int searchStartIndex = 0;

        while (true) {
            int startIndex = command.indexOf(AppConst.PLACEHOLDER_START, searchStartIndex);

            if (startIndex < 0) {
                break;
            }
            // 開始記号が見つからなければ、もう穴は残っていないのでループを抜ける

            int endIndex = command.indexOf(AppConst.PLACEHOLDER_END, startIndex);

            if (endIndex < 0) {
                break;
            }
            // 閉じ記号が無い（書きかけ）の場合は、そこから先は穴とみなさない

            String name = command.substring(startIndex + AppConst.PLACEHOLDER_START.length(), endIndex);
            // trimはしない。あとで fill が {{名前}} をそのまま文字列置換するので、両者の名前を完全に一致させる必要がある

            if (!name.isEmpty() && !names.contains(name)) {
                names.add(name);
            }
            // 同じ名前を2回聞かれると使う側が面倒なので、重複は登録しない

            searchStartIndex = endIndex + AppConst.PLACEHOLDER_END.length();
            // 次の探索は今見つけた穴の後ろから始める（無限ループ防止）
        }

        return names;
    }

    /**
     * プレースホルダを実際の値に置き換えたコマンドを返す。
     */
    public static String fill(String command, Map<String, String> values) {
        String filledCommand = command;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String target = AppConst.PLACEHOLDER_START + entry.getKey() + AppConst.PLACEHOLDER_END;
            filledCommand = filledCommand.replace(target, entry.getValue());
            // replaceは正規表現ではなくただの文字列置換なので、記号入りの値でも安全に置き換えられる
        }

        return filledCommand;
    }
}
