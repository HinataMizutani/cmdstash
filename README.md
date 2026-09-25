# cmdstash 1.0

よく使うコマンドを登録しておいて、あとから探して使えるCLIアプリです。

長いコマンドや、たまにしか使わないコマンドを毎回思い出せないので作りました。

## 動かし方

Eclipse で `src` をソースフォルダにして、`cmdstash.Main` を実行します。

## 機能

| 番号 | 機能 |
|---|---|
| 1 | 登録する |
| 2 | 一覧を見る |
| 3 | 検索する |
| 4 | 使う（クリップボードにコピー） |
| 5 | 編集する |
| 6 | 削除する |
| 0 | 終了する |

検索はタイトル・コマンド・タグ・メモのどれかに含まれていればヒットします。

登録の途中で空欄のままEnterを押すとやめられます。編集と削除はIDに0を入れると戻ります。

## コマンドの中に穴を作る

コマンドに `{{ホスト名}}` のように書いておくと、使うときにその値を聞いてくれます。

```
登録したコマンド : ssh {{ユーザー}}@{{ホスト}}

4番の「使う」を選ぶと
  ユーザー > deploy
  ホスト > example.com
  $ ssh deploy@example.com
```

ホスト名やコンテナ名を書き換え忘れると事故るので、毎回聞くようにしました。
同じ名前が2か所に出てくる場合も、聞かれるのは1回です。

## クラス構成

| クラス | 役割 |
|---|---|
| Main | アプリを起動する |
| Menu | メニューを表示して機能を呼び出す |
| Snippet | コマンド1件分のデータ |
| SnippetService | 登録・検索・削除などの処理 |
| SnippetFile | ファイルの読み書き |
| InputUtil | キーボード入力 |

## 保存

実行したフォルダに `snippets.txt` ができます。タブ区切りです。

```
1	コンテナに入る	docker exec -it {{コンテナ名}} bash	docker	よく忘れる
2	ポート確認	lsof -i :8080	network	
```

カンマ区切りにするとコマンドの中のカンマとぶつかるので、タブにしました。

登録・編集・削除をするたびに保存しています。

## 使ったJavaの内容

- クラス、カプセル化、コンストラクタ
- ArrayList
- switch文、while文、拡張for文
- String の indexOf、substring、replace（穴の置き換え）
- try-catch、try-with-resources
- ファイル入出力（BufferedReader、BufferedWriter）
- クリップボード（Toolkit、StringSelection）

ファイル入出力とクリップボードは授業でまだやっていないので、下の「調べて使ったところ」に参考にしたページを書いています。

## 調べて使ったところ

授業でまだやっていない部分は、公式のAPI仕様を見ながら書きました。

### ファイルの読み書き（SnippetFile）

アプリを閉じても登録内容が残らないと意味がないので、テキストファイルに保存することにしました。

- [BufferedReader (Java SE 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/BufferedReader.html) — `readLine()` で1行ずつ読む
- [BufferedWriter (Java SE 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/BufferedWriter.html) — `write()` と `newLine()` で1行ずつ書く
- [String.split (Java SE 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html#split(java.lang.String,int)) — 第2引数に `-1` を入れると末尾の空文字が消えない

`try-with-resources` は授業（第16章）でやった書き方をそのまま使っています。

### クリップボードへのコピー（Menu）

探したコマンドを手で打ち直すと、このアプリを使う意味がなくなるのでコピーできるようにしました。(ここはどうしても譲れないポイント)

- [StringSelection (Java SE 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.datatransfer/java/awt/datatransfer/StringSelection.html) — 文字列をクリップボードに渡せる形にするクラス
- [Toolkit (Java SE 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Toolkit.html) — `getSystemClipboard()` でOSのクリップボードを取り出す

```java
StringSelection selection = new StringSelection(text);
Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
```

2つ目の引数は ClipboardOwner で、クリップボードの中身が他のアプリに置き換えられたときに知らせてもらう相手を指定します。
今回は知らせてもらう必要がないので `null` にしています。

## 詰まったところ

タグとメモを空欄で登録すると、次に起動したときにその行が表示されなくなりました。
`split()` は末尾が空文字だと項目を減らすらしく、5項目のはずが3項目になっていました。
`split(SEPARATOR, -1)` にしたら直りました。

## 開発について

要件の整理と実装でAIに相談しました。

## これからやりたいこと

- よく使う順に並べ替える
- タグだけで絞り込む
