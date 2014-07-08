package org.julp.db;

/* KEY_COLUMNS: Generates WHERE stastement with Primary Key only */
/* KEY_AND_MODIFIED_COLUMNS: Generates WHERE stastement with Primary Key and modified columns  */
/* KEY_AND_UPDATEBLE_COLUMNS: Generates WHERE stastement with Primary Key and all updatable columns */
public enum OptimisticLock {
    KEY_COLUMNS, KEY_AND_MODIFIED_COLUMNS, KEY_AND_UPDATEBLE_COLUMNS;
}
