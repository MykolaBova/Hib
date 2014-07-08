package simpleorm.examples;

import static simpleorm.dataset.SFieldFlags.SPRIMARY_KEY;
import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldInteger;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;

/**
 * This test class defines the Payroll tables as static inner tables which
 * demonstrate identifying foreign keys.
 */
public class Payroll {

	static public class Period extends SRecordInstance {

//		public Period(boolean attached){
//			super(attached);
//		}
		public static final SRecordMeta<Period> meta = new SRecordMeta<Period>(Period.class,
				"XX_PAY_PERIOD");

		public static final SFieldInteger YEAR = new SFieldInteger(meta,
				"YEAR", SPRIMARY_KEY);

		public static final SFieldInteger PERIOD = new SFieldInteger(meta,
				"PERIOD", SPRIMARY_KEY);

		public static final SFieldDouble TOTAL_PAYROLL = new SFieldDouble(meta,
				"TOTAL_PAYROLL");

		public SRecordMeta<Period> getMeta() {
			return meta;
		};
	}

	static public class PaySlip extends SRecordInstance {

//		public PaySlip(boolean attached){
//			super(attached);
//		}
		public static final SRecordMeta<PaySlip> meta = new SRecordMeta<PaySlip>(PaySlip.class,
				"XX_PAY_SLIP");

		static final SFieldString INCONSISTENT_EMP_NR = // not recommended
		new SFieldString(meta, "INCONSIST_EMP_NR", 20, SPRIMARY_KEY);

		static final SFieldReference<Employee> EMPLOYEE =
			new SFieldReference<Employee>(meta, Employee.EMPLOYEE,
													   "INCONS",
													   new SFieldScalar[] { INCONSISTENT_EMP_NR },
													   new SFieldScalar[] {Employee.EMPEE_ID });

		static final SFieldInteger YEAR = new SFieldInteger(meta, "YEAR", SPRIMARY_KEY);
		static final SFieldInteger PERIOD_NR  = new SFieldInteger(meta, "PERIOD_NR",SPRIMARY_KEY);
		static final SFieldReference<Period> PERIOD =
			new SFieldReference<Period>(meta, Period.meta,
					"PERIOD",
					new SFieldScalar[] { YEAR, PERIOD_NR },
					new SFieldScalar[] { Period.YEAR, Period.PERIOD });

		public static final SFieldString COMMENTS = new SFieldString(meta,
				"COMMENTS", 200);

		public SRecordMeta<PaySlip> getMeta() {
			return meta;
		};
	}

	@SuppressWarnings("unchecked")
	static public class PaySlipDetail extends SRecordInstance {

//		public PaySlipDetail(boolean attached){
//			super(attached);
//		}
		public static final SRecordMeta<PaySlipDetail> meta = new SRecordMeta<PaySlipDetail>(
				PaySlipDetail.class, "XX_PSLIP_DTL");

		static final SFieldString INCONSISTENT_EMP_NR = // not recommended
			new SFieldString(meta, "INCONSIST_EMP_NR", 20, SPRIMARY_KEY);
		static final SFieldInteger YEAR = new SFieldInteger(meta, "YEAR", SPRIMARY_KEY);
		static final SFieldInteger PERIOD_NR  = new SFieldInteger(meta, "PERIOD_NR",SPRIMARY_KEY);
		static final SFieldReference PAY_SLIP = new SFieldReference(meta,
				PaySlip.meta, "PAY_SLIP",
				new SFieldScalar[]{PERIOD_NR, YEAR, INCONSISTENT_EMP_NR}, // bad order, sorted by SimpleOrm
				new SFieldScalar[]{PaySlip.PERIOD_NR, PaySlip.YEAR, PaySlip.INCONSISTENT_EMP_NR});

		public static final SFieldInteger DETAIL_TYPE = // Should be Enum
		new SFieldInteger(meta, "DETAIL_TYPE", SPRIMARY_KEY);

		public static final SFieldDouble VALUE = new SFieldDouble(meta, "VALUE");

		public SRecordMeta<PaySlipDetail> getMeta() {
			return meta;
		};
	}

	static public class UglyPaySlipDetail extends SRecordInstance {

//		public UglyPaySlipDetail(boolean attached){
//			super(attached);
//		}
		public static final SRecordMeta<UglyPaySlipDetail> meta 
            = new SRecordMeta(UglyPaySlipDetail.class, "XX_UGLY_PAY_DTL");

		static final SFieldString UGLY_EMP_NR = new SFieldString(meta,
				"UGLY_EMP_NR", 20, SPRIMARY_KEY);
		
		static final SFieldInteger YEAR = new SFieldInteger(meta, "YEAR", SPRIMARY_KEY);
		static final SFieldInteger PERIOD_NR  = new SFieldInteger(meta, "PERIOD_NR", SPRIMARY_KEY);

		static final SFieldReference<Employee> EMPLOYEE = new SFieldReference(meta,
				Employee.EMPLOYEE, "UGLYEMP",
				new SFieldScalar[] { UGLY_EMP_NR },
				new SFieldScalar[] { Employee.EMPEE_ID });

		static final SFieldReference<Period> PERIOD = new SFieldReference(meta,
				Period.meta, "PERIOD",
				new SFieldScalar[] { YEAR, PERIOD_NR },
				new SFieldScalar[] { Period.YEAR, Period.PERIOD });

		static final SFieldReference<PaySlip> PAY_SLIP = new SFieldReference(meta,
				PaySlip.meta, "UGLY_PAYSLIP",
				new SFieldScalar[] { UGLY_EMP_NR, YEAR, PERIOD_NR },
				new SFieldScalar[] { PaySlip.INCONSISTENT_EMP_NR, PaySlip.YEAR, PaySlip.PERIOD_NR });

		public static final SFieldInteger DETAIL_TYPE = // Should be Enum
		new SFieldInteger(meta, "DETAIL_TYPE", SPRIMARY_KEY);

		public static final SFieldDouble VALUE = new SFieldDouble(meta, "VALUE");

		public SRecordMeta<UglyPaySlipDetail> getMeta() {
			return meta;
		};
	}
}
