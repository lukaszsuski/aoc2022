/**
--- Day 8: Treetop Tree House ---
The expedition comes across a peculiar patch of tall trees all planted carefully in a grid. The Elves explain that a previous expedition planted these trees as a reforestation effort. Now, they're curious if this would be a good location for a tree house.

First, determine whether there is enough tree cover here to keep a tree house hidden. To do this, you need to count the number of trees that are visible from outside the grid when looking directly along a row or column.

The Elves have already launched a quadcopter to generate a map with the height of each tree (your puzzle input). For example:

30373
25512
65332
33549
35390
Each tree is represented as a single digit whose value is its height, where 0 is the shortest and 9 is the tallest.

A tree is visible if all of the other trees between it and an edge of the grid are shorter than it. Only consider trees in the same row or column; that is, only look up, down, left, or right from any given tree.

All of the trees around the edge of the grid are visible - since they are already on the edge, there are no trees to block the view. In this example, that only leaves the interior nine trees to consider:

The top-left 5 is visible from the left and top. (It isn't visible from the right or bottom since other trees of height 5 are in the way.)
The top-middle 5 is visible from the top and right.
The top-right 1 is not visible from any direction; for it to be visible, there would need to only be trees of height 0 between it and an edge.
The left-middle 5 is visible, but only from the right.
The center 3 is not visible from any direction; for it to be visible, there would need to be only trees of at most height 2 between it and an edge.
The right-middle 3 is visible from the right.
In the bottom row, the middle 5 is visible, but the 3 and 4 are not.
With 16 trees visible on the edge and another 5 visible in the interior, a total of 21 trees are visible in this arrangement.

Consider your map; how many trees are visible from outside the grid?

Your puzzle answer was 1787.

--- Part Two ---
Content with the amount of tree cover available, the Elves just need to know the best spot to build their tree house: they would like to be able to see a lot of trees.

To measure the viewing distance from a given tree, look up, down, left, and right from that tree; stop if you reach an edge or at the first tree that is the same height or taller than the tree under consideration. (If a tree is right on the edge, at least one of its viewing distances will be zero.)

The Elves don't care about distant trees taller than those found by the rules above; the proposed tree house has large eaves to keep it dry, so they wouldn't be able to see higher than the tree house anyway.

In the example above, consider the middle 5 in the second row:

30373
25512
65332
33549
35390
Looking up, its view is not blocked; it can see 1 tree (of height 3).
Looking left, its view is blocked immediately; it can see only 1 tree (of height 5, right next to it).
Looking right, its view is not blocked; it can see 2 trees.
Looking down, its view is blocked eventually; it can see 2 trees (one of height 3, then the tree of height 5 that blocks its view).
A tree's scenic score is found by multiplying together its viewing distance in each of the four directions. For this tree, this is 4 (found by multiplying 1 * 1 * 2 * 2).

However, you can do even better: consider the tree of height 5 in the middle of the fourth row:

30373
25512
65332
33549
35390
Looking up, its view is blocked at 2 trees (by another tree with a height of 5).
Looking left, its view is not blocked; it can see 2 trees.
Looking down, its view is also not blocked; it can see 1 tree.
Looking right, its view is blocked at 2 trees (by a massive tree of height 9).
This tree's scenic score is 8 (2 * 2 * 1 * 2); this is the ideal spot for the tree house.

Consider each tree on your map. What is the highest scenic score possible for any tree?

Your puzzle answer was 440640.
 * */

class Day8 : Day(8) {

    class TreeMap(val map: List<List<Int>>) {
        private val ySize = map.size
        private val xSize = map[0].size

        fun visibleTrees(): Int {
            return map.withIndex()
                .sumOf { column ->
                    column.value.withIndex()
                        .count { row -> isVisible(column.index, row.index, row.value) }
                }
        }

        fun highestScenicScore(): Int {
            return map.withIndex()
                .filter { it.index in 1 until ySize-1 }
                .maxOf { row ->
                    row.value.withIndex()
                        .filter { it.index in 1 until xSize-1 }
                        .maxOf { col -> scenicScore(row.index, col.index, col.value) }
                }
        }

        private fun scenicScore(row: Int, col: Int, value: Int): Int {

            var visibleOnLeft = map[row]
                .filterIndexed { idx, _ -> idx < col }
                .takeLastWhile { it < value }
                .count()
            if (visibleOnLeft < distanceFrom(Direction.LEFT, row, col)) visibleOnLeft += 1

            var visibleOnRight = map[row]
                .filterIndexed { idx, _ -> idx > col }
                .takeWhile { it < value }
                .count()
            if (visibleOnRight < distanceFrom(Direction.RIGHT, row, col)) visibleOnRight += 1

            var visibleOnTop = map
                .filterIndexed { idx, _ -> idx < row }
                .takeLastWhile { it[col] < value }
                .count()
            if (visibleOnTop < distanceFrom(Direction.TOP, row, col)) visibleOnTop += 1

            var visibleOnBottom = map
                .filterIndexed { idx, _ -> idx > row }
                .takeWhile { it[col] < value }
                .count()
            if (visibleOnBottom < distanceFrom(Direction.BOTTOM, row, col)) visibleOnBottom += 1

            return visibleOnLeft * visibleOnRight * visibleOnTop * visibleOnBottom
        }

        enum class Direction {
            LEFT, RIGHT, TOP, BOTTOM
        }

        private fun distanceFrom(dir: Direction, row: Int, col: Int): Int {
            return when(dir) {
                Direction.LEFT   -> col
                Direction.RIGHT  -> xSize - col - 1
                Direction.TOP    -> row
                Direction.BOTTOM -> ySize - row - 1
            }
        }

        private fun isVisible(col: Int, row: Int, value: Int): Boolean {
            val visibleFromTop = map[col]
                .filterIndexed { idx, _ -> idx < row }
                .all { it < value }

            val visibleFromBottom = map[col]
                .filterIndexed { idx, _ -> idx > row }
                .all { it < value }

            val visibleFromLeft = map
                .filterIndexed { idx, _ -> idx < col }
                .all { it[row] < value }

            val visibleFromRight = map
                .filterIndexed { idx, _ -> idx > col }
                .all { it[row] < value }

            return visibleFromTop or visibleFromBottom or visibleFromLeft or visibleFromRight
        }

    }

    override fun solve(input: List<String>) {

        val treeMap = TreeMap(input.map {
            it.toList().map {
                it.digitToInt()
            }
        })

        println("part1: ${treeMap.visibleTrees()}")//1787
        println("part2: ${treeMap.highestScenicScore()}")//440640

    }

}



