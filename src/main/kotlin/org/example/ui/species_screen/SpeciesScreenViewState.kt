package org.example.ui.species_screen

import org.example.life.Configuration
import org.example.life.Species

data class SpeciesScreenViewState(
    val config: Configuration,
    val currentSpecies: Species = config.species.first()
)