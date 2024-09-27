package ng.i.sav.qdroid.infra.annotation

import ng.i.sav.qdroid.infra.persistence.H2Configuration
import org.springframework.context.annotation.Import

@Import(H2Configuration::class)
annotation class EnableH2Database