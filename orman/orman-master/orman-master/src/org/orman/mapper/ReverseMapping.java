package org.orman.mapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.orman.dbms.ResultList.ResultRow;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.OneToOne;
import org.orman.sql.Query;
import org.orman.util.logging.Log;

/**
 * Provides reverse mapping engine which can convert {@link ResultRow} objects
 * into instances whose their class definitions extend {@link Model}. (which
 * they are also {@link Entity}.
 * 
 * Has smart and safe type conversions so that returned data is appropriate.
 * 
 * @author ahmet alp balkan <ahmetalpbalkan@gmail.com>
 */
public class ReverseMapping {
	private static final DateFormat dateTimeParser = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final DateFormat dateParser = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	@SuppressWarnings("unchecked")
	public static <E> E map(ResultRow row, Class<E> type, Entity e) {

		E instance = null;

		try {
			/* instantiate and cast to intended type */
			instance = (E) e.getDefaultConstructor().newInstance();
		} catch (Exception e1) {
			// TODO assuming that no invocation exceptions will occur at
			// runtime.
			Log.error(e1.getMessage());
		}

		if (instance != null) {
			for (Field f : e.getFields()) {
				if (!f.isList()) {
					/* Use direct value from database */
					// do not fill field if it is already filled somehow.

					// read field value
					Object fieldValue = row.getColumn(f.getGeneratedName());

					// TODO do needed conversions. (McCabe cyclomatic
					// complexity)
					fieldValue = smartCasting(fieldValue, f.getClazz());

					// according to lazy loading policy.
					fieldValue = makeCardinalityBinding(f, instance, fieldValue);

					// Reverse Map enum fields.
					if (f.getClazz().isEnum()) {
						Object[] enumConstant = f.getClazz().getEnumConstants();
						try {
							if (fieldValue != null)
								fieldValue = enumConstant[new Integer(
										fieldValue.toString())];
						} catch (Exception ex) {
							Log.error(
									"Unable to reverse map enum type %s with value %s (%s)",
									f.getClazz().getName(),
									fieldValue == null ? null : fieldValue);
							fieldValue = null;
						}

					}

					// set field
					if (fieldValue != null)
						((Model<?>) instance).setEntityField(f, e, fieldValue);
				} else {
					Object fieldValue = makeCardinalityBinding(f, instance,
							((Model<?>) instance)
									.getEntityField(((Model<?>) instance)
											.getEntity()
											.getAutoIncrementField()));

					if (fieldValue != null)
						((Model<?>) instance).setEntityField(f, e, fieldValue);
				}
			}
			((Model<?>) instance).makePersistent(); // because record is not
													// dirty.
		}
		return instance;
	}

	/**
	 * Returns related instance (or instances) of specified cardinality if some
	 * @*To* annotation exists on the given field. Returns the same object if
	 * the field does not have such relationship annotations.
	 * 
	 * Threats the given value as key for finding related entities.
	 * 
	 * @param <E>
	 * @param f
	 * @param instance
	 *            to make bidirectional mapping.
	 * @param key
	 * @return the original key if key is already null.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <E> Object makeCardinalityBinding(Field f, E instance,
			Object key) {
		if (key == null)
			return key;

		Class<?> intendedType = f.getClazz();

		if (!MappingSession.entityExists(intendedType)) {
			/* our entity is not mappable! */
			return key; // return the same
		} else {
			if (((Model<?>) instance).getEntityField(f) != null)
				return null; // field is already filled somehow.

			Entity intendedEntity = MappingSession.getEntity(intendedType);

			if (!f.isList()) {
				/* we have a 1:1 mapping without checking the annotation */
				boolean doLoading = false;

				doLoading |= f.isAnnotationPresent(OneToOne.class)
						&& f.getAnnotation(OneToOne.class).load()
								.equals(LoadingPolicy.EAGER);

				doLoading |= f.isAnnotationPresent(ManyToOne.class)
						&& f.getAnnotation(ManyToOne.class).load()
								.equals(LoadingPolicy.EAGER);

				// if no annotations present on field do loading
				doLoading |= !f.isAnnotationPresent(OneToOne.class)
						&& !f.isAnnotationPresent(ManyToOne.class);

				if (doLoading) {
					Query c = ModelQuery
							.select()
							.from(intendedEntity)
							.where(C.eq(intendedType, intendedEntity
									.getAutoIncrementField().getOriginalName(),
									key)).getQuery();

					E result = (E) Model.fetchSingle(c, intendedType);

					// make reverse binding on the target (result) if requested
					if (f.isAnnotationPresent(OneToOne.class)) {
						OneToOne ann = f.getAnnotation(OneToOne.class);
						if (result != null
								&& !"".equals(ann.targetBindingField())) {
							Field on = F.f(f.getClazz(),
									ann.targetBindingField());
							((Model<E>) result).setEntityField(on,
									intendedEntity, instance);
						}
					}
					return result;
				}

				return null;
			} else {
				/* we have a 1:* or *:* mapping */
				boolean doLoading = false;

				OneToMany ann = f.getAnnotation(OneToMany.class);
				doLoading |= (ann != null)
						&& ann.load().equals(LoadingPolicy.EAGER);

				if (doLoading) {
					OneToMany config = f.getAnnotation(OneToMany.class);
					Query q = ModelQuery.select().from(intendedEntity)
							.where(C.eq(intendedType, config.onField(), key))
							.getQuery();

					List<?> resultList = Model.fetchQuery(q, intendedType);
					return new EntityList(instance.getClass(), intendedType,
							instance, resultList);
				} else {
					// set lazy loading EntityList
					return new EntityList(instance.getClass(), intendedType,
							instance, true);
				}

			}
		}
	}

	/**
	 * makes conversions String<->long<->integer<->boolean.
	 * 
	 * @param value
	 * @param clazz
	 *            desired class type.
	 * @return casted instance. it may be newly created. do not rely on
	 *         reference. it may return the same instance if no eligible changes
	 *         are found. null if <code>value</code> is null.
	 */
	private static Object smartCasting(Object value, Class<?> desired) {
		if (value == null)
			return null;

		if (Integer.class.equals(desired) || Integer.TYPE.equals(desired)) {
			// destination: Integer.

			if (value.getClass().equals(Integer.class)
					|| value.getClass().equals(Integer.TYPE))
				return value;
			else
				return new Integer(value.toString());
		}

		if (Long.class.equals(desired) || Long.TYPE.equals(desired)) {
			// destination: Long.
			if (value.getClass().equals(Long.class)
					|| value.getClass().equals(Long.TYPE))
				return value;
			else
				return new Long(value.toString());
		}

		if (Float.class.equals(desired) || Float.TYPE.equals(desired)) {
			// destination: Float.
			if (value.getClass().equals(Float.class)
					|| value.getClass().equals(Float.TYPE))
				return value;
			else
				return new Float(value.toString());
		}

		if (Double.class.equals(desired) || Double.TYPE.equals(desired)) {
			// destination: Double.
			if (value.getClass().equals(Double.class)
					|| value.getClass().equals(Double.TYPE))
				return value;
			else
				return new Double(value.toString());
		}

		if (Boolean.class.equals(desired) || Boolean.TYPE.equals(desired)) {
			// destination: Double.
			if (value.getClass().equals(Boolean.class)
					|| value.getClass().equals(Boolean.TYPE))
				return value;
			else
				return ((Integer) smartCasting(value, Integer.TYPE)) > 0;
		}

		if (String.class.equals(desired)) {
			// destination: String.
			if (value.getClass().equals(String.class))
				return value;
			else
				return ((Integer) smartCasting(value, Integer.TYPE)) > 0;
		}

		if (Date.class.equals(desired)) {
			// destination: java.util.Date.
			if (value.getClass().equals(Date.class))
				return value;
			else {
				try {
					String dateStr = value.toString();
					Date d = null;

					if (dateStr.length() > 10) {
						d = dateTimeParser.parse(dateStr);
					} else {
						d = dateParser.parse(dateStr);
					}
					return d;
				} catch (ParseException e) {
					Log.error("The following date could not be parsed: "
							+ value.toString());
					return null;
				}
			}
		}

		return value;
	}
}
