package com.seo4d696b75.diagram.sample

import com.seo4d696b75.diagram.station.calculateStationDiagram

/**
 * 駅座標点集合のドロネー・ボロノイ分割と Kd-tree 構造を計算する
 *
 * @param args 入力・出力のファイルパス２つ
 * 1. 入力 [Station]のリスト相当のJSONファイルのパス
 * 2. 出力 [Result]のJSONファイルを書き出すパス
 */
fun main(args: Array<String>) {
    require(args.size >= 2)
    calculateStationDiagram(args[0], args[1])
}
