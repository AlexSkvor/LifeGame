package org.example.life

class SimpleSequentialCell(startCellState: Boolean) : Cell {

    override val alive: Boolean
        get() = currentState

    override fun countNextState() {
        val aliveNeighboursCount = neighbours.count { it.alive }
        countedNextState = if (alive) (aliveNeighboursCount in 2..3)
        else (aliveNeighboursCount == 3)
    }

    override fun updateNextState() {
        currentState = countedNextState
    }

    private lateinit var neighbours: List<Cell>
    override fun setNeighbours(neighbours: List<Cell>) {
        this.neighbours = neighbours
    }

    private var currentState: Boolean = startCellState
    private var countedNextState: Boolean = startCellState

}