package cmdstash;

import java.util.Scanner;

// キーボード入力
public class InputUtil {

	private Scanner scanner = new Scanner(System.in);

	public String readText(String message) {
		while (true) {
			System.out.print(message);
			String input = scanner.nextLine().trim();

			if (input.isEmpty()) {
				System.out.println("何か入力してください。");
				continue;
			}

			// タブは保存するときの区切り文字なので使えない
			if (input.contains("\t")) {
				System.out.println("タブは使えません。");
				continue;
			}

			return input;
		}
	}

	public String readTextOrEmpty(String message) {
		System.out.print(message);
		String input = scanner.nextLine().trim();

		if (input.contains("\t")) {
			System.out.println("タブは使えないので空欄にします。");
			return "";
		}

		return input;
	}

	public int readNumber(String message) {
		while (true) {
			System.out.print(message);
			String input = scanner.nextLine().trim();

			try {
				return Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("数字を入力してください。");
			}
		}
	}
}
