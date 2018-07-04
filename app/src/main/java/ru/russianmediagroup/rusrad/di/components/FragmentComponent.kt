package ru.russianmediagroup.rusrad.di.components

import dagger.Component
import ru.russianmediagroup.rusrad.di.modules.PresentersModule
import ru.kodep.quickeaters.di.PerFragment

/**
 * @author by Arthur Korchagin on 03.12.17.
 */
@PerFragment
@Component(dependencies = arrayOf(ActivityComponent::class),
        modules = arrayOf(PresentersModule::class))
interface FragmentComponent {


}