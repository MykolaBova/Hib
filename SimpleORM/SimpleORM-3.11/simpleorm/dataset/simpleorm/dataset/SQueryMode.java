package simpleorm.dataset;

/**
 * SFOR_UPDATE causes addtional database locking (Typically SELECT ... FOR UPDATE)
 *   Should be specified where an update is expected but not actually needed to update.
 * SBASIC no special database locking enabled.  Still optimistic locked.
 * SREAD_ONLY enforces that record not be modified.
 * SASSUME_CREATE suppresses the initial SELECT, just does an INSERT.
 */
public enum SQueryMode {
    SBASIC,
	SFOR_UPDATE,
	SREAD_ONLY,
	SREFERENCE_NO_QUERY,
	SASSUME_CREATE
}
	