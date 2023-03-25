# diagram

ドロネー図・ボロノイ図の描画をJavaで実装  
[ドロネー図・ボロノイ図の描画アルゴリズムの説明](https://qiita.com/Seo-4d696b75/items/c088f5b853010507224c)

## モジュール構成

- core  
  図形計算の処理関連ソース
- app  
  `core`機能を利用して[駅データの座標点](https://github.com/Seo-4d696b75/station_database) からボロノイ分割を計算する

## core モジュール

### ドロネー図

**DelaunayDiagram.java**

```kt
//母点の集合
val points: Collection<Point> = listOf()
//すべての母点を含む矩形
val rect = Rectangle(0, 0, 0, 0)

val diagram = DelaunayDiagram(points)
diagram.split(rect)

//分割された三角形とその辺
val triangles = diagram.getTriangles()
val edges = diagram.getEdges()
```

### ボロノイ図

**VoronoiDiagram.java**

```kt
//母点の集合
val points: Collection<Point> = listOf()
//すべての母点を含む矩形
val rect = Rectangle(0, 0, 0, 0)

val diagram = VoronoiDiagram(points)
diagram.split(rect)
```

## app モジュール

```bash
./gradlew app:run --args="${srcFile} ${dstFile}"
```

もしくは Run > Edit Configurations > Add から`jp.seo.station.app.MainKt`をターゲットに実行を設定

# Github Package + Gradle

他のプロジェクトから簡単に利用できます

## Publish方法

### Local

- 環境変数のセットアップ
    - GRADLE_PUBLISH_VERSION: パッケージのバージョン
    - GITHUB_PACKAGE_USERNAME: Githubのアカウント名
    - GITHUB_PACKAGE_TOKEN: GithubのアクセスToken（write:packagesの権限が必要）
- Gradleタスクの実行  
  `./gradlew assemble publish`

### GitHub Actions

`v${version}`という名前のtagをpushすると自動でpublish

[workflowファイル](./.github/workflows/publish.yml)

## 利用方法

駅座標点からからボロノイ分割を計算するコンソールアプリケーションの例

`build.gradle.kt`

```gradle.kt
plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("jp.seo.station.app.MainKt")
}

repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Seo-4d696b75/diagram")
        credentials {
            username = "your_github_name"
            password = "your_github_access_token"
        }
    }
    mavenCentral()
}

dependencies {
    implementation("com.github.seo4d696b75:diagram:0.1.0")
}
```

実行

```bash
./gradlew run --args="${srcFile} ${dstFile}"
```