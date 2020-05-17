package org.example.life

interface Cell {
    val alive: Boolean
    fun countNextState()
    fun updateNextState()
    fun setNeighbours(neighbours: List<Cell>)
}