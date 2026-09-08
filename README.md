# diagram

ドロネー図・ボロノイ図の描画をJavaで実装  
[ドロネー図・ボロノイ図の描画アルゴリズムの説明](https://qiita.com/Seo-4d696b75/items/c088f5b853010507224c)

[![Maven version](https://img.shields.io/maven-central/v/com.seo4d696b75.diagram/core)](https://central.sonatype.com/artifact/com.seo4d696b75.diagram/core)
![License MIT](https://img.shields.io/badge/Apache_2.0-9E9F9F?label=License)

## 利用方法

`build.gradle.kts`

```kotlin
dependencies {
  // 駅座標の計算
  implementation("com.seo4d696b75.diagram:station:$version")

  // 図形計算の基本実装のみ
  implementation("com.seo4d696b75.diagram:core:$version")
}
```

参考：[sample モジュール](sample/README.md)は station パッケージを利用する簡単なコンソールアプリケーションの実装例です

## core パッケージ

基本的な図形計算の実装

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

## station パッケージ

core を利用して[駅データの座標点](https://github.com/Seo-4d696b75/station_database)
からボロノイ分割などを計算するロジックを提供する

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

## Publish方法

以下の状態を前提とする

- GPGによる署名方法は準備済み（鍵を生成済み）
- 署名に使用するのは主鍵ではなく副鍵
- Central Portalにアカウントを作成済み・namespaceも取得済み

### 0. GPG公開鍵の送信（初回のみ）

Gitコミットへの署名だけでは不十分。
Maven Centralへの公開には、鍵サーバーへの公開鍵の登録が必要

```shell
gpg --keyserver keyserver.ubuntu.com --send-keys $id
```

### 1. バージョン更新

- libs.versions.toml `versions.publish` を更新する
- 署名付きのタグを打つ `git tag -s $name -m $message`

### 2. UserToken確認

初回・有効期限切れの場合は[再発行が必要です](#usertoken取得)

https://central.sonatype.com/usertoken

`~/.gradle/gradle.properties`ファイルに記載しておく

```properties
mavenCentralUsername=username
mavenCentralPassword=the_password
```

### 3. 署名用の鍵ID確認

公開スクリプト実行時に必要な鍵IDを確認する

```shell
gpg --list-secret-keys --keyid-format=short
```

出力例の `ssb ed25519/` の `/` 以降の8文字（Short形式）を使用

```shell
sec#  ed25519/${主鍵のkeyid} ${主鍵の有効期限} [C]
uid         [ unknown] ${user.name} <${user.email}>
ssb   ed25519/${副鍵のkeyid} ${副鍵の有効期限} [S]
```

### 4. 公開スクリプトの実行

```shell
./publish.sh YOUR_KEY_ID YOUR_PASSPHRASE
```

スクリプトは以下の処理を自動で行います：

- GPG秘密鍵のエクスポート（メモリ内のみ）
- 環境変数経由でGradleへ署名情報を渡す
- [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) の実行

### 5. 確認

https://central.sonatype.com/publishing/deployments
