package cmdstash.repository;

import cmdstash.constant.AppConst;
import cmdstash.exception.SnippetStorageException;
import cmdstash.model.Snippet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * スニペットをテキストファイルに読み書きするクラス。
 * 「ファイルのどこに・どんな形式で置くか」を知っているのはこのクラスだけにして、
 * 保存形式を変えたくなったときの影響範囲をここに閉じ込める。
 */
public class SnippetRepository {

    /** 日時の文字列化ルール。読み書きで必ず同じものを使う */
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path dataFilePath;

    /** 前回の読み込みで復元できなかった行数。次の保存で消えてしまうので、利用者に知らせるために持つ */
    private int brokenLineCount;

    public SnippetRepository() {
        String homeDirectory = System.getProperty("user.home");
        // 実行した場所に左右されず、いつも同じ場所を見に行きたいのでホームディレクトリを基準にする

        this.dataFilePath = Paths.get(homeDirectory, AppConst.DATA_DIR_NAME, AppConst.DATA_FILE_NAME);
    }

    /**
     * 保存ファイルの場所を返す（画面に「どこに保存したか」を出すため）。
     */
    public Path getDataFilePath() {
        return dataFilePath;
    }

    /**
     * ファイルから全スニペットを読み込む。ファイルがまだ無ければ空のリストを返す。
     */
    /**
     * 前回の読み込みで捨てた行数を返す。
     */
    public int getBrokenLineCount() {
        return brokenLineCount;
    }

    public List<Snippet> loadAll() {
        List<Snippet> snippets = new ArrayList<>();
        brokenLineCount = 0;
        // 読み直すたびに数え直したいので、最初にリセットする

        if (!Files.exists(dataFilePath)) {
            return snippets;
        }
        // 初回起動時はファイルが無いのが正常なので、エラーにせず空で返して早期return

        List<String> lines;
        try {
            lines = Files.readAllLines(dataFilePath, StandardCharsets.UTF_8);
            // 日本語のメモを書ける必要があるので、文字コードはUTF-8で固定する
        } catch (IOException e) {
            throw new SnippetStorageException("スニペットの読み込みに失敗しました: " + dataFilePath, e);
            // IOExceptionのまま投げず、アプリの言葉に翻訳した独自例外に包み直す
        }

        for (String line : lines) {
            Snippet snippet = parseLine(line);

            if (snippet != null) {
                snippets.add(snippet);
                continue;
            }
            // 壊れた行が1行あっただけで全部使えなくなるのは困るので、その行だけ捨てて先へ進む

            if (!line.trim().isEmpty()) {
                brokenLineCount++;
            }
            // ただし黙って捨てると次の保存で完全に消える。空行以外は数えておき、起動時に警告する
        }

        return snippets;
    }

    /**
     * 渡されたリストの内容でファイルを丸ごと書き直す。
     */
    public void saveAll(List<Snippet> snippets) {
        try {
            Files.createDirectories(dataFilePath.getParent());
            // 保存先フォルダが無いと書き込みで落ちるので、書く前に必ず作っておく
        } catch (IOException e) {
            throw new SnippetStorageException("保存先フォルダを作成できませんでした: " + dataFilePath.getParent(), e);
        }

        Path tempPath = dataFilePath.resolveSibling(dataFilePath.getFileName() + AppConst.TEMP_FILE_SUFFIX);
        // 本番のファイルにいきなり書かず、まず隣に「書きかけファイル」を作る（理由は下のコメント）

        try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
            for (Snippet snippet : snippets) {
                writer.write(toLine(snippet));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new SnippetStorageException("スニペットの保存に失敗しました: " + dataFilePath, e);
        }
        // try-with-resources にして、例外が出てもファイルが閉じられるようにする

        try {
            Files.move(tempPath, dataFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SnippetStorageException("保存ファイルの差し替えに失敗しました: " + dataFilePath, e);
        }
        // 書き終わってから一気に置き換える。
        // 毎回ファイルを全部書き直す方式なので、直接上書き中に強制終了されると全データが消える。
        // 「別ファイルに書き切ってから名前を付け替える」ことで、失敗しても元のファイルが無傷で残る。
    }

    /**
     * 1件のスニペットをタブ区切りの1行に変換する。
     */
    private String toLine(Snippet snippet) {
        String lastUsedText = AppConst.NEVER_USED_MARK;

        if (!snippet.isNeverUsed()) {
            lastUsedText = snippet.getLastUsedAt().format(DATE_TIME_FORMAT);
        }
        // 未使用（null）はそのままだと "null" という文字列になってしまうので、専用の記号に置き換える

        return snippet.getId()
                + AppConst.FIELD_SEPARATOR + snippet.getTitle()
                + AppConst.FIELD_SEPARATOR + snippet.getCommand()
                + AppConst.FIELD_SEPARATOR + snippet.getTag()
                + AppConst.FIELD_SEPARATOR + snippet.getDescription()
                + AppConst.FIELD_SEPARATOR + snippet.getUsageCount()
                + AppConst.FIELD_SEPARATOR + snippet.getCreatedAt().format(DATE_TIME_FORMAT)
                + AppConst.FIELD_SEPARATOR + lastUsedText;
    }

    /**
     * タブ区切りの1行をスニペットに戻す。復元できない行は null を返す。
     */
    private Snippet parseLine(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
        // 末尾の空行などをそのまま解析すると落ちるので、先に弾いておく

        String[] fields = line.split(AppConst.FIELD_SEPARATOR, -1);
        // 区切り文字が連続しても項目数を減らさないため、limitに-1を指定する。
        // 注意：splitの第1引数は正規表現として解釈される。タブは正規表現でもそのままの意味なので今は問題ないが、
        // 区切り文字を "|" などに変えると正規表現の記号とぶつかって壊れる。変更するときはここも見直すこと

        if (fields.length != AppConst.FIELD_COUNT) {
            return null;
        }

        try {
            int id = Integer.parseInt(fields[0]);
            String title = fields[1];
            String command = fields[2];
            String tag = fields[3];
            String description = fields[4];
            int usageCount = Integer.parseInt(fields[5]);
            LocalDateTime createdAt = LocalDateTime.parse(fields[6], DATE_TIME_FORMAT);

            LocalDateTime lastUsedAt = null;
            if (!AppConst.NEVER_USED_MARK.equals(fields[7])) {
                lastUsedAt = LocalDateTime.parse(fields[7], DATE_TIME_FORMAT);
            }
            // 保存時に "-" へ置き換えた未使用状態を、ここで null に戻す

            return new Snippet(id, title, command, tag, description, usageCount, createdAt, lastUsedAt);
        } catch (RuntimeException e) {
            return null;
            // 数値や日時が壊れている行は復元をあきらめる。アプリ全体を止めないための判断
        }
    }
}
