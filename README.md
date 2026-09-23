# Hitokoma

1日1枚の写真に短いコメントを添えて残す、「今日の一枚」日記アプリです。
Kotlin Multiplatform + Compose Multiplatform で Android / iOS 向けに開発しています。

## 主な機能

- **今日の一枚** — ギャラリーから写真を1枚選び、コメントを添えて保存します。当日に登録した写真は、その日のうちはこの画面に表示されます。
- **1日1枚の制約** — 同じ日にすでに写真がある場合は、上書きするかどうかを確認するダイアログを表示します（`SaveDailyPhotoUseCase` と DB の主キーの両方で制約しています）。
- **一覧** — これまでに登録した写真を一覧で表示し、削除できます。
- 画面下部のナビゲーションバーで「今日の一枚」と「一覧」を切り替えます。

## 対応プラットフォーム

| プラットフォーム | 状況 |
| --- | --- |
| Android | 動作します（minSdk 24 / targetSdk 35） |
| iOS | 未対応。`LocalImageStorage`・`AppDatabase`・Koin モジュールの iOS 向け `actual` 実装がまだないため、iOS ターゲットはコンパイルできません。`iosApp/` も SwiftUI のテンプレートのままです。 |

## 技術スタック

- Kotlin 2.0.0 / Compose Multiplatform 1.6.11（Material 3）
- Room Multiplatform 2.7.0-alpha13 + バンドル版 SQLite ドライバ（KSP）
- Koin 4.0.0（DI）
- FileKit 0.8.8（ギャラリーからの画像選択）
- kotlinx-datetime / kotlinx-coroutines

依存関係のバージョンは `gradle/libs.versions.toml` で一元管理しています。

## プロジェクト構成

```
.
├── androidApp/   # Android アプリのシェル（MainActivity から shared の App() を呼ぶだけ）
├── iosApp/       # iOS アプリの Xcode プロジェクト（SwiftUI）
└── shared/       # 共通のロジックと Compose UI（KMP モジュール）
    └── src/commonMain/kotlin/jp/pinolab/hitokoma/
        ├── App.kt          # ルートのコンポーザブル（タブ切り替え）
        ├── core/           # ファイル保存・画像・日付などの共通処理（expect/actual を含む）
        ├── data/           # Room のエンティティ/DAO/DB、マッパー、リポジトリ実装
        ├── di/             # Koin モジュール定義
        ├── domain/         # ドメインモデルとリポジトリインターフェース
        └── feature/
            ├── selector/   # 今日の一枚（domain / presentation）
            └── gallery/    # 一覧（domain / presentation）
```

各 feature は `feature/<name>/domain`（ユースケース）と `feature/<name>/presentation`（ViewModel・UiState・Screen）に分かれています。

## ビルドと実行

### 前提

- JDK 17 以上（Room の Gradle プラグインが JDK 17 を必要とします。ターミナルのデフォルトが古い場合は、Android Studio 同梱の JDK を指定してください：`export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`）
- Android Studio（Android 向け）
- Xcode（iOS 向け）

### よく使うコマンド

```sh
./gradlew :androidApp:assembleDebug   # Android のデバッグ APK をビルド
./gradlew :androidApp:installDebug    # 接続中の端末/エミュレータにインストール
./gradlew :shared:testDebugUnitTest   # shared モジュールのユニットテスト（Android ターゲット）
./gradlew :shared:allTests            # 全ターゲットでテストを実行
```

Android Studio からは `androidApp` の実行構成でそのまま起動できます。

## 開発メモ

- 新しい feature を追加するときは `feature/<name>/{domain,presentation}` の構成に合わせてください。feature をまたいで使うモデルやリポジトリはトップレベルの `domain/`・`data/` に置きます。
- 依存関係を追加するときは `build.gradle.kts` にバージョンを直書きせず、`gradle/libs.versions.toml` に追加してください。
- アーキテクチャの詳細は [CLAUDE.md](CLAUDE.md) にまとめています。
