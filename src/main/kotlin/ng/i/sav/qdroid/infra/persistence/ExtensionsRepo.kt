package ng.i.sav.qdroid.infra.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExtensionsRepo : JpaRepository<Extensions, String>
