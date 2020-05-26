package org.example.ui.species_screen

import org.example.life.Configuration
import org.example.life.Species

sealed class SpeciesScreenPartialState {
    data class NewConfig(val config: Configuration) : SpeciesScreenPartialState()
    data class SpeciesChosen(val species: Species) : SpeciesScreenPartialState()
    data class NewSpeciesCreated(val config: Configuration, val speciesId: Int) : SpeciesScreenPartialState()
}