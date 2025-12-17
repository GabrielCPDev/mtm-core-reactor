package io.iggdrasil.mtm.config.setup

import io.iggdrasil.mtm.config.props.MultiTenancyProperties

interface MultiTenancyConfigurer {
    fun configure(properties: MultiTenancyProperties)
}