package ro.mihneabaia.api.repository

import ro.mihneabaia.api.repository.base.DbAware

trait DataAccess extends DbAware with ArenaRepository with ContactRepository with VenueRepository
