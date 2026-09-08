# sample

station パッケージを利用した簡単なコンソールアプリケーション

例：station パッケージのテスト用データで計算する

```shell
cp station/src/test/resources/station.json sample/input.json
./gradlew sample:run --args="input.json output.json"
```