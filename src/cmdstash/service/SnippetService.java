package cmdstash.service;

import cmdstash.constant.AppConst;
import cmdstash.exception.SnippetStorageException;
import cmdstash.model.Snippet;
import cmdstash.repository.SnippetRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * スニペットに対する操作（登録・検索・並べ替えなど）をまとめたクラス。
 * 画面表示はせず、キーボード入力も受け取らない。ロジックだけをここに置くことで、
 * あとでWeb版を作るときもこのクラスをそのまま使い回せる。
 */
public class SnippetService {

    private final SnippetRepository repository;
    private final List<Snippet> snippets;
    private int nextId;

    public SnippetService(SnippetRepository repository) {
        this.repository = repository;
        this.snippets = repository.loadAll();
        // 起動時に一度だけ読み込み、以降はメモリ上のリストを正として扱う（毎回ファイルを読むと遅い）

        this.nextId = calculateNextId();
    }

    /**
     * 次に振るIDを決める。既存の最大ID + 1 にすることで、削除後も番号が衝突しない。
     */
    private int calculateNextId() {
        int maxId = 0;

        for (Snippet snippet : snippets) {
            if (snippet.getId() > maxId) {
                maxId = snippet.getId();
            }
        }

        return maxId + 1;
        // 件数+1にすると、途中を削除したときに既存IDとぶつかるのでこの方式にしている
    }

    public int countAll() {
        return snippets.size();
    }

    /**
     * 同じタイトルが既に登録されているかを返す。
     */
    public boolean existsSameTitle(String title) {
        for (Snippet snippet : snippets) {
            if (snippet.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }

        return false;
        // 大文字小文字違いの重複も「同じもの」として扱いたいので equalsIgnoreCase を使う
    }

    /**
     * 新しいスニペットを登録して保存する。
     */
    public Snippet register(String title, String command, String tag, String description) {
        Snippet snippet = new Snippet(nextId, title, command, tag, description);
        snippets.add(snippet);
        nextId++;

        saveOrRollback();
        // 登録のたびに保存する。アプリが強制終了しても入力が消えないようにするため

        return snippet;
    }

    /**
     * 指定IDのスニペットの内容を書き換えて保存する。書き換えられたら true。
     */
    public boolean update(int id, String title, String command, String tag, String description) {
        Snippet target = findById(id);

        if (target == null) {
            return false;
        }

        target.setTitle(title);
        target.setCommand(command);
        target.setTag(tag);
        target.setDescription(description);
        // 書き換えはこのクラスの中だけで行う。画面側にSnippetを直接いじらせない

        saveOrRollback();

        return true;
    }

    /**
     * 保存する。失敗したらメモリ上の変更を捨て、ファイルの内容に戻してから例外を投げ直す。
     *
     * これが無いと「保存に失敗しました」と画面に出したのに、
     * メモリ上のリストには変更が残ってしまう。
     * その状態で次に別の操作をして保存が成功すると、
     * 失敗したはずの変更まで一緒に書き込まれる ＝ 画面の表示が嘘になる。
     * ファイルの内容を常に正しいものとみなし、書けなかったら読み直して足並みをそろえる。
     */
    private void saveOrRollback() {
        try {
            repository.saveAll(snippets);
        } catch (SnippetStorageException saveError) {
            reloadFromFile();
            throw saveError;
            // 元の失敗理由を画面に出したいので、投げ直すのは最初の例外
        }
    }

    /**
     * メモリ上のリストを、ファイルの内容で作り直す。
     */
    private void reloadFromFile() {
        try {
            snippets.clear();
            snippets.addAll(repository.loadAll());
            nextId = calculateNextId();
        } catch (SnippetStorageException reloadError) {
            // 読み直しにも失敗したら打つ手がない。保存失敗のほうを伝えたいので、ここでは何もしない
        }
    }

    /**
     * IDでスニペットを1件探す。見つからなければ null を返す。
     */
    public Snippet findById(int id) {
        for (Snippet snippet : snippets) {
            if (snippet.getId() == id) {
                return snippet;
            }
        }

        return null;
    }

    /**
     * 指定IDのスニペットを削除する。削除できたら true。
     */
    public boolean deleteById(int id) {
        Snippet target = findById(id);

        if (target == null) {
            return false;
        }
        // 見つからないケースを先に返して、この下は「必ず存在する」前提で書けるようにする

        snippets.remove(target);
        saveOrRollback();

        return true;
    }

    /**
     * スニペットを1回使ったことを記録して保存する。
     */
    public void recordUsage(Snippet snippet) {
        snippet.recordUsage();
        saveOrRollback();
        // 使用回数はランキングの元データなので、その場で保存して取りこぼさない
    }

    /**
     * よく使う順（同数ならタイトル順）に並べた一覧を返す。
     */
    public List<Snippet> findAllSortedByUsage() {
        List<Snippet> sorted = new ArrayList<>(snippets);
        // 元のリストを直接並べ替えると保存順まで変わってしまうので、コピーしてから並べ替える

        sorted.sort(Comparator.comparingInt(Snippet::getUsageCount).reversed()
                .thenComparing(Snippet::getTitle));
        // 使用回数の多い順が主、同数のときは名前順にして表示位置が毎回ブレないようにする

        return sorted;
    }

    /**
     * キーワードを含むスニペットを、よく使う順で返す。
     */
    public List<Snippet> searchByKeyword(String keyword) {
        List<Snippet> matched = new ArrayList<>();

        for (Snippet snippet : findAllSortedByUsage()) {
            if (snippet.matchesKeyword(keyword)) {
                matched.add(snippet);
            }
        }
        // 並べ替え済みのリストを回すことで、検索結果も自動的に「よく使う順」になる

        return matched;
    }

    /**
     * よく使うスニペットの上位を返す（一度も使っていないものは除く）。
     */
    public List<Snippet> findTopUsed() {
        List<Snippet> topUsed = new ArrayList<>();

        for (Snippet snippet : findAllSortedByUsage()) {
            if (snippet.isNeverUsed()) {
                continue;
            }
            // 使用回数0のものがランキングに並んでも情報にならないので除外する

            topUsed.add(snippet);

            if (topUsed.size() == AppConst.TOP_RANKING_SIZE) {
                break;
            }
        }

        return topUsed;
    }

    /**
     * しばらく使っていない（または一度も使っていない）スニペットを返す。
     */
    public List<Snippet> findStaleSnippets() {
        List<Snippet> stale = new ArrayList<>();

        for (Snippet snippet : snippets) {
            if (snippet.isNeverUsed()) {
                stale.add(snippet);
                continue;
            }

            if (snippet.getDaysSinceLastUsed() >= AppConst.STALE_DAYS_THRESHOLD) {
                stale.add(snippet);
            }
        }
        // 「登録したまま忘れている」ものを可視化して、棚卸し（整理か使い直し）を促すのが狙い

        return stale;
    }
}
