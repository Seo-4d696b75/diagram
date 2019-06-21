# diagram
ドロネー図・ボロノイ図の描画をJavaで実装  
[ドロネー図・ボロノイ図の描画アルゴリズムの説明](https://qiita.com/Seo-4d696b75/items/c088f5b853010507224c)

## Package構成
* jp/ac/u_tokyo/t/eeic/seo/diagram  
全部このパッケージに入ってます  

## 使用例
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