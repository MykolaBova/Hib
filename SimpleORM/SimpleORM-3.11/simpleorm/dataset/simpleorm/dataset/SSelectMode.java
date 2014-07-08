package simpleorm.dataset;

/**
 * Public interface to globaly choose which fields to retrieve
 * SDESCRIPTIVE : only descriptive fields, typically pkeys and labels
 * SNORMAL : all fields except those marked SUNQUIERED
 * SALL : all fields, including SUNQUIERED
 * SNONE : used for SQuery.join to not add joined columns to Select list.
 * 
 * @author franck
 */
public enum SSelectMode {
	SNONE,
	SDESCRIPTIVE,
	SNORMAL,
	SALL
}
