# CLAUDE.md

このファイルは、このリポジトリでコードを扱う際にClaude Code (claude.ai/code) へ向けたガイダンスを提供します。

## プロジェクト概要

Hitokoma (jp.pinolab.hitokoma) は、Compose Multiplatformで構築されたKotlin Multiplatformアプリ（Android + iOS）です。コアコンセプトは、1日1枚の写真を選び、短いコメント（「今日の一枚」）を添えること。1日1枚という制約はDBおよびユースケース層の両方で強制されます。

- `shared/` — ドメイン/データ/プレゼンテーションロジックとCompose UIをすべて含み、プラットフォーム間で共有されるKMPモジュール。
- `androidApp/` — 薄いAndroidアプリのシェル（`shared`の`App()`を呼び出す`MainActivity`のみ）。
- `iosApp/` — `shared`フレームワークを組み込むXcodeプロジェクト（SwiftUI）。現時点ではデフォルトテンプレート（`ContentView`/`iOSApp.swift`）のままで、共有のCompose UIとはまだ接続されていません。

## よく使うコマンド

リポジトリのルートからGradle wrapperを使って実行します。

```
./gradlew build                     # 全体をビルド
./gradlew :shared:build             # sharedのKMPモジュールのみビルド
./gradlew :androidApp:assembleDebug # AndroidのデバッグAPKをビルド
./gradlew :shared:testDebugUnitTest # sharedモジュールのユニットテストを実行（Androidターゲット）
./gradlew :shared:allTests          # 全KMPターゲットでテストを実行
./gradlew test --tests "jp.pinolab.hitokoma.SomeTest"  # 単一のテストクラスを実行
```

現時点では専用のlint/ktlint/detekt設定はリポジトリに存在しません。`./gradlew build`は標準のAndroid/Kotlinコンパイラのチェックのみを行います。`commonTest`は`kotlin-test`にのみ依存しており、テストはまだ書かれていません。

iOS側は、`iosApp/iosApp.xcodeproj`をXcodeで開くか、`./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`でKMPフレームワークを直接ビルドしてください（実機/シミュレータやアーキテクチャに応じてターゲット名を調整）。

## アーキテクチャ

sharedモジュールは`shared/src/commonMain/kotlin/jp/pinolab/hitokoma/`配下でレイヤー構造になっています。

- `domain/model` — 機能横断で共有されるプレーンなデータクラス（例: `DailyPhoto`）。
- `domain/repository` — 各featureやユースケースが依存するリポジトリインターフェース（`PhotoRepository`）。実装は`data/`にあります。
- `data/local/db` — Roomのエンティティ・DAO（`PhotoEntity`、`PhotoDao`）。ここで使われているRoomはマルチプラットフォームのプレビュービルド（`2.7.0-alpha13`）で、バンドルされたSQLiteドライバを利用しています。
- `data/mapper` — `Entity <-> Domain`モデルの変換用拡張関数（`toDomain()` / `toEntity()`）。
- `data/repository` — DAOをラップしてマッパー変換を行うリポジトリ実装（`PhotoRepositoryImpl`）。
- `feature/<featureName>/domain` — feature単位のユースケースとドメイン例外（例: `feature/selector/domain/SaveDailyPhotoUseCase`、`PhotoAlreadyExistsException`）。ユースケースは`class X(...) { suspend operator fun invoke(...): Result<T> }`というスタイルで、`runCatching`でリポジトリ呼び出しをラップしています。
- `feature/<featureName>/presentation` — `ViewModel`（androidx.lifecycleの`ViewModel`、`StateFlow<UiState>`を公開）、派生プロパティ（例: `canSave`）を持つ`UiState`データクラス、Composeの`Screen`コンポーザブル。
- `core/file` — Kotlinの`expect`/`actual`によるクロスプラットフォーム抽象化（現状は`LocalImageStorage`のみで、選択した画像バイト列をプラットフォームのローカルストレージに保存しパスを返す）。

新しいfeatureを追加する際も同じ`feature/<name>/{domain,presentation}`分割に従い、feature横断で使う共通の契約はトップレベルの`domain/`/`data/`パッケージに置いてください。

### データモデル

`daily_photos`（Room）はISO-8601形式の日付文字列（`dateString`、例: `"2026-08-01"`）を主キーとしており、主キー制約と`OnConflictStrategy.REPLACE`によるupsertによって「1日1枚」をDBレベルで強制しています。`PhotoDao`は月別カレンダー表示向けのprefix `LIKE`クエリ（`observePhotosByYearMonth`）と、「過去の同じ日」検索向けのsuffix `LIKE`クエリ（`observePhotosOnThisDay`、`"-MM-dd"`サフィックスでマッチ）もサポートしています。

「1日1枚」ルールの本体は`SaveDailyPhotoUseCase`です。対象日に既存の写真がないかをチェックし、`allowOverwrite = true`が渡されない限り`Result.failure(PhotoAlreadyExistsException)`を返します。`PhotoSelectorViewModel`はこの例外を汎用エラーとしてではなく、上書き確認ダイアログの表示トリガーとして扱います。

### expect/actualによるプラットフォームコード

- `Platform.kt` / `Platform.android.kt` / `Platform.ios.kt` — 基本的なプラットフォーム情報（`getPlatform()`）。
- `core/file/LocalImageStorage` — `expect class`で、Android向けの`actual`実装（`context.filesDir/photos/`へ書き込み）はありますが、**iOS向けの`actual`実装はまだありません**。実装を追加するまでiOSターゲットはコンパイルできません。

### 現時点で未完成の配線

現状、依存性注入(DI)の設定が一切ありません。`App.kt`は`koinViewModel()`/`koinInject()`を呼び出していますが、コードベースのどこにも`startKoin { ... }`の呼び出しやKoinの`module { }`定義が存在しません。また、`PhotoDao`/`PhotoEntity`は定義されているにもかかわらず、Roomの`@Database`/`RoomDatabase.Builder`クラスも存在しません。DIや永続化の配線に触れる作業では、これらがすでに存在するという前提を置かず、ゼロから追加する必要があります。これは現状アプリが起動時にクラッシュしている状態と一致します。

## 技術スタックに関する補足

- Kotlin 2.0.0、Compose Multiplatform 1.6.11。対象プラットフォームはAndroid（`compileSdk`/`targetSdk` 35、`minSdk` 24）とiOS（x64/arm64/simulatorArm64、静的フレームワーク）。
- 永続化: Room Multiplatform（アルファ版）+ バンドルされたSQLiteドライバ。RoomコンパイラにはKSPを使用。
- DI: Koin（`koin-core`、`koin-compose`、`koin-compose-viewmodel`）— 詳細は上記「現時点で未完成の配線」を参照。
- 画像選択: FileKit（`io.github.vinceglb:filekit-*`）をギャラリーピッカーとして使用（`PhotoSelectorScreen`から利用）。
- 日付: ドメイン/データ/プレゼンテーション層全体で`kotlinx-datetime`（`LocalDate`、`Clock`）を使用。
- 依存関係のバージョンは`gradle/libs.versions.toml`（バージョンカタログ）に集約されています。各モジュールの`build.gradle.kts`にバージョンを直書きするのではなく、こちらに追加してください。
- 既存コードのUI文字列やコメントは日本語で書かれています。近くのコードを編集する際はこの慣習に合わせてください。
