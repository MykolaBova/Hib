package simpleorm.quickstart;


/**
 * This is the nice name formatter used to convert the test database used by SimpleORM.<p>
 * 
 * Normally there is a simple algrothem but is overwritten as required.<br>
 * 
 * @author <a href="mailto:richard.schmidt@inform6.com">Richard Schmidt</a>
 */
public class SimpleORMNiceNameFormatter implements INiceNameFormatter {
    /**
     * For most tables drop the leading XX_. Some table map to individual names.
     * 
     * @see simpleorm.quickstart.INiceNameFormatter#niceNameForTable(String)
     */
    public String niceNameForTable(String table) {
        //Map some of the funnies in simpleorg.examples		
        if (table.equalsIgnoreCase("XX_PAY_PERIOD")) {
            return "Period";
        }

        if (table.equalsIgnoreCase("XX_PAY_SLIP")) {
            return "PaySlip";
        }

        if (table.equalsIgnoreCase("XX_PSLIP_DETAIL")) {
            return "PaySlipDetail";
        }

        //Remove the XX_
        String str = table.substring(3);
        str = str.substring(0, 1).toUpperCase() +
            str.substring(1).toLowerCase();

        return str;
    }

    /**
     * Normally simply convert to uppercase. But since VALUE is a reserved word in Interbase, 
     * a '_' was added to the field in the database generation scrips. So remove them!
     * 
     * @see simpleorm.quickstart.INiceNameFormatter#niceNameForColumn(String)
     */
    public String niceNameForColumn(String table, String column) {
        if ((table.equalsIgnoreCase("XX_PSLIP_DETAIL")) &&
                (column.equalsIgnoreCase("VALUE_"))) {
            return "VALUE";
        }

        return column.toUpperCase();
    }

	/**
	 * Normally just return the nice name of the foreign table. but in the case of 
	 * Employee that references itself, return the field as MANAGER
	 * 
	 * @see simpleorm.quickstart.INiceNameFormatter#niceNameForForeignKey(String, String)
	 */
    public String niceNameForForeignKey(String localTable, String foreignTable) {
        if (localTable.equalsIgnoreCase("XX_EMPLOYEE") &&
                foreignTable.equalsIgnoreCase("XX_EMPLOYEE")) {
            return "MANAGER";
        }

        return foreignTable.substring(3).toUpperCase();
    }
}
