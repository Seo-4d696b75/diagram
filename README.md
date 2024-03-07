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

入力: 駅座標のリスト

```json
[
  {
    "code": 1110101,
    "name": "函館",
    "lat": 41.773709,
    "lng": 140.726413
  }
]
```

出力: 各種図形計算の結果
- [left, right: kd-tree構造](https://github.com/Seo-4d696b75/station_database/wiki/kdtree)
- [next: ドロネー分割における隣接点の駅コード一覧](https://github.com/Seo-4d696b75/station_database/wiki/station-area)
- [voronoi: ボロノイ分割の領域図形（GeoJSON形式）](https://github.com/Seo-4d696b75/station_database/wiki/geojson)

```json
{
  "root": 3001218,
  "node_list":[
    {
      "lat":41.773709,
      "lng":140.726413,
      "code":1110101,
      "name":"函館",
      "right":1110108,
      "left":1120505,
      "next":[9910514,1110102,9910518,9910622,9910621,9910515,9910623,9910517],
      "voronoi":{
        "type":"Feature",
        "geometry":{
          "type":"Polygon",
          "coordinates":[
            [
              [140.72591,41.771256],
              [140.717527,41.773829],
              [140.71735,41.774204],
              [140.714999,41.785757],
              [140.714787,41.792259],
              [140.72972,41.788694],
              [140.730562,41.78452],
              [140.731074,41.778908],
              [140.72591,41.771256]
            ]
          ]
        },
        "properties":{}
      }
    }
  ]
}
```

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