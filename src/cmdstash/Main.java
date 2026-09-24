package cmdstash;

import cmdstash.exception.InputClosedException;
import cmdstash.exception.SnippetStorageException;
import cmdstash.repository.SnippetRepository;
import cmdstash.service.SnippetService;
import cmdstash.service.TemplateCatalog;
import cmdstash.ui.InputUtil;
import cmdstash.ui.Menu;
import cmdstash.ui.SnippetView;

import java.util.Scanner;

/**
 * cmdstash の起動クラス。
 * ここでは動かすだけにして、機能そのものは各クラスに任せる。
 */
public class Main {

    public static void main(String[] args) {
        SnippetView view = new SnippetView();

        try (Scanner scanner = new Scanner(System.in)) {
            SnippetRepository repository = new SnippetRepository();
            SnippetService service = new SnippetService(repository);
            TemplateCatalog templateCatalog = new TemplateCatalog();
            InputUtil input = new InputUtil(scanner);
            Menu menu = new Menu(service, templateCatalog, view, input);
            // 部品を作って渡す組み立て作業をmainに集中させ、各クラスは各クラスに集中できるようにする

            view.printWelcome(repository.getDataFilePath().toString(), service.countAll());
            view.printBrokenLineWarning(repository.getBrokenLineCount(), repository.getDataFilePath().toString());
            // 壊れた行は次の保存で消えるので、上書きされる前に気づけるよう起動直後に伝える

            view.printEmptyStateGuide(service.countAll());

            menu.run();
        } catch (InputClosedException e) {
            System.out.println();
            System.out.println("入力が終了したため、cmdstash を終了します。");
            // Ctrl+D などで入力が尽きたケース。異常ではない、エラー扱いにせず静かに終わる
        } catch (SnippetStorageException e) {
            System.out.println();
            System.out.println("【エラー】" + e.getMessage());
            System.out.println("保存ファイルの場所と、書き込み権限を確認してください。");
            // 保存まわりの失敗はユーザーにはどうにもできないので、原因と次の手順だけを短く伝えて終了する
        }
        // try-with-resources にして、どんな終わり方でもScannerが閉じられるようにする
    }
}
