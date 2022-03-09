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
```ドロネー図
//母点の集合
Collection<Point> points; 
//すべての母点を含む矩形
Rectangle rect; 

DelaunayDiagram diagram = new DelaunayDiagram(points);
diagram.split(rect);

//分割された三角形とその辺
Set<Triangle> triangles = diagram.getTriangles();
Set<Edge> edges = diagram.getEdges();
```

### ボロノイ図
**VoronoiDiagram.java**
```ボロノイ図
//母点の集合
Collection<Point> points; 
//すべての母点を含む矩形
Rectangle rect;

VoronoiDiagram diagram = new VoronoiDiagram(points);
diagram.split(rect);
```

## app モジュール  
### 実行方法
```bash
gradle app:run --args="${srcFile} ${dstFile}"
```

もしくは Run > Edit Configurations > Add から`jp.seo.station.app.DiagramCalc`をターゲットに実行を設定
### build方法
Build > Build Artifacts > diagram.app.main:jar

`out`ディレクトリ下にjarファイルが出力されるので次のように利用する
```bash
java -jar ${path2jar} ${srcFile} ${dstFile}
```